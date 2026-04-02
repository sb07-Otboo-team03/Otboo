package com.codeit.otboo.domain.clothes.recommendation.service;

import com.codeit.otboo.domain.clothes.attribute.attributevalue.entity.ClothesAttributeValue;
import com.codeit.otboo.domain.clothes.management.entity.Clothes;
import com.codeit.otboo.domain.clothes.management.entity.ClothesType;
import com.codeit.otboo.domain.clothes.management.mapper.ClothesMapper;
import com.codeit.otboo.domain.clothes.management.repository.ClothesRepository;
import com.codeit.otboo.domain.clothes.recommendation.dto.response.RecommendationResponse;
import com.codeit.otboo.domain.weather.entity.Weather;
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
    private final ClothesMapper clothesMapper;

    @Override
    public RecommendationResponse recommend(UUID weatherId, UUID userId) {
        Weather weather = weatherRepository.findById(weatherId)
                .orElseThrow(() -> new RuntimeException("weatherId를 찾을 수 없습니다."));

        List<Clothes> clothes = clothesRepository.findAll();

        Map<ClothesType, List<Clothes>> grouped =
                clothes.stream().collect(Collectors.groupingBy(Clothes::getType));

        List<Clothes> selected = pickOnePerCategory(grouped);

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

    // 랜덤으로 갖고오기
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

}
