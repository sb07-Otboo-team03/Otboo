package com.codeit.otboo.domain.weather.service;

import com.codeit.otboo.domain.notification.dto.NotificationDto;
import com.codeit.otboo.domain.notification.dto.NotificationLevel;
import com.codeit.otboo.domain.notification.entity.Notification;
import com.codeit.otboo.domain.notification.repository.NotificationRepository;
import com.codeit.otboo.domain.profile.entity.Profile;
import com.codeit.otboo.domain.profile.repository.ProfileRepository;
import com.codeit.otboo.domain.sse.event.SseEvent;
import com.codeit.otboo.domain.weather.dto.alert.HourlyPrecipitationStatus;
import com.codeit.otboo.domain.weather.dto.alert.HourlyTemperature;
import com.codeit.otboo.domain.weather.dto.alert.PrecipitationChangeSummary;
import com.codeit.otboo.domain.weather.dto.alert.TemperatureGapSummary;
import com.codeit.otboo.domain.weather.entity.Weather;
import com.codeit.otboo.domain.weather.entity.YesterdayHourlyWeather;
import com.codeit.otboo.domain.weather.repository.WeatherRepository;
import com.codeit.otboo.domain.weather.repository.YesterdayHourlyWeatherRepository;
import com.codeit.otboo.global.util.TimeProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.codeit.otboo.domain.weather.service.WeatherAlertPolicyService.END_TIME;
import static com.codeit.otboo.domain.weather.service.WeatherAlertPolicyService.START_TIME;

@Service
@RequiredArgsConstructor
public class WeatherAlertService {

    private final ProfileRepository profileRepository;
    private final WeatherRepository weatherRepository;
    private final YesterdayHourlyWeatherRepository yesterdayHourlyWeatherRepository;
    private final NotificationRepository notificationRepository;
    private final WeatherAlertPolicyService weatherAlertPolicyService;
    private final ApplicationEventPublisher eventPublisher;
    private final TimeProvider timeProvider;

    @Transactional
    public void sendDailyWeatherAlerts() {
        List<Profile> profiles = profileRepository.findAllForWeatherAlert();

        // X, Y를 담은 RegionKey를 기준으로 지역별로 사용자들을 그룹핑
        Map<RegionKey, List<Profile>> profilesByRegion = profiles.stream()
                .collect(Collectors.groupingBy(profile ->
                        new RegionKey(
                                profile.getLocation().getX(),
                                profile.getLocation().getY()
                        )
                ));

        LocalDate today = timeProvider.nowDate();
        LocalDate yesterday = today.minusDays(1);

        // START_TIME, END_TIME은 WeatherAlertPolicyService에서 정의
        LocalDateTime forecastedAt = today.atStartOfDay();
        LocalDateTime todayStart = today.atTime(START_TIME);
        LocalDateTime todayEnd = today.atTime(END_TIME);

        LocalTime yesterdayStart = START_TIME;
        LocalTime yesterdayEnd = END_TIME;

        for (Map.Entry<RegionKey, List<Profile>> entry : profilesByRegion.entrySet()) {
            RegionKey region = entry.getKey();
            List<Profile> regionProfiles = entry.getValue();

            List<Weather> todayWeathers =
                    weatherRepository.findWeatherForAlertByRegion(
                            forecastedAt,
                            region.x(),
                            region.y(),
                            todayStart,
                            todayEnd
                    );

            if (todayWeathers.isEmpty()) {
                continue;
            }

            sendTemperatureGapAlerts(
                    regionProfiles,
                    region,
                    yesterday,
                    yesterdayStart,
                    yesterdayEnd,
                    todayWeathers
            );

            sendPrecipitationChangeAlerts(regionProfiles, todayWeathers);
        }
    }

    private void sendTemperatureGapAlerts(
            List<Profile> regionProfiles,
            RegionKey region,
            LocalDate yesterday,
            LocalTime yesterdayStart,
            LocalTime yesterdayEnd,
            List<Weather> todayWeathers
    ) {
        List<YesterdayHourlyWeather> yesterdayWeathers =
                yesterdayHourlyWeatherRepository.findYesterdayWeatherForAlertByRegion(
                        yesterday,
                        region.x(),
                        region.y(),
                        yesterdayStart,
                        yesterdayEnd
                );

        TemperatureGapSummary summary = weatherAlertPolicyService.summarize(
                toTodayHourlyTemperatures(todayWeathers),
                toYesterdayHourlyTemperatures(yesterdayWeathers)
        );

        // 알림이 필요 없다면 종료
        if (!summary.shouldNotify()) {
            return;
        }

        List<Notification> notifications = regionProfiles.stream()
                .map(profile -> new Notification(
                        "어제와 기온 차가 커요",
                        summary.content(),
                        NotificationLevel.INFO,
                        profile.getUser()
                ))
                .toList();

        List<Notification> savedNotifications = notificationRepository.saveAll(notifications);
        publishSseEvents(savedNotifications);
    }

    private void sendPrecipitationChangeAlerts(List<Profile> regionProfiles, List<Weather> todayWeathers) {
        PrecipitationChangeSummary summary = weatherAlertPolicyService.summarizePrecipitationChanges(
                toHourlyPrecipitationStatuses(todayWeathers)
        );

        if (!summary.shouldNotify()) {
            return;
        }

        List<Notification> notifications = regionProfiles.stream()
                .map(profile -> new Notification(
                        "오늘 강수 예보가 바뀌어요",
                        summary.content(),
                        NotificationLevel.INFO,
                        profile.getUser()
                ))
                .toList();

        List<Notification> savedNotifications = notificationRepository.saveAll(notifications);
        publishSseEvents(savedNotifications);
    }

    // 날씨 엔티티에서 시간과, 온도 정보만 가지는 HourlyTemperature로 매핑
    private List<HourlyTemperature> toTodayHourlyTemperatures(List<Weather> weathers) {
        return weathers.stream()
                .map(weather -> new HourlyTemperature(
                        weather.getForecastAt().toLocalTime(),
                        weather.getTemperatureCurrent()
                ))
                .toList();
    }

    // 위와 동일하게 HourlyTemperature로 매핑
    private List<HourlyTemperature> toYesterdayHourlyTemperatures(List<YesterdayHourlyWeather> weathers) {
        return weathers.stream()
                .map(weather -> new HourlyTemperature(
                        weather.getHour(),
                        weather.getTemperature()
                ))
                .toList();
    }

    private List<HourlyPrecipitationStatus> toHourlyPrecipitationStatuses(List<Weather> weathers) {
        return weathers.stream()
                .map(weather -> new HourlyPrecipitationStatus(
                        weather.getForecastAt().toLocalTime(),
                        weather.getPrecipitationType()
                ))
                .toList();
    }

    private void publishSseEvents(List<Notification> notifications) {
        LocalDateTime publishedAt = timeProvider.nowDateTime();

        for (Notification notification : notifications) {
            eventPublisher.publishEvent( SseEvent.of(
                    new NotificationDto(
                            notification.getId(),
                            notification.getCreatedAt(),
                            notification.getReceiver().getId(),
                            notification.getTitle(),
                            notification.getContent(),
                            notification.getLevel()
                    )
            ));
        }
    }

    public record RegionKey(
            Integer x,
            Integer y
    ) {
    }
}
