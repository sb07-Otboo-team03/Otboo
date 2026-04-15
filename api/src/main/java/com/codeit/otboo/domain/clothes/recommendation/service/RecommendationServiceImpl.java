package com.codeit.otboo.domain.clothes.recommendation.service;

import com.codeit.otboo.domain.binarycontent.entity.BinaryContent;
import com.codeit.otboo.domain.binarycontent.resolver.BinaryContentUrlResolver;
import com.codeit.otboo.domain.clothes.attribute.attributevalue.entity.ClothesAttributeValue;
import com.codeit.otboo.domain.clothes.management.entity.Clothes;
import com.codeit.otboo.domain.clothes.management.entity.ClothesType;
import com.codeit.otboo.domain.clothes.management.mapper.ClothesMapper;
import com.codeit.otboo.domain.clothes.management.repository.ClothesRepository;
import com.codeit.otboo.domain.clothes.recommendation.dto.response.RecommendationResponse;
import com.codeit.otboo.domain.profile.entity.Profile;
import com.codeit.otboo.domain.profile.exception.ProfileNotFoundException;
import com.codeit.otboo.domain.profile.repository.ProfileRepository;
import com.codeit.otboo.domain.weather.entity.Weather;
import com.codeit.otboo.domain.weather.exception.WeatherNotFoundException;
import com.codeit.otboo.domain.weather.repository.WeatherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class RecommendationServiceImpl implements RecommendationService {

    private final ClothesRepository clothesRepository;
    private final WeatherRepository weatherRepository;
    private final ProfileRepository profileRepository;
    private final BinaryContentUrlResolver binaryContentUrlResolver;
    private final ClothesMapper clothesMapper;

    private final Random random;

    // 온도 기준 상수
    private static final int HOT = 25;
    private static final int WARM = 18;
    private static final int COOL = 10;
    private static final int COLD = 4;

    @Override
    public RecommendationResponse recommend(UUID weatherId, UUID userId) {
        Weather weather = weatherRepository.findById(weatherId)
                .orElseThrow(() -> new WeatherNotFoundException(weatherId));
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ProfileNotFoundException(userId));

        // 프로필의 온도 민감도 반영
        int sensitivity = profile.getTemperatureSensitivity();
        List<Clothes> clothes = clothesRepository.findByOwnerId(userId);

        // 날씨 + 민감도 필터
        clothes = applyWeatherFilter(clothes, weather, sensitivity);

        Map<ClothesType, List<Clothes>> grouped =
                clothes.stream().collect(Collectors.groupingBy(Clothes::getType));

        List<Clothes> selected = pickOnePerCategory(grouped);

        // 카테고리에서 원피스가 나오면 BOTTOM 제외
//        selected = applyOnePieceRules(selected, grouped);

        Map<UUID, List<String>> groupingMap = clothes.stream()
                .flatMap(c -> c.getValues().stream())
                .collect(Collectors.groupingBy( // 타입별로 묶기
                        v -> v.getAttributeDef().getId(),
                        Collectors.mapping(
                                ClothesAttributeValue::getSelectableValue,
                                Collectors.toList()
                        )
                ));

        log.info("effectiveTemp: {}, sensitivity: {}, rawTemp: {}",
                weather.getTemperatureCurrent() + (sensitivity - 3), sensitivity, weather.getTemperatureCurrent());

        return RecommendationResponse.builder()
                .weatherId(weatherId)
                .userId(userId)
                .clothes(
                        selected.stream()
                                .map(c -> clothesMapper.toDto(
                                        c, resolveImageUrl(c.getBinaryContent()), groupingMap
                                ))
                                .toList()
                )
                .build();
    }

    // 날씨 필터 (카테고리 생존여부 결정)
    private List<Clothes> applyWeatherFilter(List<Clothes> clothes, Weather weather, int sensitivity) {
        double temp = getEffectiveTemp(weather, sensitivity);

        return clothes.stream()
                .filter(c -> {
                    String name = normalize(c.getName());
                    return switch (c.getType()) {
                        case TOP -> isValidTop(name, temp);
                        case DRESS -> isValidDress(name, temp);
                        case OUTER -> isValidOuter(name, temp);
                        case SHOES -> isValidShoes(name, weather, temp);
                        default -> true; // BOTTOM, HAT, ACCESSORY, BAG은 필터 없이 통과
                    };
                })
                .toList();
    }

    // TOP 필터
    private boolean isValidTop(String name, double temp) {
        TopType type = TopType.from(name);
        if (type == null) return false;

        return switch (type) {
            case SHORT_SLEEVE -> temp >= WARM; // 기온 >= 18
            case SLEEVELESS -> temp >= HOT; // 기온 >= 25
            case LONG_SLEEVE -> temp < HOT; // 기온 < 25
            case KNIT -> temp < WARM; // 기온 < 18
            case SWEATSHIRT -> temp < HOT; // 기온 < 25
            case HOODIE -> temp < HOT; // 기온 < 25
            case TURTLENECK ->  temp < COLD; // 기온 < 4
        };
    }

    // DRESS 필터
    private boolean isValidDress(String name, double temp) {
        DressType type = DressType.from(name);
        if (type == null) return false;

        return switch (type) {
            case SHORT_SLEEVE -> temp >= WARM; // 기온 >= 18
            case SLEEVELESS -> temp >= HOT; // 기온 >= 25
            case LONG_SLEEVE -> temp < HOT; // 기온 < 25
            case KNIT -> temp < WARM; // 기온 < 18
        };
    }

    // OUTER 필터
    private boolean isValidOuter(String name, double temp) {
        OuterType type = OuterType.from(name);
        if (type == null) return false;

        return switch (type) {
            case CARDIGAN -> temp < HOT && temp >= WARM; // 18 <= 기온 < 25
            case JACKET -> temp < WARM && temp >= COLD; // 10 <= 기온 < 18
            case COAT -> temp < WARM && temp >= COLD; // 10 <= 기온 < 18
            case PADDING -> temp < COOL; // 기온 < 10
        };
    }

    // SHOES 필터 (온도 → 날씨 순서)
    private boolean isValidShoes(String name, Weather weather, double temp) {
        ShoesType type = ShoesType.from(name);
        if (type == null) return false;

        // 1차: 온도 기준 기본 허용 목록
        Set<ShoesType> allowed = getShoesByTemp(temp);

        // 2차: 날씨 보정
        switch (weather.getPrecipitationType()) {
            case RAIN, SHOWER, RAIN_SNOW -> {
                allowed.add(ShoesType.RAIN_BOOTS);
                allowed.remove(ShoesType.SANDALS);
                allowed.remove(ShoesType.SLIPPERS);
                allowed.remove(ShoesType.FORMAL);
            }
            case SNOW -> {
                allowed.add(ShoesType.WINTER_BOOTS);
                allowed.remove(ShoesType.SANDALS);
                allowed.remove(ShoesType.SLIPPERS);
                allowed.remove(ShoesType.FORMAL);
            }
            case NONE -> {
                allowed.add(ShoesType.SANDALS);
                allowed.add(ShoesType.FORMAL);
                allowed.add(ShoesType.BOOTS);
                allowed.add(ShoesType.SNEAKERS);
                allowed.add(ShoesType.SLIPPERS);
            }
        }

        return allowed.contains(type);
    }

    private Set<ShoesType> getShoesByTemp(double temp) {
        Set<ShoesType> result = new HashSet<>();
        result.add(ShoesType.SNEAKERS); // 항상 허용

        if (temp >= HOT) { // 기온 >= 25
            result.add(ShoesType.SANDALS);
            result.add(ShoesType.SLIPPERS);
        } else if (temp >= WARM) { // 기온 >= 18
            result.add(ShoesType.FORMAL);
            result.add(ShoesType.BOOTS);
            result.add(ShoesType.SLIPPERS);
        } else if (temp >= COOL) { // 기온 >= 10
            result.add(ShoesType.BOOTS);
            result.add(ShoesType.FORMAL);
        } else {
            result.add(ShoesType.BOOTS);
            result.add(ShoesType.WINTER_BOOTS);
        }
        return result;
    }

    // 카테고리별 1개 선택 (랜덤)
    private List<Clothes> pickOnePerCategory(Map<ClothesType, List<Clothes>> grouped) {
        List<Clothes> result = new ArrayList<>();

        pickUpperBody(result, grouped, random);

        List<ClothesType> others = List.of(
                ClothesType.SHOES,
                ClothesType.OUTER,
                ClothesType.HAT,
                ClothesType.BAG
        );
        for (ClothesType other : others) {
            List<Clothes> items = grouped.getOrDefault(other, List.of());
            if (!items.isEmpty()) {
                result.add(items.get(random.nextInt(items.size())));
            }
        }

        List<Clothes> accessories = grouped.getOrDefault(ClothesType.ACCESSORY, List.of());
        if (!accessories.isEmpty() && random.nextBoolean()) {
            result.add(accessories.get(random.nextInt(accessories.size())));
        }

        return result;
    }

    private void pickUpperBody(
            List<Clothes> result,
            Map<ClothesType, List<Clothes>> grouped,
            Random random
    ) {
        List<Clothes> dresses = grouped.getOrDefault(ClothesType.DRESS, List.of());
        List<Clothes> tops = grouped.getOrDefault(ClothesType.TOP, List.of());
        List<Clothes> bottoms = grouped.getOrDefault(ClothesType.BOTTOM, List.of());

        boolean hasDress = !dresses.isEmpty();
        boolean hasTopBottom = !tops.isEmpty() && !bottoms.isEmpty();

        if (hasDress && hasTopBottom) {
            // 둘 다 가능하면 DRESS 30% / TOP+BOTTOM 70%
            if (random.nextInt(10) < 3) {
                pickDress(result, dresses, tops, random);
            } else {
                result.add(tops.get(random.nextInt(tops.size())));
                result.add(bottoms.get(random.nextInt(bottoms.size())));
            }
        } else if (hasDress) {
            pickDress(result, dresses, tops, random);
        } else if (hasTopBottom) {
            result.add(tops.get(random.nextInt(tops.size())));
            result.add(bottoms.get(random.nextInt(bottoms.size())));
        }
    }

    private void pickDress(
            List<Clothes> result, List<Clothes> dresses, List<Clothes> tops, Random random
    ) {
        Clothes dress = dresses.get(random.nextInt(dresses.size()));
        result.add(dress);

        // 뷔스티에, 나시 원피스면 상의 레이어링 추가
        DressType dressType = DressType.from(normalize(dress.getName()));
        if(dressType == DressType.SLEEVELESS && !tops.isEmpty()) {
            result.add(tops.get(random.nextInt(tops.size())));
        }
    }

    /**
     * 공통 유틸
     */
    // 체감온도 = {현재온도 + (온도 민감도 - 디폴트값) }
    private double getEffectiveTemp(Weather weather, int sensitivity) {
        return weather.getTemperatureCurrent() + (sensitivity - 3);
    }

    // 문자열 처리
    private String normalize(String name) {
        return name == null ? "" : name.replaceAll("\\s+", "").toLowerCase();
    }

    private String resolveImageUrl(BinaryContent binaryContent) {
        if (binaryContent == null) return null;
        return binaryContentUrlResolver.resolve(binaryContent.getId());
    }
}
