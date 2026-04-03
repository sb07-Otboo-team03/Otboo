package com.codeit.otboo.domain.clothes.recommendation.unit.service;

import com.codeit.otboo.domain.clothes.attribute.attributedef.entity.ClothesAttributeDef;
import com.codeit.otboo.domain.clothes.attribute.attributevalue.entity.ClothesAttributeValue;
import com.codeit.otboo.domain.clothes.management.dto.response.ClothesResponse;
import com.codeit.otboo.domain.clothes.management.entity.Clothes;
import com.codeit.otboo.domain.clothes.management.entity.ClothesType;
import com.codeit.otboo.domain.clothes.management.mapper.ClothesMapper;
import com.codeit.otboo.domain.clothes.management.repository.ClothesRepository;
import com.codeit.otboo.domain.clothes.recommendation.dto.response.RecommendationResponse;
import com.codeit.otboo.domain.clothes.recommendation.service.RecommendationServiceImpl;
import com.codeit.otboo.domain.weather.entity.Weather;
import com.codeit.otboo.domain.weather.exception.WeatherNotFoundException;
import com.codeit.otboo.domain.weather.repository.WeatherRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {
    @Mock
    private ClothesRepository clothesRepository;
    @Mock
    private WeatherRepository weatherRepository;
    @Mock
    private ClothesMapper clothesMapper;
    @InjectMocks
    private RecommendationServiceImpl recommendationService;

    // 테스트용 헬퍼메서드
    private Clothes createClothes(ClothesType type) {
        Clothes clothes = mock(Clothes.class);
        when(clothes.getType()).thenReturn(type);
        return clothes;
    }

    private Clothes createClothesWithValue() {
        Clothes clothes = mock(Clothes.class);
        ClothesAttributeValue value = mock(ClothesAttributeValue.class);
        ClothesAttributeDef def = mock(ClothesAttributeDef.class);

        UUID defId = UUID.randomUUID();

        when(def.getId()).thenReturn(defId);
        when(value.getAttributeDef()).thenReturn(def);
        when(value.getSelectableValue()).thenReturn("RED");

        when(clothes.getType()).thenReturn(ClothesType.TOP);
        when(clothes.getValues())
                .thenReturn(List.of(value));

        return clothes;
    }


    @Test
    @DisplayName("추천 정상 동작 - 타입별 1개씩 반환")
    void recommendation_Success() {
        // given
        UUID weatherId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Weather weather = mock(Weather.class);

        when(weatherRepository.findById(weatherId))
                .thenReturn(Optional.of(weather));

        Clothes top1 = createClothes(ClothesType.TOP);
        Clothes top2 = createClothes(ClothesType.TOP);
        Clothes bottom1 = createClothes(ClothesType.BOTTOM);

        List<Clothes> clothesList = List.of(top1, top2, bottom1);

        when(clothesRepository.findAll()).thenReturn(clothesList);
        when(clothesMapper.toDto(any(), any(), any()))
                .thenReturn(mock(ClothesResponse.class));

        // when
        RecommendationResponse response = recommendationService.recommend(weatherId, userId);

        // then
        assertThat(response.weatherId()).isEqualTo(weatherId);
        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.clothes()).hasSize(2);
        verify(clothesMapper, times(2)).toDto(any(), any(), any());

    }

    @Test
    @DisplayName("호출 실패 - 날씨 Id 없음")
    void recommendation_Fail_weatherId_Not_Found() {
        // given
        UUID weatherId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(weatherRepository.findById(weatherId))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> recommendationService.recommend(weatherId, userId))
                .isInstanceOf(WeatherNotFoundException.class)
                .hasMessage("날씨 정보를 찾을 수 없습니다.");

    }

    @Test
    @DisplayName("groupingMap확인")
    void recommendation_groupingMap_Success() {
        // given
        UUID weatherId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Weather weather = mock(Weather.class);
        when(weatherRepository.findById(weatherId))
                .thenReturn(Optional.of(weather));

        Clothes clothes = createClothesWithValue();
        when(clothesRepository.findAll()).thenReturn(List.of(clothes));

        when(clothesMapper.toDto(any(), any(), any())).thenReturn(mock(ClothesResponse.class));


        // when
        recommendationService.recommend(weatherId, userId);

        // then
        ArgumentCaptor<Map<UUID, List<String>>> captor = ArgumentCaptor.forClass(Map.class);

        verify(clothesMapper).toDto(any(), any(), captor.capture());

        Map<UUID, List<String>> groupingMap = captor.getValue();

        assertThat(groupingMap).hasSize(1);
        assertThat(groupingMap.values().iterator().next()).contains("RED");
    }

}