package com.codeit.otboo.domain.weather.service;

import com.codeit.otboo.domain.notification.entity.Notification;
import com.codeit.otboo.domain.notification.repository.NotificationRepository;
import com.codeit.otboo.domain.profile.entity.Location;
import com.codeit.otboo.domain.profile.entity.Profile;
import com.codeit.otboo.domain.profile.repository.ProfileRepository;
import com.codeit.otboo.domain.sse.event.SseEvent;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.weather.dto.alert.TemperatureGapSummary;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeatherAlertServiceTest {

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private WeatherRepository weatherRepository;

    @Mock
    private YesterdayHourlyWeatherRepository yesterdayRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private WeatherAlertPolicyService policyService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private TimeProvider timeProvider;

    @InjectMocks
    private WeatherAlertService weatherAlertService;

    @Nested
    @DisplayName("sendDailyTemperatureGapAlerts")
    class SendDailyTemperatureGapAlertsTest {

        @Test
        @DisplayName("알림 대상 프로필이 없으면 아무 작업도 하지 않는다")
        void sendDailyTemperatureGapAlerts_whenNoProfiles_doNothing() {
            // given
            LocalDate today = LocalDate.of(2026, 3, 31);
            when(timeProvider.nowDate()).thenReturn(today);

            when(profileRepository.findAllForWeatherAlert()).thenReturn(List.of());

            // when
            weatherAlertService.sendDailyTemperatureGapAlerts();

            // then
            verify(weatherRepository, never()).findWeatherForAlertByRegion(any(), any(), any(), any(), any());
            verify(yesterdayRepository, never()).findYesterdayWeatherForAlertByRegion(any(), any(), any(), any(), any());
            verify(notificationRepository, never()).saveAll(any());
            verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("같은 지역의 여러 프로필은 지역별로 한 번만 계산하고 각 사용자에게 알림을 저장한다")
        void sendDailyTemperatureGapAlerts_groupsProfilesByRegionAndSavesNotifications() {
            // given
            LocalDate today = LocalDate.of(2026, 3, 31);
            when(timeProvider.nowDate()).thenReturn(today);
            when(timeProvider.nowDateTime()).thenReturn(LocalDateTime.of(2026, 3, 31, 6, 0));

            Profile profile1 = createProfile(UUID.randomUUID(), 60, 127);
            Profile profile2 = createProfile(UUID.randomUUID(), 60, 127);

            when(profileRepository.findAllForWeatherAlert()).thenReturn(List.of(profile1, profile2));

            Weather weather = mock(Weather.class);
            when(weather.getForecastAt()).thenReturn(LocalDateTime.of(2026, 3, 31, 6, 0));
            when(weather.getTemperatureCurrent()).thenReturn(10.0);

            YesterdayHourlyWeather yesterdayWeather = mock(YesterdayHourlyWeather.class);
            when(yesterdayWeather.getHour()).thenReturn(LocalTime.of(6, 0));
            when(yesterdayWeather.getTemperature()).thenReturn(15.0);

            when(weatherRepository.findWeatherForAlertByRegion(any(), eq(60), eq(127), any(), any()))
                    .thenReturn(List.of(weather));
            when(yesterdayRepository.findYesterdayWeatherForAlertByRegion(any(), eq(60), eq(127), any(), any()))
                    .thenReturn(List.of(yesterdayWeather));

            TemperatureGapSummary summary = new TemperatureGapSummary(
                    -3.5, 6, -6, LocalTime.of(8, 0), -5.0, true, "오늘은 어제보다 전반적으로 3도 낮아요."
            );
            when(policyService.summarize(anyList(), anyList())).thenReturn(summary);

            when(notificationRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

            // when
            weatherAlertService.sendDailyTemperatureGapAlerts();

            // then
            verify(policyService, times(1)).summarize(anyList(), anyList());

            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<Notification>> notificationCaptor = ArgumentCaptor.forClass(List.class);
            verify(notificationRepository).saveAll(notificationCaptor.capture());

            List<Notification> savedNotifications = notificationCaptor.getValue();
            assertThat(savedNotifications).hasSize(2);
            assertThat(savedNotifications)
                    .extracting(Notification::getContent)
                    .containsOnly("오늘은 어제보다 전반적으로 3도 낮아요.");

            verify(eventPublisher, times(2)).publishEvent(any(SseEvent.class));
        }

        @Test
        @DisplayName("정책상 알림 대상이 아니면 Notification 저장과 SSE 발행을 하지 않는다")
        void sendDailyTemperatureGapAlerts_whenShouldNotifyFalse_doesNotSaveNotification() {
            // given
            LocalDate today = LocalDate.of(2026, 3, 31);
            when(timeProvider.nowDate()).thenReturn(today);

            Profile profile = createProfile(UUID.randomUUID(), 55, 124);

            when(profileRepository.findAllForWeatherAlert()).thenReturn(List.of(profile));

            Weather weather = mock(Weather.class);
            when(weather.getForecastAt()).thenReturn(LocalDateTime.of(2026, 3, 31, 6, 0));
            when(weather.getTemperatureCurrent()).thenReturn(10.0);

            YesterdayHourlyWeather yesterdayWeather = mock(YesterdayHourlyWeather.class);
            when(yesterdayWeather.getHour()).thenReturn(LocalTime.of(6, 0));
            when(yesterdayWeather.getTemperature()).thenReturn(15.0);

            when(weatherRepository.findWeatherForAlertByRegion(any(), eq(55), eq(124), any(), any()))
                    .thenReturn(List.of(weather));
            when(yesterdayRepository.findYesterdayWeatherForAlertByRegion(any(), eq(55), eq(124), any(), any()))
                    .thenReturn(List.of(yesterdayWeather));

            TemperatureGapSummary summary = new TemperatureGapSummary(
                    -1.5, 3, -3, LocalTime.of(9, 0), -2.0, false, null
            );
            when(policyService.summarize(anyList(), anyList())).thenReturn(summary);

            // when
            weatherAlertService.sendDailyTemperatureGapAlerts();

            // then
            verify(notificationRepository, never()).saveAll(any());
            verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("정책 기준 시간 START_TIME ~ END_TIME으로 오늘과 어제 데이터를 조회한다")
        void sendDailyTemperatureGapAlerts_usesPolicyTimeRange() {
            // given
            LocalDate today = LocalDate.of(2026, 3, 31);
            when(timeProvider.nowDate()).thenReturn(today);

            Profile profile = createProfile(UUID.randomUUID(), 50, 120);

            when(profileRepository.findAllForWeatherAlert()).thenReturn(List.of(profile));
            when(weatherRepository.findWeatherForAlertByRegion(any(), any(), any(), any(), any()))
                    .thenReturn(List.of());
            when(yesterdayRepository.findYesterdayWeatherForAlertByRegion(any(), any(), any(), any(), any()))
                    .thenReturn(List.of());
            when(policyService.summarize(anyList(), anyList()))
                    .thenReturn(new TemperatureGapSummary(0, 0, 0, null, 0, false, null));

            // when
            weatherAlertService.sendDailyTemperatureGapAlerts();

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

        Location location = Location.builder()
                .x(x)
                .y(y)
                .latitude(37.5)
                .longitude(126.9)
                .locationNames("테스트 지역")
                .build();

        ReflectionTestUtils.setField(profile, "location", location);
        return profile;
    }
}