package com.codeit.otboo.domain.weather.batch;

import com.codeit.otboo.batch.weather.scheduler.WeatherBatchScheduler;
import com.codeit.otboo.global.util.TimeProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WeatherBatchSchedulerTest {

    @Mock
    private JobLauncher jobLauncher;

    @Mock(name = "weatherForecastCollectionJob")
    private Job weatherForecastCollectionJob;

    @Mock(name = "deleteYesterdayWeatherJob")
    private Job deleteYesterdayWeatherJob;

    @Mock(name = "weatherAlertJob")
    private Job weatherAlertJob;

    @Mock
    private TimeProvider timeProvider;

    private WeatherBatchScheduler weatherBatchScheduler;

    @BeforeEach
    void setUp() { // Job을 직접 주입
        weatherBatchScheduler = new WeatherBatchScheduler(
                jobLauncher,
                weatherForecastCollectionJob,
                deleteYesterdayWeatherJob,
                weatherAlertJob,
                timeProvider
        );
    }

    @Test
    @DisplayName("날씨 조회 배치 실행 시 baseDate, baseTime, requestedAt 파라미터를 생성한다")
    void runWeatherForecastBatch() throws Exception {
        // given
        given(timeProvider.nowDate()).willReturn(LocalDate.of(2026, 4, 6));
        given(timeProvider.nowDateTime()).willReturn(LocalDateTime.of(2026, 4, 6, 2, 15));
        given(timeProvider.nowTime()).willReturn(LocalTime.of(2, 15));

        // when
        weatherBatchScheduler.runWeatherForecastBatch();

        // then
        ArgumentCaptor<JobParameters> captor = ArgumentCaptor.forClass(JobParameters.class);
        verify(jobLauncher).run(eq(weatherForecastCollectionJob), captor.capture());

        JobParameters jobParameters = captor.getValue();
        assertThat(jobParameters.getString("baseDate")).isEqualTo("20260406");
        assertThat(jobParameters.getString("baseTime")).isEqualTo("0200");
        assertThat(jobParameters.getString("requestedAt")).isEqualTo("2026-04-06T02:15");
    }

    @Test
    @DisplayName("어제 날씨 이관/삭제 배치 실행 시 requestedAt 파라미터를 생성한다")
    void runDeleteYesterdayWeatherBatch() throws Exception {
        // given
        given(timeProvider.nowDate()).willReturn(LocalDate.of(2026, 4, 6));
        given(timeProvider.nowDateTime()).willReturn(LocalDateTime.of(2026, 4, 6, 0, 0));

        // when
        weatherBatchScheduler.runDeleteYesterdayWeatherBatch();

        // then
        ArgumentCaptor<JobParameters> captor = ArgumentCaptor.forClass(JobParameters.class);
        verify(jobLauncher).run(eq(deleteYesterdayWeatherJob), captor.capture());

        JobParameters jobParameters = captor.getValue();
        assertThat(jobParameters.getString("requestedAt")).isEqualTo("2026-04-06T00:00");
    }

    @Test
    @DisplayName("날씨 알림 배치 실행 시 requestedAt 파라미터를 생성한다")
    void runWeatherAlertBatch() throws Exception {
        // given
        given(timeProvider.nowDateTime()).willReturn(LocalDateTime.of(2026, 4, 6, 6, 0));

        // when
        weatherBatchScheduler.runWeatherAlertBatch();

        // then
        ArgumentCaptor<JobParameters> captor = ArgumentCaptor.forClass(JobParameters.class);
        verify(jobLauncher).run(eq(weatherAlertJob), captor.capture());

        JobParameters jobParameters = captor.getValue();
        assertThat(jobParameters.getString("requestedAt")).isEqualTo("2026-04-06T06:00");
    }
}
