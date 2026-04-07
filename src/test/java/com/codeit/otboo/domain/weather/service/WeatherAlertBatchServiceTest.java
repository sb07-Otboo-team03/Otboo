package com.codeit.otboo.domain.weather.service;

import com.codeit.otboo.domain.notification.entity.Notification;
import com.codeit.otboo.domain.profile.entity.Location;
import com.codeit.otboo.domain.profile.entity.Profile;
import com.codeit.otboo.domain.profile.repository.ProfileRepository;
import com.codeit.otboo.domain.sse.event.WeatherSseEvent;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.weather.batch.dto.RegionAlertResult;
import com.codeit.otboo.domain.weather.batch.dto.RegionAlertTarget;
import com.codeit.otboo.domain.weather.dto.alert.PrecipitationChangeSummary;
import com.codeit.otboo.domain.weather.dto.alert.TemperatureGapSummary;
import com.codeit.otboo.domain.weather.entity.PrecipitationType;
import com.codeit.otboo.domain.weather.entity.Weather;
import com.codeit.otboo.domain.weather.entity.YesterdayHourlyWeather;
import com.codeit.otboo.domain.weather.repository.WeatherRepository;
import com.codeit.otboo.domain.weather.repository.YesterdayHourlyWeatherRepository;
import com.codeit.otboo.global.util.TimeProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static com.codeit.otboo.domain.weather.service.WeatherAlertPolicyService.END_TIME;
import static com.codeit.otboo.domain.weather.service.WeatherAlertPolicyService.START_TIME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeatherAlertBatchServiceTest {

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private WeatherRepository weatherRepository;

    @Mock
    private YesterdayHourlyWeatherRepository yesterdayRepository;

    @Mock
    private WeatherAlertPolicyService policyService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private TimeProvider timeProvider;

    @InjectMocks
    private WeatherAlertBatchService weatherAlertBatchService;

    @Nested
    @DisplayName("findAlertTargetsByRegion")
    class FindAlertTargetsByRegionTest {

        @Test
        @DisplayName("알림 대상 프로필이 없으면 빈 리스트를 반환한다")
        void findAlertTargetsByRegion_whenNoProfiles_returnsEmptyList() {
            // given
            when(profileRepository.findAllForWeatherAlert()).thenReturn(List.of());

            // when
            List<RegionAlertTarget> result = weatherAlertBatchService.findAlertTargetsByRegion();

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("같은 지역의 여러 프로필은 하나의 RegionAlertTarget으로 그룹핑한다")
        void findAlertTargetsByRegion_groupsProfilesByRegion() {
            // given
            Profile profile1 = createProfile(UUID.randomUUID(), 60, 127);
            Profile profile2 = createProfile(UUID.randomUUID(), 60, 127);
            Profile profile3 = createProfile(UUID.randomUUID(), 55, 124);

            when(profileRepository.findAllForWeatherAlert())
                    .thenReturn(List.of(profile1, profile2, profile3));

            // when
            List<RegionAlertTarget> result = weatherAlertBatchService.findAlertTargetsByRegion();

            // then
            assertThat(result).hasSize(2);

            RegionAlertTarget seoulTarget = result.stream()
                    .filter(target -> target.x().equals(60) && target.y().equals(127))
                    .findFirst()
                    .orElseThrow();

            RegionAlertTarget otherTarget = result.stream()
                    .filter(target -> target.x().equals(55) && target.y().equals(124))
                    .findFirst()
                    .orElseThrow();

            assertThat(seoulTarget.profiles()).hasSize(2);
            assertThat(otherTarget.profiles()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("buildRegionAlertResult")
    class BuildRegionAlertResultTest {

        @Test
        @DisplayName("오늘 날씨 데이터가 없으면 빈 결과를 반환하고 나머지 로직을 수행하지 않는다")
        void buildRegionAlertResult_whenTodayWeathersEmpty_returnsEmptyResult() {
            // given
            LocalDate today = LocalDate.of(2026, 3, 31);
            when(timeProvider.nowDate()).thenReturn(today);

            Profile profile = createProfile(UUID.randomUUID(), 50, 120);
            RegionAlertTarget target = new RegionAlertTarget(50, 120, List.of(profile));

            when(weatherRepository.findWeatherForAlertByRegion(any(), any(), any(), any(), any()))
                    .thenReturn(List.of());

            // when
            RegionAlertResult result = weatherAlertBatchService.buildRegionAlertResult(target);

            // then
            assertThat(result.isEmpty()).isTrue();
            assertThat(result.notifications()).isEmpty();

            verify(yesterdayRepository, never()).findYesterdayWeatherForAlertByRegion(any(), any(), any(), any(), any());
            verify(policyService, never()).summarize(anyList(), anyList());
            verify(policyService, never()).summarizePrecipitationChanges(anyList());
        }

        @Test
        @DisplayName("정책 기준 시간 START_TIME ~ END_TIME으로 오늘과 어제 데이터를 조회한다")
        void buildRegionAlertResult_usesPolicyTimeRange() {
            // given
            LocalDate today = LocalDate.of(2026, 3, 31);
            when(timeProvider.nowDate()).thenReturn(today);

            Profile profile = createProfile(UUID.randomUUID(), 50, 120);
            RegionAlertTarget target = new RegionAlertTarget(50, 120, List.of(profile));

            Weather weather = mock(Weather.class);
            when(weather.getForecastAt()).thenReturn(LocalDateTime.of(2026, 3, 31, 6, 0));
            when(weather.getTemperatureCurrent()).thenReturn(10.0);
            when(weather.getPrecipitationType()).thenReturn(PrecipitationType.NONE);

            when(weatherRepository.findWeatherForAlertByRegion(any(), any(), any(), any(), any()))
                    .thenReturn(List.of(weather));
            when(yesterdayRepository.findYesterdayWeatherForAlertByRegion(any(), any(), any(), any(), any()))
                    .thenReturn(List.of());

            when(policyService.summarize(anyList(), anyList()))
                    .thenReturn(new TemperatureGapSummary(0, 0, 0, null, 0, false, null));
            when(policyService.summarizePrecipitationChanges(anyList()))
                    .thenReturn(PrecipitationChangeSummary.empty());

            // when
            weatherAlertBatchService.buildRegionAlertResult(target);

            // then
            verify(weatherRepository).findWeatherForAlertByRegion(
                    eq(today.atStartOfDay()),
                    eq(50),
                    eq(120),
                    eq(today.atTime(START_TIME)),
                    eq(today.atTime(END_TIME))
            );

            verify(yesterdayRepository).findYesterdayWeatherForAlertByRegion(
                    eq(today.minusDays(1)),
                    eq(50),
                    eq(120),
                    eq(START_TIME),
                    eq(END_TIME)
            );
        }

        @Test
        @DisplayName("같은 지역의 여러 프로필에 대해 기온 알림 Notification 목록을 생성한다")
        void buildRegionAlertResult_returnsTemperatureNotifications() {
            // given
            LocalDate today = LocalDate.of(2026, 3, 31);
            when(timeProvider.nowDate()).thenReturn(today);

            Profile profile1 = createProfile(UUID.randomUUID(), 60, 127);
            Profile profile2 = createProfile(UUID.randomUUID(), 60, 127);
            RegionAlertTarget target = new RegionAlertTarget(60, 127, List.of(profile1, profile2));

            Weather weather = mock(Weather.class);
            when(weather.getForecastAt()).thenReturn(LocalDateTime.of(2026, 3, 31, 6, 0));
            when(weather.getTemperatureCurrent()).thenReturn(10.0);
            when(weather.getPrecipitationType()).thenReturn(PrecipitationType.NONE);

            YesterdayHourlyWeather yesterdayWeather = mock(YesterdayHourlyWeather.class);
            when(yesterdayWeather.getHour()).thenReturn(LocalTime.of(6, 0));
            when(yesterdayWeather.getTemperature()).thenReturn(15.0);

            when(weatherRepository.findWeatherForAlertByRegion(any(), eq(60), eq(127), any(), any()))
                    .thenReturn(List.of(weather));
            when(yesterdayRepository.findYesterdayWeatherForAlertByRegion(any(), eq(60), eq(127), any(), any()))
                    .thenReturn(List.of(yesterdayWeather));

            when(policyService.summarize(anyList(), anyList()))
                    .thenReturn(new TemperatureGapSummary(
                            -3.5, 6, -6, LocalTime.of(8, 0), -5.0, true, "오늘은 어제보다 전반적으로 3도 낮아요."
                    ));
            when(policyService.summarizePrecipitationChanges(anyList()))
                    .thenReturn(PrecipitationChangeSummary.empty());

            // when
            RegionAlertResult result = weatherAlertBatchService.buildRegionAlertResult(target);

            // then
            assertThat(result.isEmpty()).isFalse();
            assertThat(result.notifications()).hasSize(2);
            assertThat(result.notifications())
                    .extracting(Notification::getTitle)
                    .containsOnly("어제와 기온 차가 커요");
            assertThat(result.notifications())
                    .extracting(Notification::getContent)
                    .containsOnly("오늘은 어제보다 전반적으로 3도 낮아요.");

            verify(policyService, times(1)).summarize(anyList(), anyList());
        }

        @Test
        @DisplayName("기온 알림이 없고 강수 변화 알림만 있으면 강수 알림 Notification 목록을 생성한다")
        void buildRegionAlertResult_returnsPrecipitationNotifications_whenOnlyPrecipitationChanges() {
            // given
            LocalDate today = LocalDate.of(2026, 3, 31);
            when(timeProvider.nowDate()).thenReturn(today);

            Profile profile1 = createProfile(UUID.randomUUID(), 60, 127);
            Profile profile2 = createProfile(UUID.randomUUID(), 60, 127);
            RegionAlertTarget target = new RegionAlertTarget(60, 127, List.of(profile1, profile2));

            Weather weather = mock(Weather.class);
            when(weather.getForecastAt()).thenReturn(LocalDateTime.of(2026, 3, 31, 6, 0));
            when(weather.getTemperatureCurrent()).thenReturn(10.0);
            when(weather.getPrecipitationType()).thenReturn(PrecipitationType.NONE);

            YesterdayHourlyWeather yesterdayWeather = mock(YesterdayHourlyWeather.class);
            when(yesterdayWeather.getHour()).thenReturn(LocalTime.of(6, 0));
            when(yesterdayWeather.getTemperature()).thenReturn(15.0);

            when(weatherRepository.findWeatherForAlertByRegion(any(), eq(60), eq(127), any(), any()))
                    .thenReturn(List.of(weather));
            when(yesterdayRepository.findYesterdayWeatherForAlertByRegion(any(), eq(60), eq(127), any(), any()))
                    .thenReturn(List.of(yesterdayWeather));

            when(policyService.summarize(anyList(), anyList()))
                    .thenReturn(new TemperatureGapSummary(0, 0, 0, null, 0, false, null));
            when(policyService.summarizePrecipitationChanges(anyList()))
                    .thenReturn(new PrecipitationChangeSummary(
                            LocalTime.of(8, 0),
                            PrecipitationType.RAIN,
                            null,
                            null,
                            true,
                            "오늘 오전 8시부터 비가 올 예정이에요."
                    ));

            // when
            RegionAlertResult result = weatherAlertBatchService.buildRegionAlertResult(target);

            // then
            assertThat(result.isEmpty()).isFalse();
            assertThat(result.notifications()).hasSize(2);
            assertThat(result.notifications())
                    .extracting(Notification::getTitle)
                    .containsOnly("오늘 강수 예보가 바뀌어요");
            assertThat(result.notifications())
                    .extracting(Notification::getContent)
                    .containsOnly("오늘 오전 8시부터 비가 올 예정이에요.");
        }

        @Test
        @DisplayName("기온 알림과 강수 알림이 모두 없으면 빈 결과를 반환한다")
        void buildRegionAlertResult_whenNoAlerts_returnsEmptyResult() {
            // given
            LocalDate today = LocalDate.of(2026, 3, 31);
            when(timeProvider.nowDate()).thenReturn(today);

            Profile profile = createProfile(UUID.randomUUID(), 55, 124);
            RegionAlertTarget target = new RegionAlertTarget(55, 124, List.of(profile));

            Weather weather = mock(Weather.class);
            when(weather.getForecastAt()).thenReturn(LocalDateTime.of(2026, 3, 31, 6, 0));
            when(weather.getTemperatureCurrent()).thenReturn(10.0);
            when(weather.getPrecipitationType()).thenReturn(PrecipitationType.NONE);

            YesterdayHourlyWeather yesterdayWeather = mock(YesterdayHourlyWeather.class);
            when(yesterdayWeather.getHour()).thenReturn(LocalTime.of(6, 0));
            when(yesterdayWeather.getTemperature()).thenReturn(15.0);

            when(weatherRepository.findWeatherForAlertByRegion(any(), eq(55), eq(124), any(), any()))
                    .thenReturn(List.of(weather));
            when(yesterdayRepository.findYesterdayWeatherForAlertByRegion(any(), eq(55), eq(124), any(), any()))
                    .thenReturn(List.of(yesterdayWeather));

            when(policyService.summarize(anyList(), anyList()))
                    .thenReturn(new TemperatureGapSummary(-1.5, 3, -3, LocalTime.of(9, 0), -2.0, false, null));
            when(policyService.summarizePrecipitationChanges(anyList()))
                    .thenReturn(PrecipitationChangeSummary.empty());

            // when
            RegionAlertResult result = weatherAlertBatchService.buildRegionAlertResult(target);

            // then
            assertThat(result.isEmpty()).isTrue();
            assertThat(result.notifications()).isEmpty();
        }

        @Test
        @DisplayName("기온 알림과 강수 알림이 모두 있으면 두 종류의 Notification을 모두 생성한다")
        void buildRegionAlertResult_returnsBothNotifications() {
            // given
            LocalDate today = LocalDate.of(2026, 3, 31);
            when(timeProvider.nowDate()).thenReturn(today);

            Profile profile1 = createProfile(UUID.randomUUID(), 60, 127);
            Profile profile2 = createProfile(UUID.randomUUID(), 60, 127);
            RegionAlertTarget target = new RegionAlertTarget(60, 127, List.of(profile1, profile2));

            Weather weather = mock(Weather.class);
            when(weather.getForecastAt()).thenReturn(LocalDateTime.of(2026, 3, 31, 6, 0));
            when(weather.getTemperatureCurrent()).thenReturn(10.0);
            when(weather.getPrecipitationType()).thenReturn(PrecipitationType.NONE);

            YesterdayHourlyWeather yesterdayWeather = mock(YesterdayHourlyWeather.class);
            when(yesterdayWeather.getHour()).thenReturn(LocalTime.of(6, 0));
            when(yesterdayWeather.getTemperature()).thenReturn(15.0);

            when(weatherRepository.findWeatherForAlertByRegion(any(), eq(60), eq(127), any(), any()))
                    .thenReturn(List.of(weather));
            when(yesterdayRepository.findYesterdayWeatherForAlertByRegion(any(), eq(60), eq(127), any(), any()))
                    .thenReturn(List.of(yesterdayWeather));

            when(policyService.summarize(anyList(), anyList()))
                    .thenReturn(new TemperatureGapSummary(
                            -3.5, 6, -6, LocalTime.of(8, 0), -5.0, true, "오늘은 어제보다 전반적으로 3도 낮아요."
                    ));
            when(policyService.summarizePrecipitationChanges(anyList()))
                    .thenReturn(new PrecipitationChangeSummary(
                            LocalTime.of(8, 0),
                            PrecipitationType.RAIN,
                            null,
                            null,
                            true,
                            "오늘 오전 8시부터 비가 올 예정이에요."
                    ));

            // when
            RegionAlertResult result = weatherAlertBatchService.buildRegionAlertResult(target);

            // then
            assertThat(result.notifications()).hasSize(4);
            assertThat(result.notifications())
                    .extracting(Notification::getTitle)
                    .containsExactlyInAnyOrder(
                            "어제와 기온 차가 커요",
                            "어제와 기온 차가 커요",
                            "오늘 강수 예보가 바뀌어요",
                            "오늘 강수 예보가 바뀌어요"
                    );
        }
    }

    @Nested
    @DisplayName("saveAndPublish")
    class SaveAndPublishTest {

        @Test
        @DisplayName("빈 결과면 이벤트를 발행하지 않는다")
        void saveAndPublish_whenResultIsEmpty_doNothing() {
            // given
            RegionAlertResult result = new RegionAlertResult(60, 127, List.of());

            // when
            weatherAlertBatchService.publishWeatherEvent(result);

            // then
            verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("알림 결과가 있으면 WeatherSseEvent를 한 번 발행한다")
        void saveAndPublish_savesNotificationsAndPublishesEvents() {
            // given
            Profile profile1 = createProfile(UUID.randomUUID(), 60, 127);
            Profile profile2 = createProfile(UUID.randomUUID(), 60, 127);

            Notification notification1 = new Notification(
                    "어제와 기온 차가 커요",
                    "오늘은 어제보다 전반적으로 3도 낮아요.",
                    com.codeit.otboo.domain.notification.dto.NotificationLevel.INFO,
                    profile1.getUser()
            );
            Notification notification2 = new Notification(
                    "어제와 기온 차가 커요",
                    "오늘은 어제보다 전반적으로 3도 낮아요.",
                    com.codeit.otboo.domain.notification.dto.NotificationLevel.INFO,
                    profile2.getUser()
            );

            RegionAlertResult result = new RegionAlertResult(60, 127, List.of(notification1, notification2));

            ArgumentCaptor<WeatherSseEvent> eventCaptor = ArgumentCaptor.forClass(WeatherSseEvent.class);

            // when
            weatherAlertBatchService.publishWeatherEvent(result);

            // then
            verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());

            WeatherSseEvent publishedEvent = eventCaptor.getValue();
            assertThat(publishedEvent.getNotificationList()).hasSize(2);
            assertThat(publishedEvent.getNotificationList())
                    .extracting(Notification::getTitle)
                    .containsOnly("어제와 기온 차가 커요");
            assertThat(publishedEvent.getNotificationList())
                    .extracting(Notification::getContent)
                    .containsOnly("오늘은 어제보다 전반적으로 3도 낮아요.");
        }
    }

    private Profile createProfile(UUID userId, int x, int y) {
        User user = new User(
                "test@naver.com",
                "test123"
        );
        ReflectionTestUtils.setField(user, "id", userId);

        Profile profile = Profile.builder()
                .user(user)
                .name("테스트유저")
                .build();

        List<String> locationNames = List.of("서울특별시", "강남구", "역삼동");

        Location location = Location.builder()
                .x(x)
                .y(y)
                .latitude(37.5)
                .longitude(126.9)
                .region1depthName(locationNames.get(0))
                .region2depthName(locationNames.get(1))
                .region3depthName(locationNames.get(2))
                .region4depthName("")
                .build();

        ReflectionTestUtils.setField(profile, "location", location);
        return profile;
    }
}