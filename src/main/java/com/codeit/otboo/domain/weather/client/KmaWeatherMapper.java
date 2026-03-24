package com.codeit.otboo.domain.weather.client;

import com.codeit.otboo.domain.weather.client.dto.KmaWeatherItem;
import com.codeit.otboo.domain.weather.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.IntStream;

@Component
@Slf4j
public class KmaWeatherMapper {

    private static final Set<KmaCategory> WEATHER_CATEGORIES = EnumSet.of(
            KmaCategory.POP,
            KmaCategory.PCP,
            KmaCategory.PTY,
            KmaCategory.REH,
            KmaCategory.SKY,
            KmaCategory.TMP,
            KmaCategory.TMX,
            KmaCategory.TMN,
            KmaCategory.WSD
    );

    private static final Set<KmaCategory> YESTERDAY_CATEGORIES = EnumSet.of(
            KmaCategory.REH,
            KmaCategory.TMP
    );

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    private static final String MIN_TEMPERATURE_TIME = "0600";
    private static final String MAX_TEMPERATURE_TIME = "1500";

    public List<Weather> toWeathers(String baseTime, int nx, int ny, List<KmaWeatherItem> items, boolean isScheduling) {

        // 각 날짜 별로 TMX, TMN을 최고, 최저 기온에 넣어가지고 전달?
        // 업데이트하는거도 생각해서 해야됨.

        // 필요한 정보만 필터링
        List<KmaWeatherItem> filtered = items.stream()
                .filter(item -> KmaCategory.from(item.category())
                        .map(WEATHER_CATEGORIES::contains)
                        .orElse(false)) // category를 enum으로 변환 실패시 필터로 거르겠다.
                .toList();

        List<String> forecastDates = resolveForecastDates(baseTime, isScheduling);

        // 날짜 별로 데이터 정제
        return forecastDates.stream()
                .flatMap(date -> refineWeatherInfo(date, filtered, nx, ny).stream())
                .toList();
    }

    private List<String> resolveForecastDates(String baseTime, boolean isScheduling) {
        LocalDate startDate = ("2300".equals(baseTime) && isScheduling)
                ? LocalDate.now(SEOUL).plusDays(1)
                : LocalDate.now(SEOUL);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        return IntStream.range(0, 5)
                .mapToObj(startDate::plusDays)
                .map(date -> date.format(formatter))
                .toList();
    }

    private List<Weather> refineWeatherInfo(String fcstDate, List<KmaWeatherItem> filtered, int nx, int ny) {

        // fcstDate의 값을 가진 데이터만 필터링
        List<KmaWeatherItem> filteredDate = filterByForecastDate(fcstDate, filtered);

        // 값이 없는 경우 빈 리스트 반환
        if (filteredDate.isEmpty()) {
            return Collections.emptyList();
        }

        // KEY : 예측 시간 (0800, 0900 ...)
        // VALUE : 카테고리, 값 (WSD, 20)
        Map<String, Map<KmaCategory, String>> groupedByTime = groupByForecastTime(filteredDate);

        // TODO: 최저온도, 최고온도가 null인 경우 어떻게 저장할지 고민해보기
        Double temperatureMin = extractTemperature(groupedByTime, MIN_TEMPERATURE_TIME, KmaCategory.TMN);
        Double temperatureMax = extractTemperature(groupedByTime, MAX_TEMPERATURE_TIME, KmaCategory.TMX);

        // 시간 별로 날씨 엔티티 생성
        return groupedByTime.entrySet().stream()
                .sorted(Map.Entry.comparingByKey()) // 시간순 정렬
                .map(entry -> buildWeather(
                        fcstDate,
                        entry.getKey(),
                        entry.getValue(),
                        temperatureMin,
                        temperatureMax,
                        nx,
                        ny
                ))
                .toList();
    }

    private List<KmaWeatherItem> filterByForecastDate(String fcstDate, List<KmaWeatherItem> items) {
        return items.stream()
                .filter(item -> fcstDate.equals(item.fcstDate()))
                .toList();
    }

    private Map<String, Map<KmaCategory, String>> groupByForecastTime(List<KmaWeatherItem> items) {
        Map<String, Map<KmaCategory, String>> grouped = new HashMap<>();

        for (KmaWeatherItem item : items) {
            KmaCategory category = KmaCategory.from(item.category()).orElse(null);

            if (category == null) {
                log.warn("Unknown KMA category: {}", item.category());
                continue;
            }

            grouped.computeIfAbsent(item.fcstTime(), key -> new EnumMap<>(KmaCategory.class)) // 있으면 key에 해당하는 value를 꺼내고 아니면 생성
                    .put(category, item.fcstValue()); // value 값에 item의 새로운 타입과 값 추가
        }

        return grouped;
    }

    private Double extractTemperature(Map<String, Map<KmaCategory, String>> groupedByTime, String time, KmaCategory category) {
        Map<KmaCategory, String> valuesByTime = groupedByTime.get(time);

        if (valuesByTime == null) {
            return null;
        }

        String value = valuesByTime.get(category);
        if (value == null) {
            return null;
        }

        return Double.parseDouble(value);
    }

