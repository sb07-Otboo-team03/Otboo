package com.codeit.otboo.domain.clothes.recommendation.service;

import com.codeit.otboo.domain.clothes.attribute.attributevalue.entity.ClothesAttributeValue;
import com.codeit.otboo.domain.clothes.management.entity.Clothes;
import com.codeit.otboo.domain.clothes.management.entity.ClothesType;
import com.codeit.otboo.domain.clothes.management.mapper.ClothesMapper;
import com.codeit.otboo.domain.clothes.management.repository.ClothesRepository;
import com.codeit.otboo.domain.clothes.recommendation.dto.response.RecommendationResponse;
import com.codeit.otboo.domain.profile.repository.ProfileRepository;
import com.codeit.otboo.domain.weather.entity.Weather;
import com.codeit.otboo.domain.weather.exception.WeatherNotFoundException;
import com.codeit.otboo.domain.weather.repository.WeatherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RecommendationServiceImpl implements RecommendationService {

    private final ClothesRepository clothesRepository;
    private final WeatherRepository weatherRepository;
    private final ProfileRepository profileRepository;
    private final ClothesMapper clothesMapper;

    // 필터용 키워드 상수
    private static final List<String> ONE_PIECE_KEYWORDS = List.of("원피스");
    private static final List<String> LAYERED_KEYWORDS = List.of("뷔스티에", "나시");
    private static final List<String> ALLOWED_TOP_KEYWORDS = List.of("셔츠", "반팔", "긴팔", "티셔츠");

    @Override
    public RecommendationResponse recommend(UUID weatherId, UUID userId) {
        Weather weather = weatherRepository.findById(weatherId)
                .orElseThrow(()-> new WeatherNotFoundException(weatherId));

        List<Clothes> clothes = clothesRepository.findByOwnerId(userId);

        Map<ClothesType, List<Clothes>> grouped =
                clothes.stream().collect(Collectors.groupingBy(Clothes::getType));

        List<Clothes> selected = pickOnePerCategory(grouped);

        // TOP에서 원피스가 나오면 BOTTOM 제외
        selected = applyOnePieceRules(selected);

        Map<UUID, List<String>> groupingMap = clothes.stream()
                .flatMap(c->c.getValues().stream())
                        .collect(Collectors.groupingBy(
                                v->v.getAttributeDef().getId(),
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
                                .map(c->clothesMapper.toDto(
                                        c, null, groupingMap
                                ))
                                .toList()
                )
                .build();
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
    private List<Clothes> applyOnePieceRules(List<Clothes> selected){
        Optional<Clothes> onePieceOpt = selected.stream()
                .filter(this::isOnePiece).findFirst();

        if(onePieceOpt.isEmpty()) return selected;

        Clothes onePiece = onePieceOpt.get();

        boolean isLayered = isLayeredOnePiece(onePiece);

        return selected.stream()
                .filter(c -> {
                    // 하의 제외
            if(c.getType() == ClothesType.BOTTOM) {
                return false;
            }
            // 일반 원피스는 상의 제외
            if(!isLayered && c.getType() == ClothesType.TOP && !isOnePiece(c)) {
                return isAllowedTop(c);
            }
            return true;
        }).toList();
    }

    // 문자열 처리
    private String normalize(String name){
        return name == null ? "" : name.replaceAll("\\s+", "").toLowerCase();
    }

    private boolean containsKeyword(Clothes c, List<String> keywords) {
        String name = normalize(c.getName());

        return keywords.stream().anyMatch(name::contains);
    }

    // 도메인 판별
    private boolean isOnePiece(Clothes c) {
        return containsKeyword(c, ONE_PIECE_KEYWORDS);
    }

    private boolean isLayeredOnePiece(Clothes c) {
        return containsKeyword(c, LAYERED_KEYWORDS);
    }

    private boolean isAllowedTop(Clothes c){
        return containsKeyword(c, ALLOWED_TOP_KEYWORDS);
    }

}
