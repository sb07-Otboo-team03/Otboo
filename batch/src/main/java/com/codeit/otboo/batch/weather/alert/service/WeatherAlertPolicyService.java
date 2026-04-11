package com.codeit.otboo.batch.weather.alert.service;

import com.codeit.otboo.batch.weather.alert.model.HourlyPrecipitationStatus;
import com.codeit.otboo.batch.weather.alert.model.HourlyTemperature;
import com.codeit.otboo.batch.weather.alert.model.PrecipitationChangeSummary;
import com.codeit.otboo.batch.weather.alert.model.TemperatureGapSummary;
import com.codeit.otboo.domain.weather.entity.PrecipitationType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class WeatherAlertPolicyService {

    public static final LocalTime START_TIME = LocalTime.of(6, 0);
    public static final LocalTime END_TIME = LocalTime.of(21, 0);
    public static final LocalTime EVENING_START = LocalTime.of(18, 0);
    public static final LocalTime EVENING_END = LocalTime.of(21, 0);

    private static final int TEMPERATURE_GAP_THRESHOLD = 5;

    public TemperatureGapSummary summarize(
            List<HourlyTemperature> todayTemperatures,
            List<HourlyTemperature> yesterdayTemperatures
    ) {
        Map<LocalTime, Double> todayMap = todayTemperatures.stream()
                .collect(Collectors.toMap(HourlyTemperature::time, HourlyTemperature::temperature));

        Map<LocalTime, Double> yesterdayMap = yesterdayTemperatures.stream()
                .collect(Collectors.toMap(HourlyTemperature::time, HourlyTemperature::temperature));

        // 시간 별로 어제와 오늘 온도의 차이를 리스트에 저장
        List<TemperatureGap> gaps = buildTemperatureGaps(todayMap, yesterdayMap);

        if (gaps.isEmpty()) {
            return new TemperatureGapSummary(
                    0.0,
                    0,
                    0,
                    null,
                    0.0,
                    false,
                    null
            );
        }

        // 오늘 하루 평균 온도 차이
        double averageGap = gaps.stream()
                .mapToInt(TemperatureGap::gap)
                .average()
                .orElse(0.0);

        // 오늘 하루 제일 큰 온도 차이
        TemperatureGap maxGap = gaps.stream()
                .max(Comparator.comparingInt(g -> Math.abs(g.gap())))
                .orElseThrow();

        // eveningAverageGap: 오후 6시 ~ 오후 9시 사이의 평균 온도 차이
        // 유의미한 차이가 있는 경우 알림을 보내기 위해 저장
        List<TemperatureGap> eveningGaps = gaps.stream()
                .filter(g -> !g.time().isBefore(EVENING_START) && !g.time().isAfter(EVENING_END))
                .toList();

        double eveningAverageGap = eveningGaps.stream()
                .mapToInt(TemperatureGap::gap)
                .average()
                .orElse(0.0);

        // 온도 차이가 5도 이상 나는 경우 true
        // true인 경우, content 생성
        boolean shouldNotify = Math.abs(maxGap.gap()) >= TEMPERATURE_GAP_THRESHOLD;

        String content = shouldNotify
                ? buildNotificationContent(averageGap, maxGap, eveningAverageGap)
                : null;

        return new TemperatureGapSummary(
                roundToOneDecimal(averageGap),
                Math.abs(maxGap.gap()),
                maxGap.gap(),
                maxGap.time(),
                roundToOneDecimal(eveningAverageGap),
                shouldNotify,
                content
        );
    }

    private List<TemperatureGap> buildTemperatureGaps(
            Map<LocalTime, Double> todayMap,
            Map<LocalTime, Double> yesterdayMap
    ) {
        List<TemperatureGap> result = new ArrayList<>();

        LocalTime time = START_TIME;
        while (!time.isAfter(END_TIME)) {
            Double todayTemp = todayMap.get(time);
            Double yesterdayTemp = yesterdayMap.get(time);

            if (todayTemp != null && yesterdayTemp != null) {
                int gap = (int) Math.round(todayTemp - yesterdayTemp);
                result.add(new TemperatureGap(time, gap));
            }

            time = time.plusHours(1);
        }

        return result;
    }

    private String buildNotificationContent(
            double averageGap,
            TemperatureGap maxGap,
            double eveningAverageGap
    ) {
        String averageSentence = buildAverageSentence(averageGap);
        String maxSentence = buildMaxSentence(maxGap);
        String eveningSentence = buildEveningSentence(eveningAverageGap);

        if (eveningSentence == null) {
            return averageSentence + " " + maxSentence;
        }

        return averageSentence + " " + maxSentence + " " + eveningSentence;
    }

    // 전체 평균 온도 차이를 기준으로 생성
    private String buildAverageSentence(double averageGap) {
        int rounded = (int) Math.round(averageGap);

        if (rounded == 0) {
            return "오늘은 어제와 전반적인 기온이 비슷해요.";
        }

        String direction = rounded > 0 ? "높아요" : "낮아요";
        return String.format("오늘은 어제보다 전반적으로 %d도 %s.",
                Math.abs(rounded),
                direction
        );
    }

    // 제일 높은 온도 차이를 기준으로 생성
    private String buildMaxSentence(TemperatureGap maxGap) {
        int hour24 = maxGap.time().getHour();
        String meridiem = hour24 < 12 ? "오전" : "오후";
        int hour12 = hour24 % 12 == 0 ? 12 : hour24 % 12;
        String direction = maxGap.gap() > 0 ? "높아요" : "낮아요";

        return String.format("특히 %s %d시에는 %d도 더 %s.",
                meridiem,
                hour12,
                Math.abs(maxGap.gap()),
                direction
        );
    }

    // 저녁 시간의 온도 차이를 기준으로 생성
    // 저녁 평균 온도 차이가 5도 이하인 경우에는 생성하지 않는다.
    private String buildEveningSentence(double eveningAverageGap) {
        int rounded = (int) Math.round(eveningAverageGap);

        if (Math.abs(rounded) < TEMPERATURE_GAP_THRESHOLD) {
            return null;
        }

        String direction = rounded > 0 ? "높아요" : "낮아요";
        return String.format("저녁에도 어제보다 평균 %d도 %s.",
                Math.abs(rounded),
                direction
        );
    }

    // 실수 값을 소수점 한자리로 변환
    // Math.round() 사용 시 음수 반올림을 기대하지 않는 결과를 전달
    // ex) Math.round(-4.75) = -4.7 (기대 값은 -4.8)
    private double roundToOneDecimal(double value) {
        return BigDecimal.valueOf(value)
                .setScale(1, RoundingMode.HALF_UP) // 반올림
                .doubleValue();
    }

    private record TemperatureGap(
            LocalTime time,
            int gap
    ) {}

    /**
     * 비가 내리거나 그치는 알림 전달 여부 계산 로직
     */
    public PrecipitationChangeSummary summarizePrecipitationChanges(
            List<HourlyPrecipitationStatus> statuses
    ) {
        if (statuses.isEmpty()) {
            return PrecipitationChangeSummary.empty();
        }

        LocalTime startTime = null;
        PrecipitationType startType = null;

        LocalTime endTime = null;
        PrecipitationType endType = null;

        PrecipitationType previous = statuses.get(0).precipitationType();

        for (int i = 1; i < statuses.size(); i++) {
            HourlyPrecipitationStatus current = statuses.get(i);
            PrecipitationType currentType = current.precipitationType();

            // 강수 시작: 없음 -> 강수 있음
            if (previous == PrecipitationType.NONE && currentType != PrecipitationType.NONE && startTime == null) {
                startTime = current.time();
                startType = currentType;
            }

            // 강수 종료: 강수 있음 -> 없음
            if (previous != PrecipitationType.NONE && currentType == PrecipitationType.NONE && endTime == null) {
                endTime = current.time();
                endType = previous;
            }

            previous = currentType;
        }

        boolean shouldNotify = startTime != null || endTime != null;
        if (!shouldNotify) {
            return PrecipitationChangeSummary.empty();
        }

        return new PrecipitationChangeSummary(
                startTime,
                startType,
                endTime,
                endType,
                true,
                buildPrecipitationContent(startTime, startType, endTime, endType)
        );
    }

    private String buildPrecipitationContent(
            LocalTime startTime,
            PrecipitationType startType,
            LocalTime endTime,
            PrecipitationType endType
    ) {
        if (startTime != null && endTime != null) {
            return String.format(
                    "오늘 %s부터 %s이(가) 오고, %s쯤 그칠 예정이에요.",
                    formatHour(startTime),
                    precipitationLabel(startType),
                    formatHour(endTime)
            );
        }

        if (startTime != null) {
            return String.format(
                    "오늘 %s부터 %s이(가) 올 예정이에요.",
                    formatHour(startTime),
                    precipitationLabel(startType)
            );
        }

        return String.format(
                "오늘 %s쯤 %s이(가) 그칠 예정이에요.",
                formatHour(endTime),
                precipitationLabel(endType)
        );
    }

    private String precipitationLabel(PrecipitationType type) {
        return switch (type) {
            case RAIN -> "비";
            case SNOW -> "눈";
            case RAIN_SNOW -> "비/눈";
            case SHOWER -> "소나기";
            case NONE -> "강수";
        };
    }

    private String formatHour(LocalTime time) {
        int hour24 = time.getHour();
        String meridiem = hour24 < 12 ? "오전" : "오후";
        int hour12 = hour24 % 12 == 0 ? 12 : hour24 % 12;
        return meridiem + " " + hour12 + "시";
    }
}