    private Weather buildWeather(
            String fcstDate,
            String fcstTime,
            Map<KmaCategory, String> values,
            Double temperatureMin,
            Double temperatureMax,
            int nx,
            int ny
    ) {
        LocalDateTime forecastAt = parseForecastAt(fcstDate, fcstTime);

        double temperatureCurrent = 0;
        double windSpeed = 0;
        SkyStatus skyStatus = SkyStatus.CLEAR;
        PrecipitationType precipitationType = PrecipitationType.NONE;
        double precipitationAmount = 0;
        double precipitationProbability = 0;
        double humidityCurrent = 0;

        for (Map.Entry<KmaCategory, String> entry : values.entrySet()) {
            KmaCategory category = entry.getKey();
            String rawValue = entry.getValue();

            switch (category) {
                case TMP -> temperatureCurrent = Double.parseDouble(rawValue);
                case WSD -> windSpeed = Double.parseDouble(rawValue);
                case SKY -> skyStatus = SkyStatus.from(Integer.parseInt(rawValue));
                case PTY -> precipitationType = PrecipitationType.from(Integer.parseInt(rawValue));
                case PCP -> precipitationAmount = parsePcpToApproximateAmount(rawValue);
                case POP -> precipitationProbability = Double.parseDouble(rawValue);
                case REH -> humidityCurrent = Double.parseDouble(rawValue);
            }
        }

        WindAsWord windAsWord = WindAsWord.from(windSpeed);

        return new Weather(
                LocalDate.now(SEOUL).atStartOfDay(),
                forecastAt,
                nx,
                ny,
                temperatureCurrent,
                temperatureMax,
                temperatureMin,
                windSpeed,
                windAsWord,
                skyStatus,
                precipitationType,
                precipitationAmount,
                precipitationProbability,
                humidityCurrent
        );
    }

    private LocalDateTime parseForecastAt(String fcstDate, String fcstTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
        return LocalDateTime.parse(fcstDate + fcstTime, formatter);
    }

    // 기상청 강수량 문자열을 근사값(double)으로 변환
    private double parsePcpToApproximateAmount(String value) {
        if (value == null || value.equals("강수없음") || value.equals("0")) {
            return 0.0;
        }

        value = value.trim();

        // 1mm 미만
        if (value.equals("1mm 미만")) {
            return 1.0;
        }

        // ~ 범위 (예: 10~19mm, 30~50mm)
        if (value.contains("~")) {
            String cleaned = value.replace("mm", "");
            String[] parts = cleaned.split("~");

            double min = Double.parseDouble(parts[0]);
            double max = Double.parseDouble(parts[1]);

            return (min + max) / 2.0;
        }

        // 이상 (예: 50mm 이상)
        if (value.contains("이상")) {
            return Double.parseDouble(value.replace("mm 이상", ""));
        }

        // 일반 숫자 (예: 5mm)
        return Double.parseDouble(value.replace("mm", ""));
    }

    // 새로운 지역에서 날씨를 조회한 경우 어제 온도, 습도 저장을 위한 메서드
    public List<YesterdayHourlyWeather> toYesterdayWeathers(int nx, int ny, List<KmaWeatherItem> items) {

        // 필요한 정보만 필터링
        List<KmaWeatherItem> filtered = items.stream()
                .filter(item -> KmaCategory.from(item.category())
                        .map(YESTERDAY_CATEGORIES::contains)
                        .orElse(false))
                .toList();

        LocalDate yesterday = LocalDate.now(SEOUL).minusDays(1);
        String fcstDate = yesterday.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        return refineYesterdayWeatherInfo(yesterday, fcstDate, filtered, nx, ny).stream()
                .sorted(Comparator.comparing(YesterdayHourlyWeather::getHour))
                .toList();
    }

    private List<YesterdayHourlyWeather> refineYesterdayWeatherInfo(
            LocalDate yesterday,
            String fcstDate,
            List<KmaWeatherItem> filtered,
            int nx,
            int ny
    ) {
        List<KmaWeatherItem> filteredDate = filterByForecastDate(fcstDate, filtered);

        if (filteredDate.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, Map<KmaCategory, String>> groupedByTime = groupByForecastTime(filteredDate);

        return groupedByTime.entrySet().stream()
                .map(entry -> buildYesterdayHourlyWeather(
                        yesterday,
                        entry.getKey(),
                        entry.getValue(),
                        nx,
                        ny
                ))
                .toList();
    }

    private YesterdayHourlyWeather buildYesterdayHourlyWeather(
            LocalDate date,
            String fcstTime,
            Map<KmaCategory, String> values,
            int nx,
            int ny
    ) {
        double temperatureCurrent = 0;
        double humidityCurrent = 0;

        for (Map.Entry<KmaCategory, String> entry : values.entrySet()) {
            switch (entry.getKey()) {
                case TMP -> temperatureCurrent = Double.parseDouble(entry.getValue());
                case REH -> humidityCurrent = Double.parseDouble(entry.getValue());
            }
        }

        LocalTime hour = LocalTime.parse(fcstTime, DateTimeFormatter.ofPattern("HHmm"));

        return new YesterdayHourlyWeather(
                nx,
                ny,
                date,
                hour,
                temperatureCurrent,
                humidityCurrent
        );
    }
}
