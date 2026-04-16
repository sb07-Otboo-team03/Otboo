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
    @DisplayName("weather 테이블에서 오늘 이전 데이터는 삭제되고 오늘 데이터만 남는다")
    void deleteYesterdayWeatherJob_deletesOldWeatherData() throws Exception {
        // given
        LocalDate today = LocalDate.of(2026, 4, 6);
        LocalDate yesterday = today.minusDays(1);
        LocalDate twoDaysAgo = today.minusDays(2);

        Weather twoDaysAgoWeather = createWeather(
                twoDaysAgo.atStartOfDay(),
                twoDaysAgo.atTime(23, 0),
                60,
                127,
                10.1,
                50.0
        );

        Weather yesterdayWeather = createWeather(
                yesterday.atStartOfDay(),
                yesterday.atTime(9, 0),
                60,
                127,
                12.3,
                55.0
        );

        Weather todayWeather = createWeather(
                today.atStartOfDay(),
                today.atTime(9, 0),
                60,
                127,
                15.2,
                65.0
        );

        weatherRepository.saveAll(List.of(twoDaysAgoWeather, yesterdayWeather, todayWeather));

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("requestedAt", LocalDateTime.now().toString())
                .toJobParameters();

        // when
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        // then
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        List<Weather> remainingWeathers = weatherRepository.findAll();
        assertThat(remainingWeathers).hasSize(1);
        assertThat(remainingWeathers.get(0).getForecastedAt()).isEqualTo(today.atStartOfDay());
    }

    @Test
    @DisplayName("yesterday_hourly_weather 테이블에서는 어제 이전 데이터만 삭제되고 어제 데이터는 남는다")
    void deleteYesterdayWeatherJob_deletesOnlyBeforeYesterdayFromYesterdayTable() throws Exception {
        // given
        LocalDate today = LocalDate.of(2026, 4, 6);
        LocalDate yesterday = today.minusDays(1);
        LocalDate twoDaysAgo = today.minusDays(2);

        YesterdayHourlyWeather oldData = createYesterdayHourlyWeather(
                60,
                127,
                twoDaysAgo,
                LocalTime.of(9, 0),
                11.1,
                50.0
        );

        YesterdayHourlyWeather yesterdayData = createYesterdayHourlyWeather(
                60,
                127,
                yesterday,
                LocalTime.of(10, 0),
                13.3,
                60.0
        );

        yesterdayHourlyWeatherRepository.saveAll(List.of(oldData, yesterdayData));

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("requestedAt", LocalDateTime.now().toString())
                .toJobParameters();

        // when
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        // then
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        List<YesterdayHourlyWeather> remaining = yesterdayHourlyWeatherRepository.findAll();
        assertThat(remaining).hasSize(1);
        assertThat(remaining.get(0).getDate()).isEqualTo(yesterday);
        assertThat(remaining.get(0).getHour()).isEqualTo(LocalTime.of(10, 0));
    }

    @Test
    @DisplayName("삭제 대상 데이터가 없어도 배치는 정상 완료된다")
    void deleteYesterdayWeatherJob_completesWhenNoDeleteTargetExists() throws Exception {
        // given
        LocalDate today = LocalDate.of(2026, 4, 6);
        LocalDate yesterday = today.minusDays(1);

        Weather todayWeather = createWeather(
                today.atStartOfDay(),
                today.atTime(9, 0),
                60,
                127,
                15.2,
                65.0
        );

        YesterdayHourlyWeather yesterdayData = createYesterdayHourlyWeather(
                60,
                127,
                yesterday,
                LocalTime.of(9, 0),
                13.0,
                60.0
        );

        weatherRepository.save(todayWeather);
        yesterdayHourlyWeatherRepository.save(yesterdayData);

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("requestedAt", LocalDateTime.now().toString())
                .toJobParameters();

        // when
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        // then
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(weatherRepository.findAll()).hasSize(1);
        assertThat(yesterdayHourlyWeatherRepository.findAll()).hasSize(1);
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

    private YesterdayHourlyWeather createYesterdayHourlyWeather(
            Integer x,
            Integer y,
            LocalDate date,
            LocalTime hour,
            Double temperature,
            Double humidity
    ) {
        return new YesterdayHourlyWeather(
                x,
                y,
                date,
                hour,
                temperature,
                humidity
        );
    }
}
