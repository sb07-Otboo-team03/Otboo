package com.codeit.otboo.domain.weather.batch;

import com.codeit.otboo.batch.OtbooBatchApplication;
import com.codeit.otboo.batch.weather.scheduler.WeatherBatchScheduler;
import com.codeit.otboo.domain.weather.entity.*;
import com.codeit.otboo.domain.weather.repository.WeatherRepository;
import com.codeit.otboo.domain.weather.repository.YesterdayHourlyWeatherRepository;
import com.codeit.otboo.global.util.TimeProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.BDDMockito.given;

@SpringBootTest(
        classes = OtbooBatchApplication.class,
        properties = {
                "spring.batch.job.enabled=false"  // 실행 시 배치가 자동 실행되지 않도록 설정
        }
)
@SpringBatchTest
@ActiveProfiles("test")
class WeatherHistoryBatchConfigTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    @Qualifier("deleteYesterdayWeatherJob")
    private Job deleteYesterdayWeatherJob;

    @Autowired
    private WeatherRepository weatherRepository;

    @Autowired
    private YesterdayHourlyWeatherRepository yesterdayHourlyWeatherRepository;

    @MockitoBean
    private TimeProvider timeProvider;

    @MockitoBean
    private WeatherBatchScheduler weatherBatchScheduler;

    @BeforeEach
    void setUp() {
        weatherRepository.deleteAll();
        yesterdayHourlyWeatherRepository.deleteAll();

        given(timeProvider.nowDate()).willReturn(LocalDate.of(2026, 4, 6));
        given(timeProvider.nowDateTime()).willReturn(LocalDateTime.of(2026, 4, 6, 0, 0));

        jobLauncherTestUtils.setJob(deleteYesterdayWeatherJob);
    }

    @Test
    @DisplayName("어제 날씨 데이터를 yesterday_hourly_weather에 저장하고 Weather 원본은 삭제한다")
    void deleteYesterdayWeatherJob_success() throws Exception {
        // given
        LocalDate today = LocalDate.of(2026, 4, 6);
        LocalDate yesterday = today.minusDays(1);

        Weather yesterdayWeather1 = createWeather(
                yesterday.atStartOfDay(),
                yesterday.atTime(9, 0),
                60,
                127,
                12.3,
                55.0
        );

        Weather yesterdayWeather2 = createWeather(
                yesterday.atStartOfDay(),
                yesterday.atTime(10, 0),
                60,
                127,
                13.1,
                60.0
        );

        Weather todayWeather = createWeather(
                today.atStartOfDay(),
                today.atTime(9, 0),
                60,
                127,
                15.2,
                65.0
        );

        weatherRepository.saveAll(List.of(yesterdayWeather1, yesterdayWeather2, todayWeather));

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("requestedAt", LocalDateTime.now().toString())
                .toJobParameters();

        // when
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        // then
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        List<YesterdayHourlyWeather> results = yesterdayHourlyWeatherRepository.findAll();
        assertThat(results).hasSize(2);
        assertThat(results)
                .extracting(
                        YesterdayHourlyWeather::getX,
                        YesterdayHourlyWeather::getY,
                        YesterdayHourlyWeather::getDate,
                        YesterdayHourlyWeather::getHour,
                        YesterdayHourlyWeather::getTemperature,
                        YesterdayHourlyWeather::getHumidity
                )
                .containsExactlyInAnyOrder(
                        tuple(60, 127, yesterday, LocalTime.of(9, 0), 12.3, 55.0),
                        tuple(60, 127, yesterday, LocalTime.of(10, 0), 13.1, 60.0)
                );

        List<Weather> remainingWeathers = weatherRepository.findAll();
        assertThat(remainingWeathers).hasSize(1);
        assertThat(remainingWeathers.get(0).getForecastedAt()).isEqualTo(today.atStartOfDay());
    }

    @Test
    @DisplayName("어제 날씨 데이터가 없어도 배치는 정상 완료된다")
    void deleteYesterdayWeatherJob_success_whenNoYesterdayData() throws Exception {
        // given
        LocalDate today = LocalDate.of(2026, 4, 6);

        Weather todayWeather = createWeather(
                today.atStartOfDay(),
                today.atTime(9, 0),
                60,
                127,
                15.2,
                65.0
        );

        weatherRepository.save(todayWeather);

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("requestedAt", LocalDateTime.now().toString())
                .toJobParameters();

        // when
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        // then
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(yesterdayHourlyWeatherRepository.findAll()).isEmpty();
        assertThat(weatherRepository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("어제 시각에 해당하는 날씨 데이터만 YesterdayHourlyWeather로 이관한다")
    void migrateYesterdayWeather_onlyYesterdayForecastAt() throws Exception {
        // given
        LocalDate today = LocalDate.of(2026, 4, 6);
        LocalDate yesterday = today.minusDays(1);

        // 저장되어야 하는 데이터: forecastAt이 어제
        weatherRepository.save(createWeather(
                yesterday.atStartOfDay(),
                yesterday.atTime(0, 0),
                57, 126, 5.0, 65.0
        ));
        weatherRepository.save(createWeather(
                yesterday.atStartOfDay(),
                yesterday.atTime(23, 0),
                57, 126, 7.0, 70.0
        ));

        // 저장되면 안 되는 데이터 1: forecastedAt은 어제지만 forecastAt은 오늘
        weatherRepository.save(createWeather(
                yesterday.atStartOfDay(),
                today.atTime(0, 0),
                57, 126, 8.0, 80.0
        ));
        weatherRepository.save(createWeather(
                yesterday.atStartOfDay(),
                today.atTime(1, 0),
                57, 126, 9.0, 85.0
        ));

        // 저장되면 안 되는 데이터 2: forecastAt이 그제
        weatherRepository.save(createWeather(
                yesterday.minusDays(1).atStartOfDay(),
                yesterday.minusDays(1).atTime(23, 0),
                57, 126, 3.0, 60.0
        ));

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("requestedAt", "2026-04-06T00:00:00")
                .toJobParameters();

        // when
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        // then
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        List<YesterdayHourlyWeather> results = yesterdayHourlyWeatherRepository.findAll();

        assertThat(results).hasSize(2);

        assertThat(results)
                .extracting(
                        YesterdayHourlyWeather::getX,
                        YesterdayHourlyWeather::getY,
                        YesterdayHourlyWeather::getDate,
                        YesterdayHourlyWeather::getHour,
                        YesterdayHourlyWeather::getTemperature,
                        YesterdayHourlyWeather::getHumidity
                )
                .containsExactlyInAnyOrder(
                        tuple(57, 126, yesterday, LocalTime.of(0, 0), 5.0, 65.0),
                        tuple(57, 126, yesterday, LocalTime.of(23, 0), 7.0, 70.0)
                );

        assertThat(results)
                .extracting(YesterdayHourlyWeather::getDate)
                .containsOnly(yesterday);

        assertThat(results)
                .extracting(YesterdayHourlyWeather::getHour)
                .doesNotContain(LocalTime.of(1, 0));
    }

    private Weather createWeather(
            LocalDateTime forecastedAt,
            LocalDateTime forecastAt,
            Integer x,
            Integer y,
            Double temperatureCurrent,
            Double humidityCurrent
    ) {
        return new Weather(
                forecastedAt,
                forecastAt,
                x,
                y,
                temperatureCurrent,
                null,
                null,
                1.2,
                WindAsWord.WEAK,
                SkyStatus.CLEAR,
                PrecipitationType.NONE,
                0.0,
                20.0,
                humidityCurrent
        );
    }
}
