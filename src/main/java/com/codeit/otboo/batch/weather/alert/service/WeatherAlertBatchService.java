package com.codeit.otboo.batch.weather.alert.service;

import com.codeit.otboo.batch.weather.alert.model.*;
import com.codeit.otboo.domain.notification.dto.NotificationCreateCommand;
import com.codeit.otboo.domain.notification.dto.NotificationLevel;
import com.codeit.otboo.domain.profile.repository.ProfileRepository;
import com.codeit.otboo.domain.sse.event.WeatherSseEvent;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.codeit.otboo.batch.weather.alert.service.WeatherAlertPolicyService.END_TIME;
import static com.codeit.otboo.batch.weather.alert.service.WeatherAlertPolicyService.START_TIME;

@Service
@RequiredArgsConstructor
public class WeatherAlertBatchService {

    private final ProfileRepository profileRepository;
    private final WeatherRepository weatherRepository;
    private final YesterdayHourlyWeatherRepository yesterdayHourlyWeatherRepository;
    private final WeatherAlertPolicyService weatherAlertPolicyService;
    private final ApplicationEventPublisher eventPublisher;
    private final TimeProvider timeProvider;

    @Transactional(readOnly = true)
    public List<RegionAlertTarget> findAlertTargetsByRegion() {
        List<AlertTarget> targets = profileRepository.findAllForWeatherAlert();

        Map<RegionKey, List<UUID>> grouped = targets.stream()
                .collect(Collectors.groupingBy(t ->
                        new RegionKey(t.x(), t.y()),
                        Collectors.mapping(AlertTarget::userId, Collectors.toList())
                ));

        return grouped.entrySet().stream()
                .map(entry -> new RegionAlertTarget(
                        entry.getKey().x(),
                        entry.getKey().y(),
                        entry.getValue()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public RegionAlertResult buildRegionAlertResult(RegionAlertTarget target) {
        LocalDate today = timeProvider.nowDate();
        LocalDate yesterday = today.minusDays(1);

        LocalDateTime forecastedAt = today.atStartOfDay();
        LocalDateTime todayStart = today.atTime(START_TIME);
        LocalDateTime todayEnd = today.atTime(END_TIME);

        List<Weather> todayWeathers = weatherRepository.findWeatherForAlertByRegion(
                forecastedAt,
                target.x(),
                target.y(),
                todayStart,
                todayEnd
        );

        if (todayWeathers.isEmpty()) {
            return new RegionAlertResult(target.x(), target.y(), List.of());
        }

        List<NotificationCreateCommand> commands = new ArrayList<>();

        commands.addAll(buildTemperatureGapNotifications(
                target.userIds(),
                target.x(),
                target.y(),
                yesterday,
                todayWeathers
        ));

        commands.addAll(buildPrecipitationChangeNotifications(
                target.userIds(),
                todayWeathers
        ));

        return new RegionAlertResult(target.x(), target.y(), commands);
    }

    private List<NotificationCreateCommand> buildTemperatureGapNotifications(
            List<UUID> userIds,
            Integer x,
            Integer y,
            LocalDate yesterday,
            List<Weather> todayWeathers
    ) {
        List<YesterdayHourlyWeather> yesterdayWeathers =
                yesterdayHourlyWeatherRepository.findYesterdayWeatherForAlertByRegion(
                        yesterday,
                        x,
                        y,
                        START_TIME,
                        END_TIME
                );

        TemperatureGapSummary summary = weatherAlertPolicyService.summarize(
                toTodayHourlyTemperatures(todayWeathers),
                toYesterdayHourlyTemperatures(yesterdayWeathers)
        );

        if (!summary.shouldNotify()) {
            return List.of();
        }

        return userIds.stream()
                .map(userId -> new NotificationCreateCommand(
                        userId,
                        "어제와 기온 차가 커요",
                        summary.content(),
                        NotificationLevel.INFO
                ))
                .toList();
    }

    private List<NotificationCreateCommand> buildPrecipitationChangeNotifications(
            List<UUID> userIds,
            List<Weather> todayWeathers
    ) {
        PrecipitationChangeSummary summary = weatherAlertPolicyService.summarizePrecipitationChanges(
                toHourlyPrecipitationStatuses(todayWeathers)
        );

        if (!summary.shouldNotify()) {
            return List.of();
        }

        return userIds.stream()
                .map(userId -> new NotificationCreateCommand(
                        userId,
                        "오늘 강수 예보가 바뀌어요",
                        summary.content(),
                        NotificationLevel.INFO
                ))
                .toList();
    }

    public void publishWeatherEvent(RegionAlertResult result) {
        if (result.isEmpty()) {
            return;
        }

        eventPublisher.publishEvent(new WeatherSseEvent(result.commands()));
    }

    private List<HourlyTemperature> toTodayHourlyTemperatures(List<Weather> weathers) {
        return weathers.stream()
                .map(weather -> new HourlyTemperature(
                        weather.getForecastAt().toLocalTime(),
                        weather.getTemperatureCurrent()
                ))
                .toList();
    }

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

    public record RegionKey(
            Integer x,
            Integer y
    ) {
    }
}
