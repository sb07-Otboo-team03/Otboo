package com.codeit.otboo.domain.weather.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codeit.otboo.domain.notification.dto.NotificationDto;
import com.codeit.otboo.domain.notification.entity.Notification;
import com.codeit.otboo.domain.notification.repository.NotificationRepository;
import com.codeit.otboo.domain.profile.entity.Location;
import com.codeit.otboo.domain.profile.entity.Profile;
import com.codeit.otboo.domain.profile.repository.ProfileRepository;
import com.codeit.otboo.domain.sse.event.SseEvent;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.weather.dto.alert.PrecipitationChangeSummary;
import com.codeit.otboo.domain.weather.dto.alert.TemperatureGapSummary;
import com.codeit.otboo.domain.weather.entity.PrecipitationType;
import com.codeit.otboo.domain.weather.entity.Weather;
import com.codeit.otboo.domain.weather.entity.YesterdayHourlyWeather;
import com.codeit.otboo.domain.weather.repository.WeatherRepository;
import com.codeit.otboo.domain.weather.repository.YesterdayHourlyWeatherRepository;
import com.codeit.otboo.global.util.TimeProvider;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
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

@ExtendWith(MockitoExtension.class)
class WeatherAlertServiceTest {

    @Mock private ProfileRepository profileRepository;
    @Mock private WeatherRepository weatherRepository;
    @Mock private YesterdayHourlyWeatherRepository yesterdayRepository;
    @Mock private NotificationRepository notificationRepository;
    @Mock private WeatherAlertPolicyService policyService;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private TimeProvider timeProvider;

    @InjectMocks
    private WeatherAlertService weatherAlertService;

    @Nested
    @DisplayName("sendDailyWeatherAlerts")
    class SendDailyWeatherAlertsTest {

        @Test
        @DisplayName("알림 대상 프로필이 없으면 아무 작업도 하지 않는다")
        void noProfiles() {
            LocalDate today = LocalDate.of(2026, 3, 31);
            when(timeProvider.nowDate()).thenReturn(today);

            when(profileRepository.findAllForWeatherAlert()).thenReturn(List.of());

            weatherAlertService.sendDailyWeatherAlerts();

            verify(notificationRepository, never()).saveAll(any());
            verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("같은 지역 여러 프로필 → 알림 생성 + SSE 1번 발행 + DTO 2개")
        void groupedProfiles_publishOnce_withBatchDtos() {
            // given
            LocalDate today = LocalDate.of(2026, 3, 31);
            LocalDateTime now = LocalDateTime.of(2026, 3, 31, 6, 0);

            when(timeProvider.nowDate()).thenReturn(today);
            when(timeProvider.nowDateTime()).thenReturn(now);

            Profile p1 = createProfile(UUID.randomUUID(), 60, 127);
            Profile p2 = createProfile(UUID.randomUUID(), 60, 127);

            when(profileRepository.findAllForWeatherAlert()).thenReturn(List.of(p1, p2));

            Weather weather = mock(Weather.class);
            when(weather.getForecastAt()).thenReturn(now);
            when(weather.getTemperatureCurrent()).thenReturn(10.0);
            when(weather.getPrecipitationType()).thenReturn(PrecipitationType.NONE);

            YesterdayHourlyWeather yesterday = mock(YesterdayHourlyWeather.class);
            when(yesterday.getHour()).thenReturn(LocalTime.of(6, 0));
            when(yesterday.getTemperature()).thenReturn(15.0);

            when(weatherRepository.findWeatherForAlertByRegion(any(), eq(60), eq(127), any(), any()))
                .thenReturn(List.of(weather));

            when(yesterdayRepository.findYesterdayWeatherForAlertByRegion(any(), eq(60), eq(127), any(), any()))
                .thenReturn(List.of(yesterday));

            when(policyService.summarize(anyList(), anyList()))
                .thenReturn(new TemperatureGapSummary(
                    -3.5, 6, -6, LocalTime.of(8, 0), -5.0, true,
                    "오늘은 어제보다 전반적으로 3도 낮아요."
                ));

            when(policyService.summarizePrecipitationChanges(anyList()))
                .thenReturn(PrecipitationChangeSummary.empty());

            when(notificationRepository.saveAll(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

            // when
            weatherAlertService.sendDailyWeatherAlerts();

            // then
            // 1. Notification 저장 검증
            ArgumentCaptor<List<Notification>> notifCaptor =
                ArgumentCaptor.forClass(List.class);

            verify(notificationRepository).saveAll(notifCaptor.capture());

            List<Notification> saved = notifCaptor.getValue();
            assertThat(saved).hasSize(2);

            // 2. 🔥 SSE는 1번만 발행
            ArgumentCaptor<SseEvent> eventCaptor =
                ArgumentCaptor.forClass(SseEvent.class);

            verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());

            SseEvent event = eventCaptor.getValue();
            List<NotificationDto> dtos = event.notificationDtoList();

            // 3. 🔥 DTO 2개 들어있는지 검증
            assertThat(dtos).hasSize(2);

            assertThat(dtos)
                .extracting(NotificationDto::title)
                .containsOnly("어제와 기온 차가 커요");

            assertThat(dtos)
                .extracting(NotificationDto::content)
                .containsOnly("오늘은 어제보다 전반적으로 3도 낮아요.");
        }

        @Test
        @DisplayName("알림 없으면 저장/발행 안함")
        void noAlerts() {
            LocalDate today = LocalDate.of(2026, 3, 31);
            when(timeProvider.nowDate()).thenReturn(today);

            Profile profile = createProfile(UUID.randomUUID(), 50, 120);
            when(profileRepository.findAllForWeatherAlert()).thenReturn(List.of(profile));

            when(weatherRepository.findWeatherForAlertByRegion(any(), any(), any(), any(), any()))
                .thenReturn(List.of());

            weatherAlertService.sendDailyWeatherAlerts();

            verify(notificationRepository, never()).saveAll(any());
            verify(eventPublisher, never()).publishEvent(any());
        }
    }

    // ------------------------
    // helper
    // ------------------------

    private Profile createProfile(UUID userId, int x, int y) {
        User user = new User("test@naver.com", "test123");
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