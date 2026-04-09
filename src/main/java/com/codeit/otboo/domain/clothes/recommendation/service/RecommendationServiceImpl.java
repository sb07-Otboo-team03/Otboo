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
import com.codeit.otboo.domain.weather.entity.PrecipitationType;
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

    // 온도 기준 상수
    private static final int HOT = 25;
    private static final int WARM = 18;
    private static final int COOL = 10;
    private static final int COLD = 4;

    // 필터용 키워드 상수
    private static final List<String> SHORT_SLEEVE = List.of("반팔");
    private static final List<String> LONG_SLEEVE = List.of("긴팔");
    private static final List<String> WARM_TOP = List.of("니트", "기모");
    private static final List<String> VERY_COLD_TOP = List.of("폴라", "목티");

    private static final List<String> RAIN_HOT_SHOES = List.of("장화", "레인부츠");
    private static final List<String> RAIN_COLD_SHOES = List.of("양털부츠");

    private static final List<String> ONE_PIECE_KEYWORDS = List.of("원피스");
    private static final List<String> LAYERED_KEYWORDS = List.of("뷔스티에", "나시");
    private static final List<String> ALLOWED_TOP_KEYWORDS = List.of("셔츠", "반팔", "긴팔", "티셔츠");

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

        // TOP에서 원피스가 나오면 BOTTOM 제외
        selected = applyOnePieceRules(selected);

        Map<UUID, List<String>> groupingMap = clothes.stream()
                .flatMap(c -> c.getValues().stream())
                .collect(Collectors.groupingBy(
                        v -> v.getAttributeDef().getId(),
                        Collectors.mapping(
                                ClothesAttributeValue::getSelectableValue,
                                Collectors.toList()
                        )
                ));

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

    // 날씨 필터
    private List<Clothes> applyWeatherFilter(List<Clothes> clothes, Weather weather, int sensitivity) {
        double temp = getEffectiveTemp(weather, sensitivity);

        return clothes.stream()
                .filter(c -> {
                    String name = normalize(c.getName());

                    //상의
                    if (c.getType() == ClothesType.TOP) {
                        if (!isValidTopByTemp(name, temp)) return false;
                    }

                    //신발
                    if (c.getType() == ClothesType.SHOES) {
                        if (!isValidShoes(name, weather, temp)) return false;
                    }
                    return true;
                })
                .toList();
    }

    // 체감온도 = {현재온도 + (온도 민감도 - 디폴트값) }
    private double getEffectiveTemp(Weather weather, int sensitivity) {
        return weather.getTemperatureCurrent() + (sensitivity - 3);
    }

    // 온도별 상의 필터
    private boolean isValidTopByTemp(String name, double temp) {
        if (temp >= HOT) {
            return containsKeyword(name, SHORT_SLEEVE);
        }
        if (temp >= WARM) {
            return containsKeyword(name, merge(SHORT_SLEEVE, LONG_SLEEVE));
        }
        if (temp >= COOL) {
            return containsKeyword(name, LONG_SLEEVE);
        }
        if (temp >= COLD) {
            return containsKeyword(name, merge(LONG_SLEEVE, WARM_TOP));
        }
        return containsKeyword(name, VERY_COLD_TOP);
    }

    // 비/눈 + 신발 필터
    private boolean isValidShoes(String name, Weather weather, double temp) {
        Set<String> candidates = new HashSet<>();

        // 1차 선별 : 날씨 기준
        switch (weather.getPrecipitationType()) {
            case RAIN:
            case SHOWER:
            case RAIN_SNOW:
                candidates = new HashSet<>(RAIN_HOT_SHOES);
                break;

            case SNOW:
                candidates = new HashSet<>(RAIN_COLD_SHOES);
                break;

            case NONE:
            default:
                candidates = getShoesByTemp(temp); // 2차 선별(기온 기준)
                break;
        }
        boolean result = containsKeyword(name, candidates);

        return result;
    }

    private Set<String> getShoesByTemp(double temp) {
        Set<String> result = new HashSet<>();

        //항상 허용
        result.add("운동화");

        if (temp >= 20) {
            result.add("샌들");
        } else if (temp >= 10) {
            result.add("워커");
        } else {
            result.add("워커");
            result.add("양털부츠");
        }
        return result;
    }


    // 카테고리별 랜덤 선택
    private List<Clothes> pickOnePerCategory(Map<ClothesType, List<Clothes>> grouped) {
        List<Clothes> result = new ArrayList<>();
        Random random = new Random();

        for (List<Clothes> items : grouped.values()) {
            if (!items.isEmpty()) {
                result.add(items.get(random.nextInt(items.size())));
            }
        }
        return result;
    }

    // 원피스 규칙
    private List<Clothes> applyOnePieceRules(List<Clothes> selected) {
        Optional<Clothes> onePieceOpt = selected.stream()
                .filter(this::isOnePiece).findFirst();

        if (onePieceOpt.isEmpty()) return selected;

        Clothes onePiece = onePieceOpt.get();

        if (isLayeredOnePiece(onePiece)) {
            return handleLayeredOnePiece(selected);
        } else {
            return handleNormalOnePiece(selected);
        }
    }

    // 일반 원피스
    private List<Clothes> handleNormalOnePiece(List<Clothes> selected) {
        return selected.stream()
                .filter(c -> c.getType() != ClothesType.TOP &&
                        c.getType() != ClothesType.BOTTOM
                ).toList();
    }

    // 레이어드 원피스
    private List<Clothes> handleLayeredOnePiece(List<Clothes> selected) {
        return selected.stream()
                .filter(c -> {
                    // 하의 제고
                    if (c.getType() == ClothesType.BOTTOM) return false;

                    // 상의 일부 허용
                    if (c.getType() == ClothesType.TOP) {
                        return containsKeyword(
                                normalize(c.getName()), ALLOWED_TOP_KEYWORDS
                        );
                    }
                    return true;
                }).toList();
    }

    private String resolveImageUrl(BinaryContent binaryContent) {
        if (binaryContent == null) return null;
        return binaryContentUrlResolver.resolve(binaryContent.getId());
    }

    // 문자열 처리
    private String normalize(String name) {
        return name == null ? "" : name.replaceAll("\\s+", "").toLowerCase();
    }

    private boolean containsKeyword(String name, Collection<String> keywords) {
        return keywords.stream().anyMatch(name::contains);
    }

    private List<String> merge(List<String>... lists) {
        return Arrays.stream(lists)
                .flatMap(Collection::stream)
                .toList();
    }

    // 도메인 판별
    private boolean isOnePiece(Clothes c) {
        return containsKeyword(normalize(c.getName()), ONE_PIECE_KEYWORDS);
    }

    private boolean isLayeredOnePiece(Clothes c) {
        return containsKeyword(normalize(c.getName()), LAYERED_KEYWORDS);
    }
}
