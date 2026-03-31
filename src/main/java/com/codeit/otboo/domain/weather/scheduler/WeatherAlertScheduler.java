package com.codeit.otboo.domain.weather.scheduler;

import com.codeit.otboo.domain.weather.service.WeatherAlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WeatherAlertScheduler {

    private final WeatherAlertService weatherAlertService;

    @Scheduled(cron = "0 0 6 * * *")
    public void run() {
        weatherAlertService.sendDailyWeatherAlerts();
    }
}
