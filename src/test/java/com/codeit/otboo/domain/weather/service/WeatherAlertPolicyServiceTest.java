package com.codeit.otboo.domain.weather.service;

import com.codeit.otboo.batch.weather.alert.service.WeatherAlertPolicyService;
import com.codeit.otboo.batch.weather.alert.model.HourlyPrecipitationStatus;
import com.codeit.otboo.batch.weather.alert.model.HourlyTemperature;
import com.codeit.otboo.batch.weather.alert.model.PrecipitationChangeSummary;
import com.codeit.otboo.batch.weather.alert.model.TemperatureGapSummary;
import com.codeit.otboo.domain.weather.entity.PrecipitationType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WeatherAlertPolicyServiceTest {

    private final WeatherAlertPolicyService weatherAlertPolicyService = new WeatherAlertPolicyService();

    @Nested
    @DisplayName("summarize")
    class SummarizeTest {

        @Test
        @DisplayName("06시~21시 기준 평균 차이, 최대 차이, 저녁 평균 차이를 계산하고 알림 여부를 true로 반환한다")
        void summarize_returnsSummaryAndShouldNotifyTrue() {
            // given
            List<HourlyTemperature> today = List.of(
                    new HourlyTemperature(LocalTime.of(6, 0), 10),
                    new HourlyTemperature(LocalTime.of(7, 0), 11),
                    new HourlyTemperature(LocalTime.of(8, 0), 12),
                    new HourlyTemperature(LocalTime.of(18, 0), 9),
                    new HourlyTemperature(LocalTime.of(19, 0), 8),
                    new HourlyTemperature(LocalTime.of(20, 0), 7),
                    new HourlyTemperature(LocalTime.of(21, 0), 6)
            );

            List<HourlyTemperature> yesterday = List.of(
                    new HourlyTemperature(LocalTime.of(6, 0), 14), // -4
                    new HourlyTemperature(LocalTime.of(7, 0), 15), // -4
                    new HourlyTemperature(LocalTime.of(8, 0), 18), // -6
                    new HourlyTemperature(LocalTime.of(18, 0), 13), // -4
                    new HourlyTemperature(LocalTime.of(19, 0), 13), // -5
                    new HourlyTemperature(LocalTime.of(20, 0), 12), // -5
                    new HourlyTemperature(LocalTime.of(21, 0), 11)  // -5
            );

            // when
            TemperatureGapSummary result = weatherAlertPolicyService.summarize(today, yesterday);

            // then
            assertThat(result.averageGap()).isEqualTo(-4.7); // (-4-4-6-4-5-5-5)/7 = -4.714...
            assertThat(result.maxAbsGap()).isEqualTo(6);
            assertThat(result.maxGap()).isEqualTo(-6);
            assertThat(result.maxGapTime()).isEqualTo(LocalTime.of(8, 0));
            assertThat(result.eveningAverageGap()).isEqualTo(-4.8); // (-4-5-5-5)/4 = -4.75
            assertThat(result.shouldNotify()).isTrue();
            assertThat(result.content()).isNotNull();
        }

        @Test
        @DisplayName("최대 절대 기온 차이가 5도 미만이면 알림을 보내지 않는다")
        void summarize_returnsShouldNotifyFalse_whenMaxGapLessThanThreshold() {
            // given
            List<HourlyTemperature> today = List.of(
                    new HourlyTemperature(LocalTime.of(6, 0), 10),
                    new HourlyTemperature(LocalTime.of(7, 0), 11),
                    new HourlyTemperature(LocalTime.of(8, 0), 12)
            );

            List<HourlyTemperature> yesterday = List.of(
                    new HourlyTemperature(LocalTime.of(6, 0), 13), // -3
                    new HourlyTemperature(LocalTime.of(7, 0), 13), // -2
                    new HourlyTemperature(LocalTime.of(8, 0), 15)  // -3
            );

            // when
            TemperatureGapSummary result = weatherAlertPolicyService.summarize(today, yesterday);

            // then
            assertThat(result.maxAbsGap()).isEqualTo(3);
            assertThat(result.shouldNotify()).isFalse();
            assertThat(result.content()).isNull();
        }

        @Test
        @DisplayName("오늘과 어제에 모두 존재하는 시간대만 비교한다")
        void summarize_comparesOnlyMatchedTimes() {
            // given
            List<HourlyTemperature> today = List.of(
                    new HourlyTemperature(LocalTime.of(6, 0), 10),
                    new HourlyTemperature(LocalTime.of(7, 0), 11),
                    new HourlyTemperature(LocalTime.of(8, 0), 20)
            );

            List<HourlyTemperature> yesterday = List.of(
                    new HourlyTemperature(LocalTime.of(6, 0), 15), // -5
                    new HourlyTemperature(LocalTime.of(7, 0), 15)  // -4
                    // 8시는 없음
            );

            // when
            TemperatureGapSummary result = weatherAlertPolicyService.summarize(today, yesterday);

            // then
            assertThat(result.averageGap()).isEqualTo(-4.5);
            assertThat(result.maxAbsGap()).isEqualTo(5);
            assertThat(result.maxGapTime()).isEqualTo(LocalTime.of(6, 0));
            assertThat(result.shouldNotify()).isTrue();
        }

        @Test
        @DisplayName("비교 가능한 시간대가 없으면 empty summary를 반환한다")
        void summarize_returnsEmptySummary_whenNoComparableTimes() {
            // given
            List<HourlyTemperature> today = List.of(
                    new HourlyTemperature(LocalTime.of(6, 0), 10)
            );

            List<HourlyTemperature> yesterday = List.of(
                    new HourlyTemperature(LocalTime.of(7, 0), 10)
            );

            // when
            TemperatureGapSummary result = weatherAlertPolicyService.summarize(today, yesterday);

            // then
            assertThat(result.averageGap()).isZero();
            assertThat(result.maxAbsGap()).isZero();
            assertThat(result.maxGap()).isZero();
            assertThat(result.maxGapTime()).isNull();
            assertThat(result.eveningAverageGap()).isZero();
            assertThat(result.shouldNotify()).isFalse();
            assertThat(result.content()).isNull();
        }

        @Test
        @DisplayName("22시 데이터가 있어도 START_TIME~END_TIME 범위 밖이면 계산에서 제외한다")
        void summarize_ignoresOutsidePolicyRange() {
            // given
            List<HourlyTemperature> today = List.of(
                    new HourlyTemperature(LocalTime.of(21, 0), 10),
                    new HourlyTemperature(LocalTime.of(22, 0), 30)
            );

            List<HourlyTemperature> yesterday = List.of(
                    new HourlyTemperature(LocalTime.of(21, 0), 15),
                    new HourlyTemperature(LocalTime.of(22, 0), 10)
            );

            // when
            TemperatureGapSummary result = weatherAlertPolicyService.summarize(today, yesterday);

            // then
            // 21시만 반영 -> gap = -5
            assertThat(result.averageGap()).isEqualTo(-5.0);
            assertThat(result.maxAbsGap()).isEqualTo(5);
            assertThat(result.maxGap()).isEqualTo(-5);
            assertThat(result.maxGapTime()).isEqualTo(LocalTime.of(21, 0));
        }
    }

    @Nested
    @DisplayName("summarizePrecipitationChanges")
    class SummarizePrecipitationChangesTest {

        @Test
        @DisplayName("강수가 없다가 비가 오기 시작하면 시작 시간과 타입을 반환한다")
        void summarizePrecipitationChanges_returnsStartInfo_whenRainStarts() {
            // given
            List<HourlyPrecipitationStatus> statuses = List.of(
                    new HourlyPrecipitationStatus(LocalTime.of(6, 0), PrecipitationType.NONE),
                    new HourlyPrecipitationStatus(LocalTime.of(7, 0), PrecipitationType.NONE),
                    new HourlyPrecipitationStatus(LocalTime.of(8, 0), PrecipitationType.RAIN),
                    new HourlyPrecipitationStatus(LocalTime.of(9, 0), PrecipitationType.RAIN)
            );

            // when
            PrecipitationChangeSummary result =
                    weatherAlertPolicyService.summarizePrecipitationChanges(statuses);

            // then
            assertThat(result.startTime()).isEqualTo(LocalTime.of(8, 0));
            assertThat(result.startType()).isEqualTo(PrecipitationType.RAIN);
            assertThat(result.endTime()).isNull();
            assertThat(result.endType()).isNull();
            assertThat(result.shouldNotify()).isTrue();
        }

        @Test
        @DisplayName("비가 오다가 그치면 종료 시간과 타입을 반환한다")
        void summarizePrecipitationChanges_returnsEndInfo_whenRainEnds() {
            // given
            List<HourlyPrecipitationStatus> statuses = List.of(
                    new HourlyPrecipitationStatus(LocalTime.of(6, 0), PrecipitationType.RAIN),
                    new HourlyPrecipitationStatus(LocalTime.of(7, 0), PrecipitationType.RAIN),
                    new HourlyPrecipitationStatus(LocalTime.of(8, 0), PrecipitationType.NONE)
            );

            // when
            PrecipitationChangeSummary result =
                    weatherAlertPolicyService.summarizePrecipitationChanges(statuses);

            // then
            assertThat(result.startTime()).isNull();
            assertThat(result.startType()).isNull();
            assertThat(result.endTime()).isEqualTo(LocalTime.of(8, 0));
            assertThat(result.endType()).isEqualTo(PrecipitationType.RAIN);
            assertThat(result.shouldNotify()).isTrue();
        }

        @Test
        @DisplayName("강수가 시작됐다가 그치면 시작과 종료 정보를 모두 반환한다")
        void summarizePrecipitationChanges_returnsStartAndEndInfo() {
            // given
            List<HourlyPrecipitationStatus> statuses = List.of(
                    new HourlyPrecipitationStatus(LocalTime.of(6, 0), PrecipitationType.NONE),
                    new HourlyPrecipitationStatus(LocalTime.of(7, 0), PrecipitationType.RAIN),
                    new HourlyPrecipitationStatus(LocalTime.of(8, 0), PrecipitationType.RAIN),
                    new HourlyPrecipitationStatus(LocalTime.of(9, 0), PrecipitationType.NONE)
            );

            // when
            PrecipitationChangeSummary result =
                    weatherAlertPolicyService.summarizePrecipitationChanges(statuses);

            // then
            assertThat(result.startTime()).isEqualTo(LocalTime.of(7, 0));
            assertThat(result.startType()).isEqualTo(PrecipitationType.RAIN);
            assertThat(result.endTime()).isEqualTo(LocalTime.of(9, 0));
            assertThat(result.endType()).isEqualTo(PrecipitationType.RAIN);
            assertThat(result.shouldNotify()).isTrue();
            assertThat(result.content()).isNotNull();
        }

        @Test
        @DisplayName("하루 종일 강수 상태 변화가 없으면 empty summary를 반환한다")
        void summarizePrecipitationChanges_returnsEmptySummary_whenNoChange() {
            // given
            List<HourlyPrecipitationStatus> statuses = List.of(
                    new HourlyPrecipitationStatus(LocalTime.of(6, 0), PrecipitationType.NONE),
                    new HourlyPrecipitationStatus(LocalTime.of(7, 0), PrecipitationType.NONE),
                    new HourlyPrecipitationStatus(LocalTime.of(8, 0), PrecipitationType.NONE)
            );

            // when
            PrecipitationChangeSummary result =
                    weatherAlertPolicyService.summarizePrecipitationChanges(statuses);

            // then
            assertThat(result.startTime()).isNull();
            assertThat(result.startType()).isNull();
            assertThat(result.endTime()).isNull();
            assertThat(result.endType()).isNull();
            assertThat(result.shouldNotify()).isFalse();
            assertThat(result.content()).isNull();
        }
    }
}