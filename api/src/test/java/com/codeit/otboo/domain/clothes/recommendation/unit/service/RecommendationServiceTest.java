package com.codeit.otboo.domain.clothes.recommendation.unit.service;

import com.codeit.otboo.domain.binarycontent.resolver.BinaryContentUrlResolver;
import com.codeit.otboo.domain.clothes.attribute.attributedef.entity.ClothesAttributeDef;
import com.codeit.otboo.domain.clothes.attribute.attributevalue.entity.ClothesAttributeValue;
import com.codeit.otboo.domain.clothes.management.dto.response.ClothesResponse;
import com.codeit.otboo.domain.clothes.management.entity.Clothes;
import com.codeit.otboo.domain.clothes.management.entity.ClothesType;
import com.codeit.otboo.domain.clothes.management.mapper.ClothesMapper;
import com.codeit.otboo.domain.clothes.management.repository.ClothesRepository;
import com.codeit.otboo.domain.clothes.recommendation.dto.response.RecommendationResponse;
import com.codeit.otboo.domain.clothes.recommendation.service.RecommendationServiceImpl;
import com.codeit.otboo.domain.profile.entity.Profile;
import com.codeit.otboo.domain.profile.exception.ProfileNotFoundException;
import com.codeit.otboo.domain.profile.repository.ProfileRepository;
import com.codeit.otboo.domain.weather.entity.PrecipitationType;
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
    private ProfileRepository profileRepository;
    @Mock
    private BinaryContentUrlResolver binaryContentUrlResolver;
    @Mock
    private ClothesMapper clothesMapper;
    @InjectMocks
    private RecommendationServiceImpl recommendationService;

    // 테스트용 헬퍼메서드
    private Clothes createClothes(ClothesType type, String name) {
        Clothes clothes = mock(Clothes.class);
        lenient().when(clothes.getType()).thenReturn(type);
        lenient().when(clothes.getName()).thenReturn(name);
        lenient().when(clothes.getValues()).thenReturn(List.of());
        lenient().when(clothes.getBinaryContent()).thenReturn(null);
        return clothes;
    }

    private Weather mockWeather(double temp, PrecipitationType type) {
        Weather weather = mock(Weather.class);
        lenient().when(weather.getTemperatureCurrent()).thenReturn(temp);
        lenient().when(weather.getPrecipitationType()).thenReturn(type);
        return weather;
    }

    private Profile mockProfile(int sensitivity) {
        Profile profile = mock(Profile.class);
        lenient().when(profile.getTemperatureSensitivity()).thenReturn(sensitivity);
        return profile;
    }

    private Clothes createClothesWithValue(
            ClothesType type,
            String name,
            UUID defId,
            String valueString
    ) {
        Clothes clothes = mock(Clothes.class);
        ClothesAttributeValue value = mock(ClothesAttributeValue.class);
        ClothesAttributeDef def = mock(ClothesAttributeDef.class);

        when(def.getId()).thenReturn(defId);
        when(value.getAttributeDef()).thenReturn(def);
        when(value.getSelectableValue()).thenReturn(valueString);

        when(clothes.getType()).thenReturn(type);
        when(clothes.getName()).thenReturn(name);
        when(clothes.getValues()).thenReturn(List.of(value));
        when(clothes.getBinaryContent()).thenReturn(null);

        return clothes;
    }

    private void defaultSetting(
            UUID weatherId,
            UUID userId,
            Weather weather,
            Profile profile,
            List<Clothes> clothesList
    ) {
        when(weatherRepository.findById(weatherId)).thenReturn(Optional.of(weather));
        when(profileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(clothesRepository.findByOwnerId(userId)).thenReturn(clothesList);
//        when(clothesMapper.toDto(any(), any(), any())).thenReturn(mock(ClothesResponse.class));
        when(clothesMapper.toDto(any(), any(), any())).thenAnswer(invocation -> {
            Clothes clothes = invocation.getArgument(0);
            return new ClothesResponse(UUID.randomUUID(), UUID.randomUUID(), clothes.getName(),
                    null, null, null);
        });
    }


    @Test
    @DisplayName("추천 정상 동작 - 타입별 1개씩 반환")
    void recommendation_Success() {
        // given
        UUID weatherId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Weather weather = mockWeather(20, PrecipitationType.NONE);
        Profile profile = mockProfile(3);

        Clothes top1 = createClothes(ClothesType.TOP, "긴팔티");
        Clothes bottom1 = createClothes(ClothesType.BOTTOM, "청바지");

        defaultSetting(weatherId, userId, weather, profile, List.of(top1, bottom1));

        // when
        RecommendationResponse response = recommendationService.recommend(weatherId, userId);

        // then
        assertThat(response.clothes()).hasSize(2);
    }

    @Test
    @DisplayName("25도 이상 반팔 추천")
    void hot_only_short_sleeve() {
        // given
        UUID weatherId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Weather weather = mockWeather(25, PrecipitationType.NONE);
        Profile profile = mockProfile(3);

        Clothes shortSleeve = createClothes(ClothesType.TOP, "반팔티");
        Clothes longSleeve = createClothes(ClothesType.TOP, "긴팔티");

        defaultSetting(weatherId, userId, weather, profile, List.of(shortSleeve, longSleeve));

        // when
        RecommendationResponse response = recommendationService.recommend(weatherId, userId);

        // then
        assertThat(response.clothes()).hasSize(1);
        assertThat(response.clothes())
                .extracting(ClothesResponse::name)
                .anyMatch(name -> name.contains("반팔티"));
    }

    @Test
    @DisplayName("4도 이상 기모티 추천")
    void cold_long_sleeve() {
        // given
        UUID weatherId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Weather weather = mockWeather(4, PrecipitationType.NONE);
        Profile profile = mockProfile(3);

        Clothes shortSleeve = createClothes(ClothesType.TOP, "반팔티");
        Clothes longSleeve = createClothes(ClothesType.TOP, "기모 맨투맨");

        defaultSetting(weatherId, userId, weather, profile, List.of(shortSleeve, longSleeve));

        // when
        RecommendationResponse response = recommendationService.recommend(weatherId, userId);

        // then
        assertThat(response.clothes()).hasSize(1);
        assertThat(response.clothes())
                .extracting(ClothesResponse::name)
                .anyMatch(name -> name.contains("기모 맨투맨"));
    }

    @Test
    @DisplayName("4도 이하 목폴라 추천")
    void cold_cold_top_sleeve() {
        // given
        UUID weatherId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Weather weather = mockWeather(4, PrecipitationType.NONE);
        Profile profile = mockProfile(1);

        Clothes shortSleeve = createClothes(ClothesType.TOP, "반팔 셔츠");
        Clothes longSleeve = createClothes(ClothesType.TOP, "검정 목폴라");

        defaultSetting(weatherId, userId, weather, profile, List.of(shortSleeve, longSleeve));

        // when
        RecommendationResponse response = recommendationService.recommend(weatherId, userId);

        // then
        assertThat(response.clothes()).hasSize(1);
        assertThat(response.clothes())
                .extracting(ClothesResponse::name)
                .anyMatch(name -> name.contains("목폴라"));
    }

    @Test
    @DisplayName("비오고 더운 날 장화 추천")
    void rain_hot_shoes() {
        // given
        UUID weatherId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Weather weather = mockWeather(25, PrecipitationType.RAIN);
        Profile profile = mockProfile(3);

        Clothes rainBoot = createClothes(ClothesType.SHOES, "장화");
        Clothes walker = createClothes(ClothesType.SHOES, "워커");

        defaultSetting(weatherId, userId, weather, profile, List.of(rainBoot, walker));

        // when
        RecommendationResponse response = recommendationService.recommend(weatherId, userId);

        // then
        assertThat(response.clothes()).hasSize(1);
        assertThat(response.clothes())
                .extracting(ClothesResponse::name)
                .anyMatch(name -> name.contains("장화"));
    }

    @Test
    @DisplayName("눈오고 추운 날 겨울부츠 추천")
    void snow_cold_shoes() {
        // given
        UUID weatherId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Weather weather = mockWeather(7, PrecipitationType.SNOW);
        Profile profile = mockProfile(2);

        Clothes snowBoot = createClothes(ClothesType.SHOES, "양털 부츠");
        Clothes walker = createClothes(ClothesType.SHOES, "워커");

        defaultSetting(weatherId, userId, weather, profile, List.of(snowBoot, walker));

        // when
        RecommendationResponse response = recommendationService.recommend(weatherId, userId);

        // then
        assertThat(response.clothes()).hasSize(1);
        assertThat(response.clothes())
                .extracting(ClothesResponse::name)
                .anyMatch(name -> name.contains("양털 부츠"));
    }

    @Test
    @DisplayName("맑은 날 신발 추천")
    void sunnyDay_shoes() {
        // given
        UUID weatherId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Weather weather = mockWeather(15, PrecipitationType.NONE);
        Profile profile = mockProfile(2);

        Clothes runningShoes  = createClothes(ClothesType.SHOES, "운동화");
        Clothes rainShoes = createClothes(ClothesType.SHOES, "장화");

        defaultSetting(weatherId, userId, weather, profile, List.of(runningShoes, rainShoes));

        // when
        RecommendationResponse response = recommendationService.recommend(weatherId, userId);

        // then
        assertThat(response.clothes()).hasSize(1);
        assertThat(response.clothes())
                .extracting(ClothesResponse::name)
                .anyMatch(name -> name.contains("운동화"));
    }

    @Test
    @DisplayName("맑고 기온낮은 날 신발 추천")
    void sunnyDay_cold_shoes() {
        // given
        UUID weatherId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Weather weather = mockWeather(2, PrecipitationType.NONE);
        Profile profile = mockProfile(1);

        Clothes walker  = createClothes(ClothesType.SHOES, "워커");
        Clothes snowShoes  = createClothes(ClothesType.SHOES, "양털부츠");
        Clothes rainShoes = createClothes(ClothesType.SHOES, "장화");

        defaultSetting(weatherId, userId, weather, profile, List.of(walker, snowShoes, rainShoes));

        // when
        RecommendationResponse response = recommendationService.recommend(weatherId, userId);

        // then
        assertThat(response.clothes()).hasSize(1);
        assertThat(response.clothes())
                .extracting(ClothesResponse::name)
                .anyMatch(name -> name.contains("양털부츠") || name.contains("워커"));
    }

    @Test
    @DisplayName("맑고 기온 높은 날 신발 추천")
    void sunnyDay_hot_shoes() {
        // given
        UUID weatherId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Weather weather = mockWeather(18, PrecipitationType.NONE);
        Profile profile = mockProfile(5);

        Clothes walker  = createClothes(ClothesType.SHOES, "워커");
        Clothes sunnyShoes  = createClothes(ClothesType.SHOES, "샌들");
        Clothes rainShoes = createClothes(ClothesType.SHOES, "장화");

        defaultSetting(weatherId, userId, weather, profile, List.of(walker, sunnyShoes, rainShoes));

        // when
        RecommendationResponse response = recommendationService.recommend(weatherId, userId);

        // then
        assertThat(response.clothes()).hasSize(1);
        assertThat(response.clothes())
                .extracting(ClothesResponse::name)
                .anyMatch(name -> name.contains("샌들"));
    }

    @Test
    @DisplayName("원피스는 하의 선택 제외")
    void onepiece_removes_bottom() {
        // given
        UUID weatherId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Weather weather = mockWeather(20, PrecipitationType.NONE);
        Profile profile = mockProfile(3);

        Clothes onePiece = createClothes(ClothesType.DRESS, "원피스");
        Clothes bottom = createClothes(ClothesType.BOTTOM, "청바지");

        defaultSetting(weatherId, userId, weather, profile, List.of(onePiece, bottom));

        // when
        RecommendationResponse response = recommendationService.recommend(weatherId, userId);

        // then
        assertThat(response.clothes()).hasSize(1);
        assertThat(response.clothes())
                .extracting(ClothesResponse::name)
                .anyMatch(name -> name.contains("원피스"));
    }

    @Test
    @DisplayName("일반 원피스는 상의 제외")
    void onepiece_removes_top() {
        // given
        UUID weatherId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Weather weather = mockWeather(20, PrecipitationType.NONE);
        Profile profile = mockProfile(3);

        Clothes onePiece = createClothes(ClothesType.DRESS, "원피스");
        Clothes top = createClothes(ClothesType.TOP, "긴팔 무지 티");
        Clothes bottom = createClothes(ClothesType.BOTTOM, "청바지");

        defaultSetting(weatherId, userId, weather, profile, List.of(onePiece, top, bottom));

        // when
        RecommendationResponse response = recommendationService.recommend(weatherId, userId);

        // then
        assertThat(response.clothes()).hasSize(1);
        assertThat(response.clothes())
                .extracting(ClothesResponse::name)
                .anyMatch(name -> name.contains("원피스"));
    }

    @Test
    @DisplayName("뷔스티에 원피스는 상의 선택")
    void layeredOnepiece_choice_top() {
        // given
        UUID weatherId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Weather weather = mockWeather(20, PrecipitationType.NONE);
        Profile profile = mockProfile(3);

        Clothes layeredOnePiece = createClothes(ClothesType.DRESS, "뷔스티에 원피스");
        Clothes top = createClothes(ClothesType.TOP, "긴팔 무지 티");
        Clothes bottom = createClothes(ClothesType.BOTTOM, "청바지");

        defaultSetting(weatherId, userId, weather, profile, List.of(layeredOnePiece, top, bottom));

        // when
        RecommendationResponse response = recommendationService.recommend(weatherId, userId);

        // then
        assertThat(response.clothes()).hasSize(2);
        assertThat(response.clothes())
                .extracting(ClothesResponse::name)
                .anyMatch(name -> name.contains("뷔스티에"));
        assertThat(response.clothes())
                .extracting(ClothesResponse::name)
                .anyMatch(name -> name.contains("긴팔"));
    }

    @Test
    @DisplayName("온도 민감도 반영 - 민감도 1(추위탐)")
    void temperature_sensitivity_1() {
        // given
        UUID weatherId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        // 18도부터는 긴팔+반팔 추천
        Weather weather = mockWeather(19, PrecipitationType.NONE);

        // 온도 민감도 반영 => 체감온도 17도
        Profile profile = mockProfile(1);

        Clothes shortSleeve = createClothes(ClothesType.TOP, "반팔티");
        Clothes longSleeve = createClothes(ClothesType.TOP, "긴팔티");

        defaultSetting(weatherId, userId, weather, profile, List.of(shortSleeve, longSleeve));

        // when
        RecommendationResponse response = recommendationService.recommend(weatherId, userId);

        // then
        assertThat(response.clothes()).hasSize(1);
        assertThat(response.clothes())
                .extracting(ClothesResponse::name)
                .anyMatch(name -> name.contains("긴팔"));
    }

    @Test
    @DisplayName("온도 민감도 반영 - 민감도 5(더위탐)")
    void temperature_sensitivity_5() {
        // given
        UUID weatherId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        // 18도부터는 긴팔+반팔 추천
        Weather weather = mockWeather(16, PrecipitationType.NONE);

        // 온도 민감도 반영 => 체감온도 18도
        Profile profile = mockProfile(5);

        Clothes shortSleeve = createClothes(ClothesType.TOP, "반팔셔츠");
        Clothes longSleeve = createClothes(ClothesType.TOP, "긴팔셔츠");

        defaultSetting(weatherId, userId, weather, profile, List.of(shortSleeve, longSleeve));

        RecommendationResponse response = recommendationService.recommend(weatherId, userId);
        // when & then
        assertThat(response.clothes()).hasSize(1);
        assertThat(response.clothes())
                .extracting(ClothesResponse::name)
                .anyMatch(name -> name.contains("반팔") || name.contains("긴팔"));
    }

    @Test
    @DisplayName("groupingMap확인")
    void recommendation_groupingMap_Success() {
        // given
        UUID weatherId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Weather weather = mockWeather(20, PrecipitationType.NONE);
        Profile profile = mockProfile(3);

        UUID defId = UUID.randomUUID();

        Clothes clothes = createClothesWithValue(
                ClothesType.TOP,
                "긴팔티",
                defId,
                "RED"
        );

        defaultSetting(weatherId, userId, weather, profile, List.of(clothes));

        recommendationService.recommend(weatherId, userId);

        ArgumentCaptor<Map<UUID, List<String>>> captor = ArgumentCaptor.forClass(Map.class);

        verify(clothesMapper).toDto(any(), any(), captor.capture());

        Map<UUID, List<String>> map = captor.getValue();

        assertThat(map.get(defId)).contains("RED");
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
    @DisplayName("프로필 정보 없음")
    void profile_not_found() {
        // given
        UUID weatherId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Weather weather = mockWeather(20, PrecipitationType.NONE);

        when(weatherRepository.findById(weatherId)).thenReturn(Optional.of(weather));
        when(profileRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> recommendationService.recommend(weatherId, userId))
                .isInstanceOf(ProfileNotFoundException.class);
    }

}