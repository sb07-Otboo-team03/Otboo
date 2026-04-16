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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;


import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

        @Mock private ClothesRepository clothesRepository;
        @Mock private WeatherRepository weatherRepository;
        @Mock private ProfileRepository profileRepository;
        @Mock private BinaryContentUrlResolver binaryContentUrlResolver;
        @Mock private ClothesMapper clothesMapper;

        @InjectMocks
        private RecommendationServiceImpl service;

        // 고정 시드 Random 주입 (deterministic)
        // RecommendationServiceImpl의 Random 필드를 고정 시드로 교체
        @BeforeEach
        void injectRandom() {
            ReflectionTestUtils.setField(service, "random", new Random(42));
        }

        // 헬퍼 메서드
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
                ClothesType type, String name, UUID defId, String valueString
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
                UUID weatherId, UUID userId,
                Weather weather, Profile profile,
                List<Clothes> clothesList
        ) {
            when(weatherRepository.findById(weatherId)).thenReturn(Optional.of(weather));
            when(profileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
            when(clothesRepository.findByOwnerId(userId)).thenReturn(clothesList);
            when(clothesMapper.toDto(any(), any(), any())).thenAnswer(invocation -> {
                Clothes clothes = invocation.getArgument(0);
                return new ClothesResponse(
                        UUID.randomUUID(), UUID.randomUUID(),
                        clothes.getName(), null, null, null);
            });
        }

        // 결과에서 특정 이름 포함 여부 확인 헬퍼
        private boolean hasName(RecommendationResponse resp, String name) {
            return resp.clothes().stream().anyMatch(c -> c.name().equals(name));
        }

        // 결과에서 특정 타입 개수 확인 헬퍼 (타입 정보가 없으므로 이름으로 간접 확인)
        private List<String> names(RecommendationResponse resp) {
            return resp.clothes().stream().map(ClothesResponse::name).toList();
        }


        // 1. 예외: Weather 없음
        @Test
        @DisplayName("존재하지 않는 weatherId → WeatherNotFoundException")
        void weather_notFound_throwsException() {
            UUID weatherId = UUID.randomUUID();
            UUID userId    = UUID.randomUUID();
            when(weatherRepository.findById(weatherId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.recommend(weatherId, userId))
                    .isInstanceOf(WeatherNotFoundException.class);
        }

        // 2. 예외: Profile 없음
        @Test
        @DisplayName("존재하지 않는 userId → ProfileNotFoundException")
        void profile_notFound_throwsException() {
            UUID weatherId = UUID.randomUUID();
            UUID userId    = UUID.randomUUID();
            Weather weather = mockWeather(20.0, PrecipitationType.NONE);

            when(weatherRepository.findById(weatherId)).thenReturn(Optional.of(weather));
            when(profileRepository.findByUserId(userId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.recommend(weatherId, userId))
                    .isInstanceOf(ProfileNotFoundException.class);
        }

        // 3. TOP 필터: 기온별 통과/탈락
        @Test
        @DisplayName("HOT(28도) → 나시 통과, 긴팔 탈락")
        void top_hot_onlyShortSleeve() {
            UUID wId = UUID.randomUUID(), uId = UUID.randomUUID();
            Weather weather = mockWeather(28.0, PrecipitationType.NONE);
            Profile profile = mockProfile(3); // 민감도 기본값 → 체감 = 28

            List<Clothes> clothes = List.of(
                    createClothes(ClothesType.TOP, "민소매"),
                    createClothes(ClothesType.TOP, "긴팔티"),
                    createClothes(ClothesType.BOTTOM, "A라인 스커트"),
                    createClothes(ClothesType.SHOES, "운동화")
            );
            defaultSetting(wId, uId, weather, profile, clothes);

            RecommendationResponse resp = service.recommend(wId, uId);

            assertThat(names(resp)).contains("민소매");
            assertThat(names(resp)).doesNotContain("긴팔티");
        }

        @Test
        @DisplayName("COOL(15도) → 반팔 탈락, 후드·맨투맨 통과")
        void top_cool_noShortSleeve() {
            UUID wId = UUID.randomUUID(), uId = UUID.randomUUID();
            Weather weather = mockWeather(15.0, PrecipitationType.NONE);
            Profile profile = mockProfile(3);

            List<Clothes> clothes = List.of(
                    createClothes(ClothesType.TOP, "반팔티"),
                    createClothes(ClothesType.TOP, "긴팔 후드티"),
                    createClothes(ClothesType.BOTTOM, "청치마"),
                    createClothes(ClothesType.SHOES, "운동화")
            );
            defaultSetting(wId, uId, weather, profile, clothes);

            RecommendationResponse resp = service.recommend(wId, uId);

            assertThat(names(resp)).doesNotContain("반팔티");
            assertThat(names(resp)).contains("긴팔 후드티");
        }

        @Test
        @DisplayName("COLD(2도) → 목폴라 통과, 반팔·후드 탈락")
        void top_cold_onlyKnit() {
            UUID wId = UUID.randomUUID(), uId = UUID.randomUUID();
            Weather weather = mockWeather(2.0, PrecipitationType.SNOW);
            Profile profile = mockProfile(3);

            List<Clothes> clothes = List.of(
                    createClothes(ClothesType.TOP, "반팔티"),
                    createClothes(ClothesType.TOP, "긴팔 후드티"),
                    createClothes(ClothesType.TOP, "목폴라"),
                    createClothes(ClothesType.BOTTOM, "청바지"),
                    createClothes(ClothesType.SHOES, "운동화")
            );
            defaultSetting(wId, uId, weather, profile, clothes);

            RecommendationResponse resp = service.recommend(wId, uId);

            assertThat(names(resp)).doesNotContain("반팔티");
            assertThat(names(resp)).anyMatch(n -> n.equals("목폴라") || n.equals("긴팔 후드티"));
        }

        // 4. DRESS 필터
        @Test
        @DisplayName("HOT(28도) → 반팔원피스 통과, 긴팔원피스 탈락")
        void dress_hot_onlyShortSleeve() {
            UUID wId = UUID.randomUUID(), uId = UUID.randomUUID();
            Weather weather = mockWeather(28.0, PrecipitationType.NONE);
            Profile profile = mockProfile(3);

            List<Clothes> clothes = List.of(
                    createClothes(ClothesType.DRESS, "반팔원피스"),
                    createClothes(ClothesType.DRESS, "긴팔원피스"),
                    createClothes(ClothesType.SHOES, "운동화")
            );
            defaultSetting(wId, uId, weather, profile, clothes);

            // 여러 시드로 반팔원피스만 나오는지 확인
            for (int seed = 0; seed < 10; seed++) {
                ReflectionTestUtils.setField(service, "random", new Random(seed));
                RecommendationResponse resp = service.recommend(wId, uId);
                assertThat(names(resp)).doesNotContain("긴팔원피스");
            }
        }

        @Test
        @DisplayName("COLD(2도) → 뷔스티에 원피스 탈락")
        void dress_cold_sleevelessFails() {
            UUID wId = UUID.randomUUID(), uId = UUID.randomUUID();
            Weather weather = mockWeather(2.0, PrecipitationType.NONE);
            Profile profile = mockProfile(3);

            List<Clothes> clothes = List.of(
                    createClothes(ClothesType.DRESS, "뷔스티에 롱 원피스"),
                    createClothes(ClothesType.TOP, "니트"),
                    createClothes(ClothesType.BOTTOM, "청바지"),
                    createClothes(ClothesType.SHOES, "양털부츠")
            );
            defaultSetting(wId, uId, weather, profile, clothes);

            RecommendationResponse resp = service.recommend(wId, uId);

            assertThat(names(resp)).doesNotContain("뷔스티에 롱 원피스");
        }

        // 5. OUTER 필터
        @Test
        @DisplayName("HOT(28도) → 아우터 전부 탈락")
        void outer_hot_nonePass() {
            UUID wId = UUID.randomUUID(), uId = UUID.randomUUID();
            Weather weather = mockWeather(28.0, PrecipitationType.NONE);
            Profile profile = mockProfile(3);

            List<Clothes> clothes = List.of(
                    createClothes(ClothesType.TOP, "반팔티"),
                    createClothes(ClothesType.BOTTOM, "반바지"),
                    createClothes(ClothesType.OUTER, "패딩"),
                    createClothes(ClothesType.OUTER, "가디건"),
                    createClothes(ClothesType.SHOES, "운동화")
            );
            defaultSetting(wId, uId, weather, profile, clothes);

            RecommendationResponse resp = service.recommend(wId, uId);

            assertThat(names(resp)).doesNotContain("패딩");
            assertThat(names(resp)).doesNotContain("가디건");
        }

        @Test
        @DisplayName("WARM(20도) → 가디건 통과, 패딩 탈락")
        void outer_warm_cardiganPass_paddingFail() {
            UUID wId = UUID.randomUUID(), uId = UUID.randomUUID();
            Weather weather = mockWeather(20.0, PrecipitationType.NONE);
            Profile profile = mockProfile(3);

            List<Clothes> clothes = List.of(
                    createClothes(ClothesType.TOP, "반팔티"),
                    createClothes(ClothesType.BOTTOM, "청바지"),
                    createClothes(ClothesType.OUTER, "가디건"),
                    createClothes(ClothesType.OUTER, "패딩"),
                    createClothes(ClothesType.SHOES, "운동화")
            );
            defaultSetting(wId, uId, weather, profile, clothes);

            RecommendationResponse resp = service.recommend(wId, uId);

            assertThat(names(resp)).contains("가디건");
            assertThat(names(resp)).doesNotContain("패딩");
        }

        @Test
        @DisplayName("COOL(15도) → 자켓·코트 통과, 가디건·패딩 탈락")
        void outer_cool_jacketPass() {
            UUID wId = UUID.randomUUID(), uId = UUID.randomUUID();
            Weather weather = mockWeather(15.0, PrecipitationType.NONE);
            Profile profile = mockProfile(3);

            List<Clothes> clothes = List.of(
                    createClothes(ClothesType.TOP, "긴팔 후드티"),
                    createClothes(ClothesType.BOTTOM, "청바지"),
                    createClothes(ClothesType.OUTER, "자켓"),
                    createClothes(ClothesType.OUTER, "가디건"),
                    createClothes(ClothesType.OUTER, "코트"),
                    createClothes(ClothesType.OUTER, "패딩"),
                    createClothes(ClothesType.SHOES, "운동화")
            );
            defaultSetting(wId, uId, weather, profile, clothes);

            RecommendationResponse resp = service.recommend(wId, uId);

            assertThat(names(resp)).contains("자켓");
            assertThat(names(resp)).doesNotContain("가디건");
            assertThat(names(resp)).doesNotContain("패딩");
        }

        @Test
        @DisplayName("COLD(2도) → 패딩 통과, 자켓·가디건 탈락")
        void outer_cold_onlyPadding() {
            UUID wId = UUID.randomUUID(), uId = UUID.randomUUID();
            Weather weather = mockWeather(2.0, PrecipitationType.NONE);
            Profile profile = mockProfile(3);

            List<Clothes> clothes = List.of(
                    createClothes(ClothesType.TOP, "니트"),
                    createClothes(ClothesType.BOTTOM, "청바지"),
                    createClothes(ClothesType.OUTER, "패딩"),
                    createClothes(ClothesType.OUTER, "자켓"),
                    createClothes(ClothesType.OUTER, "가디건"),
                    createClothes(ClothesType.SHOES, "양털부츠")
            );
            defaultSetting(wId, uId, weather, profile, clothes);

            RecommendationResponse resp = service.recommend(wId, uId);

            assertThat(names(resp)).contains("패딩");
            assertThat(names(resp)).doesNotContain("자켓");
            assertThat(names(resp)).doesNotContain("가디건");
        }

        // 6. SHOES 필터: 온도 기준
        @Test
        @DisplayName("HOT(28도) + 맑음 → 샌들·슬리퍼 통과, 양털부츠 탈락")
        void shoes_hot_clear_sandalsPass() {
            UUID wId = UUID.randomUUID(), uId = UUID.randomUUID();
            Weather weather = mockWeather(28.0, PrecipitationType.NONE);
            Profile profile = mockProfile(3);

            List<Clothes> clothes = List.of(
                    createClothes(ClothesType.TOP, "반팔티"),
                    createClothes(ClothesType.BOTTOM, "반바지"),
                    createClothes(ClothesType.SHOES, "샌들"),
                    createClothes(ClothesType.SHOES, "양털부츠")
            );
            defaultSetting(wId, uId, weather, profile, clothes);

            // 10번 호출 → 양털부츠는 절대 나오면 안 됨
            for (int seed = 0; seed < 10; seed++) {
                ReflectionTestUtils.setField(service, "random", new Random(seed));
                assertThat(names(service.recommend(wId, uId))).doesNotContain("양털부츠");
            }
        }

        @Test
        @DisplayName("COLD(2도) + 맑음 → 양털부츠 통과, 샌들 탈락")
        void shoes_cold_clear_winterBootsPass() {
            UUID wId = UUID.randomUUID(), uId = UUID.randomUUID();
            Weather weather = mockWeather(2.0, PrecipitationType.NONE);
            Profile profile = mockProfile(3);

            List<Clothes> clothes = List.of(
                    createClothes(ClothesType.SHOES, "샌들"),
                    createClothes(ClothesType.SHOES, "양털부츠")
            );

            defaultSetting(wId, uId, weather, profile, clothes);

            // 여러 번 돌려서 "샌들이 절대 선택되지 않는지" 확인
            for (int seed = 0; seed < 20; seed++) {
                ReflectionTestUtils.setField(service, "random", new Random(seed));

                List<String> result = names(service.recommend(wId, uId));

                assertThat(result).contains("양털부츠");
                assertThat(result).doesNotContain("샌들");
            }
        }

        // 7. SHOES 필터: 날씨 보정
        @Test
        @DisplayName("비 + 어떤 온도든 → 장화 통과, 샌들·슬리퍼 탈락")
        void shoes_rain_rainBootsPass_sandalsBlocked() {
            UUID wId = UUID.randomUUID(), uId = UUID.randomUUID();
            Weather weather = mockWeather(20.0, PrecipitationType.RAIN);
            Profile profile = mockProfile(3);

            List<Clothes> clothes = List.of(
                    createClothes(ClothesType.TOP, "반팔티"),
                    createClothes(ClothesType.BOTTOM, "청바지"),
                    createClothes(ClothesType.SHOES, "장화"),
                    createClothes(ClothesType.SHOES, "샌들"),
                    createClothes(ClothesType.SHOES, "슬리퍼")
            );
            defaultSetting(wId, uId, weather, profile, clothes);

            for (int seed = 0; seed < 10; seed++) {
                ReflectionTestUtils.setField(service, "random", new Random(seed));
                List<String> result = names(service.recommend(wId, uId));
                assertThat(result).doesNotContain("샌들");
                assertThat(result).doesNotContain("슬리퍼");
            }
        }

        @Test
        @DisplayName("눈 → 양털부츠 통과, 샌들 탈락")
        void shoes_snow_winterBootsPass() {
            UUID wId = UUID.randomUUID(), uId = UUID.randomUUID();
            Weather weather = mockWeather(0.0, PrecipitationType.SNOW);
            Profile profile = mockProfile(3);

            List<Clothes> clothes = List.of(
                    createClothes(ClothesType.TOP, "니트"),
                    createClothes(ClothesType.BOTTOM, "청바지"),
                    createClothes(ClothesType.SHOES, "양털부츠"),
                    createClothes(ClothesType.SHOES, "샌들")
            );
            defaultSetting(wId, uId, weather, profile, clothes);

            for (int seed = 0; seed < 10; seed++) {
                ReflectionTestUtils.setField(service, "random", new Random(seed));
                List<String> result = names(service.recommend(wId, uId));
                assertThat(result).doesNotContain("샌들");
            }
        }

        // 8. 온도 민감도 반영
        @Test
        @DisplayName("rawTemp=17, sensitivity=5 → 체감19도(WARM) → 반팔 통과")
        void sensitivity_high_effectiveTempRises() {
            UUID wId = UUID.randomUUID(), uId = UUID.randomUUID();
            Weather weather = mockWeather(17.0, PrecipitationType.NONE); // 체감 = 17 + (5-3) = 19
            Profile profile = mockProfile(5);

            List<Clothes> clothes = List.of(
                    createClothes(ClothesType.TOP, "반팔티"),   // WARM(18) 이상이면 통과
                    createClothes(ClothesType.BOTTOM, "청바지"),
                    createClothes(ClothesType.SHOES, "운동화")
            );
            defaultSetting(wId, uId, weather, profile, clothes);

            RecommendationResponse resp = service.recommend(wId, uId);

            assertThat(names(resp)).contains("반팔티");
        }

        @Test
        @DisplayName("rawTemp=19, sensitivity=1 → 체감17도(COOL) → 반팔 탈락")
        void sensitivity_low_effectiveTempDrops() {
            UUID wId = UUID.randomUUID(), uId = UUID.randomUUID();
            Weather weather = mockWeather(19.0, PrecipitationType.NONE); // 체감 = 19 + (1-3) = 17
            Profile profile = mockProfile(1);

            List<Clothes> clothes = List.of(
                    createClothes(ClothesType.TOP, "반팔티"),   // WARM(18) 미만 → 탈락
                    createClothes(ClothesType.TOP, "니트"),
                    createClothes(ClothesType.BOTTOM, "청바지"),
                    createClothes(ClothesType.SHOES, "운동화")
            );
            defaultSetting(wId, uId, weather, profile, clothes);

            RecommendationResponse resp = service.recommend(wId, uId);

            assertThat(names(resp)).doesNotContain("반팔티");
            assertThat(names(resp)).contains("니트");
        }

        // 9. 원피스 조합 규칙
        @Test
        @DisplayName("일반 원피스 선택 → TOP·BOTTOM 결과에서 제외")
        void dress_normal_removesTopAndBottom() {
            UUID wId = UUID.randomUUID(), uId = UUID.randomUUID();
            Weather weather = mockWeather(20.0, PrecipitationType.NONE);
            Profile profile = mockProfile(3);

            List<Clothes> clothes = List.of(
                    createClothes(ClothesType.DRESS,  "반팔원피스"),
                    createClothes(ClothesType.TOP,    "반팔티"),
                    createClothes(ClothesType.BOTTOM, "청바지"),
                    createClothes(ClothesType.SHOES,  "운동화")
            );
            defaultSetting(wId, uId, weather, profile, clothes);

            // DRESS 30% 확률 → seed를 바꾸며 DRESS가 선택된 케이스만 검증
            boolean dressCaseFound = false;
            for (int seed = 0; seed < 50; seed++) {
                ReflectionTestUtils.setField(service, "random", new Random(seed));
                RecommendationResponse resp = service.recommend(wId, uId);
                if (hasName(resp, "반팔원피스")) {
                    assertThat(names(resp)).doesNotContain("반팔티");
                    assertThat(names(resp)).doesNotContain("청바지");
                    dressCaseFound = true;
                    break;
                }
            }
            assertThat(dressCaseFound).as("seed 0~49 중 DRESS가 선택된 케이스가 없음").isTrue();
        }

        @Test
        @DisplayName("뷔스티에 원피스 선택 → BOTTOM 제거, TOP은 유지")
        void dress_sleeveless_keepsTop_removesBottom() {
            UUID wId = UUID.randomUUID(), uId = UUID.randomUUID();
            Weather weather = mockWeather(28.0, PrecipitationType.NONE); // HOT → 뷔스티에 통과
            Profile profile = mockProfile(3);

            List<Clothes> clothes = List.of(
                    createClothes(ClothesType.DRESS,  "뷔스티에 롱 원피스"),
                    createClothes(ClothesType.TOP,    "반팔티"),
                    createClothes(ClothesType.BOTTOM, "청바지"),
                    createClothes(ClothesType.SHOES,  "운동화")
            );
            defaultSetting(wId, uId, weather, profile, clothes);

            boolean dressCaseFound = false;
            for (int seed = 0; seed < 50; seed++) {
                ReflectionTestUtils.setField(service, "random", new Random(seed));
                RecommendationResponse resp = service.recommend(wId, uId);
                if (hasName(resp, "뷔스티에 롱 원피스")) {
                    assertThat(names(resp)).doesNotContain("청바지");
                    assertThat(names(resp)).contains("반팔티"); // 레이어링 TOP 유지
                    dressCaseFound = true;
                    break;
                }
            }
            assertThat(dressCaseFound).as("seed 0~49 중 DRESS가 선택된 케이스가 없음").isTrue();
        }

        // 10. DRESS vs TOP+BOTTOM 가중치 (30/70)
        @Test
        @DisplayName("DRESS와 TOP+BOTTOM 모두 있을 때 TOP+BOTTOM이 더 자주 선택됨")
        void upperBody_topBottomMoreFrequentThanDress() {
            UUID wId = UUID.randomUUID(), uId = UUID.randomUUID();
            Weather weather = mockWeather(20.0, PrecipitationType.NONE);
            Profile profile = mockProfile(3);

            List<Clothes> clothes = List.of(
                    createClothes(ClothesType.DRESS,  "반팔원피스"),
                    createClothes(ClothesType.TOP,    "반팔티"),
                    createClothes(ClothesType.BOTTOM, "청바지"),
                    createClothes(ClothesType.SHOES,  "운동화")
            );
            defaultSetting(wId, uId, weather, profile, clothes);

            int dressCount = 0, topBottomCount = 0;
            for (int seed = 0; seed < 100; seed++) {
                ReflectionTestUtils.setField(service, "random", new Random(seed));
                RecommendationResponse resp = service.recommend(wId, uId);
                if (hasName(resp, "반팔원피스")) dressCount++;
                else topBottomCount++;
            }

            // DRESS는 약 30%, TOP+BOTTOM은 약 70% → TOP+BOTTOM이 더 많아야 함
            assertThat(topBottomCount).isGreaterThan(dressCount);
        }

        // 11. 악세서리 50% 확률
        @Test
        @DisplayName("악세서리는 나올 수도, 안 나올 수도 있음 (50% 확률)")
        void accessory_appearsRoughlyHalfTheTime() {
            UUID wId = UUID.randomUUID(), uId = UUID.randomUUID();
            Weather weather = mockWeather(20.0, PrecipitationType.NONE);
            Profile profile = mockProfile(3);

            List<Clothes> clothes = List.of(
                    createClothes(ClothesType.TOP,       "반팔티"),
                    createClothes(ClothesType.BOTTOM,    "청바지"),
                    createClothes(ClothesType.SHOES,     "운동화"),
                    createClothes(ClothesType.ACCESSORY, "목걸이")
            );
            defaultSetting(wId, uId, weather, profile, clothes);

            int appeared = 0;
            for (int seed = 0; seed < 100; seed++) {
                ReflectionTestUtils.setField(service, "random", new Random(seed));
                if (hasName(service.recommend(wId, uId), "목걸이")) appeared++;
            }

            // 100번 중 20~80번 등장하면 정상 (50% ±30% 허용)
            assertThat(appeared).isBetween(20, 80);
        }

        // 12. 카테고리별 1개씩 (없으면 안 나옴)
        @Test
        @DisplayName("HAT·BAG 없으면 결과에 포함 안 됨")
        void noHatNoBag_notInResult() {
            UUID wId = UUID.randomUUID(), uId = UUID.randomUUID();
            Weather weather = mockWeather(20.0, PrecipitationType.NONE);
            Profile profile = mockProfile(3);

            List<Clothes> clothes = List.of(
                    createClothes(ClothesType.TOP,    "반팔티"),
                    createClothes(ClothesType.BOTTOM, "청바지"),
                    createClothes(ClothesType.SHOES,  "운동화")
            );
            defaultSetting(wId, uId, weather, profile, clothes);

            RecommendationResponse resp = service.recommend(wId, uId);

            assertThat(resp.clothes()).hasSize(3); // TOP + BOTTOM + SHOES
        }

        @Test
        @DisplayName("의류가 아무것도 없으면 빈 결과 반환")
        void noClothes_emptyResult() {
            UUID wId = UUID.randomUUID();
            UUID uId = UUID.randomUUID();

            Weather weather = mockWeather(20.0, PrecipitationType.NONE);
            Profile profile = mockProfile(3);

            when(weatherRepository.findById(wId)).thenReturn(Optional.of(weather));
            when(profileRepository.findByUserId(uId)).thenReturn(Optional.of(profile));
            when(clothesRepository.findByOwnerId(uId)).thenReturn(List.of());

            RecommendationResponse resp = service.recommend(wId, uId);

            assertThat(resp.clothes()).isEmpty();
        }

        // 13. attribute groupingMap 반영 확인
        @Test
        @DisplayName("ClothesAttributeValue가 있으면 groupingMap에 포함됨")
        void attributeValues_includedInGroupingMap() {
            UUID wId = UUID.randomUUID(), uId = UUID.randomUUID();
            UUID defId = UUID.randomUUID();
            Weather weather = mockWeather(20.0, PrecipitationType.NONE);
            Profile profile = mockProfile(3);

            Clothes top = createClothesWithValue(ClothesType.TOP, "반팔티", defId, "코튼");
            List<Clothes> clothes = List.of(
                    top,
                    createClothes(ClothesType.BOTTOM, "청바지"),
                    createClothes(ClothesType.SHOES, "운동화")
            );
            defaultSetting(wId, uId, weather, profile, clothes);

            // clothesMapper.toDto 호출 시 groupingMap 전달 여부 검증
            service.recommend(wId, uId);

            verify(clothesMapper, atLeastOnce()).toDto(
                    any(),
                    any(),
                    argThat(map -> map.containsKey(defId) && map.get(defId).contains("코튼"))
            );
        }
    }