package com.codeit.otboo.domain.weather.client;

import com.codeit.otboo.domain.weather.client.dto.KmaHeader;
import com.codeit.otboo.domain.weather.client.dto.KmaWeatherApiResponse;
import com.codeit.otboo.domain.weather.client.dto.KmaWeatherItem;
import com.codeit.otboo.domain.weather.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.IntStream;

@Component
@RequiredArgsConstructor
public class KmaWeatherClient {

    private final RestClient restClient;

    @Value("${kma.api.key}")
    private String kmaApiKey;

    public List<KmaWeatherItem> callWeatherApi(String baseDate, String baseTime, int nx, int ny, int numOfRows) {
        KmaWeatherApiResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("http")
                        .host("apis.data.go.kr")
                        .path("/1360000/VilageFcstInfoService_2.0/getVilageFcst")
                        .queryParam("serviceKey", kmaApiKey)
                        .queryParam("numOfRows", numOfRows)
                        .queryParam("pageNo", 1)
                        .queryParam("dataType", "JSON")
                        .queryParam("base_date", baseDate)
                        .queryParam("base_time", baseTime)
                        .queryParam("nx", nx)
                        .queryParam("ny", ny)
                        .build())
                .retrieve()
                .body(KmaWeatherApiResponse.class);

        return parseItems(response);
    }

    private List<KmaWeatherItem> parseItems(KmaWeatherApiResponse response) {
        if (response == null || response.response() == null || response.response().header() == null) {
            throw new IllegalStateException("기상청 API 응답 헤더가 없습니다.");
        }

        KmaHeader header = response.response().header();
        if (!"00".equals(header.resultCode())) {
            throw new IllegalStateException(
                    "기상청 API 오류 - resultCode: " + header.resultCode()
                            + ", resultMsg: " + header.resultMsg()
            );
        }

        if (response.response().body() == null) {
            throw new IllegalStateException("기상청 응답(body)이 없습니다.");
        }

        if (response.response().body().items() == null || response.response().body().items().item() == null) {
            return Collections.emptyList();
        }

        return response.response().body().items().item();
    }

    public List<Weather> getWeathers(String baseTime, int nx, int ny, List<KmaWeatherItem> items, boolean isScheduling) {

        // 각 날짜 별로 TMX, TMN을 최고, 최저 기온에 넣어가지고 전달?
        // 업데이트하는거도 생각해서 해야됨.

        // 필요한 정보만 필터링
        List<KmaWeatherItem> filtered = items.stream()
                .filter(i -> {
                    String c = i.category();
                    return "POP".equals(c) || // 강수 확률
                            "PCP".equals(c) || // 1시간 강수량
                            "PTY".equals(c) || // 강수 형태
                            "REH".equals(c) || // 습도
                            "SKY".equals(c) || // 하늘 상태
                            "TMP".equals(c) || // 1시간 기온
                            "TMX".equals(c) || // 일 최고 기온
                            "TMN".equals(c) || // 일 최저 기온
                            "WSD".equals(c); // 풍속
                })
                .toList();

        List<String> forecastDates = resolveForecastDates(baseTime, isScheduling);

        // 날짜 별로 데이터 정제
        List<Weather> weathers = forecastDates.stream()
                .flatMap(date -> refineWeatherInfo(date, filtered, nx, ny).stream())
                .toList();

        return weathers;
    }

    private List<String> resolveForecastDates(String baseTime, boolean isScheduling) {
        ZoneId seoul = ZoneId.of("Asia/Seoul");
        LocalDate startDate = ("2300".equals(baseTime) && isScheduling)
                ? LocalDate.now(seoul).plusDays(1)
                : LocalDate.now(seoul);

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
        Map<String, Map<String, String>> groupedByTime = groupByForecastTime(filteredDate);

        // TODO: 최저온도, 최고온도가 null인 경우 어떻게 저장할지 고민해보기
        Double temperatureMin = extractTemperature(groupedByTime, "0600", "TMN");
        Double temperatureMax = extractTemperature(groupedByTime, "1500", "TMX");

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

    private Map<String, Map<String, String>> groupByForecastTime(List<KmaWeatherItem> items) {
        Map<String, Map<String, String>> grouped = new HashMap<>();

        for (KmaWeatherItem item : items) {
            grouped
                    .computeIfAbsent(item.fcstTime(), key -> new HashMap<>()) // 있으면 key에 해당하는 value를 꺼내고 아니면 생성
                    .put(item.category(), item.fcstValue()); // value 값에 item의 새로운 타입과 값 추가
        }

        return grouped;
    }

    private Double extractTemperature(Map<String, Map<String, String>> groupedByTime, String time, String category) {
        Map<String, String> valuesByTime = groupedByTime.get(time);

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
            Map<String, String> values,
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

        for (Map.Entry<String, String> entry : values.entrySet()) {
            String category = entry.getKey();
            String rawValue = entry.getValue();

            switch (category) {
                case "TMP" -> temperatureCurrent = Double.parseDouble(rawValue);
                case "WSD" -> windSpeed = Double.parseDouble(rawValue);
                case "SKY" -> skyStatus = SkyStatus.from(Integer.parseInt(rawValue));
                case "PTY" -> precipitationType = PrecipitationType.from(Integer.parseInt(rawValue));
                case "PCP" -> precipitationAmount = parsePcpToApproximateAmount(rawValue);
                case "POP" -> precipitationProbability = Double.parseDouble(rawValue);
                case "REH" -> humidityCurrent = Double.parseDouble(rawValue);
            }
        }

        WindAsWord windAsWord = WindAsWord.from(windSpeed);

        return new Weather(
                LocalDate.now().atStartOfDay(),
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
    public List<YesterdayHourlyWeather> getYesterdayWeathers(int nx, int ny, List<KmaWeatherItem> items) {

        // 필요한 정보만 필터링
        List<KmaWeatherItem> filtered = items.stream()
                .filter(i -> {
                    String c = i.category();
                    return  "REH".equals(c) || // 습도
                            "TMP".equals(c);   // 1시간 기온
                })
                .toList();

        ZoneId seoul = ZoneId.of("Asia/Seoul");
        LocalDate yesterday = LocalDate.now(seoul).minusDays(1);
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

        Map<String, Map<String, String>> groupedByTime = groupByForecastTime(filteredDate);

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
            Map<String, String> values,
            int nx,
            int ny
    ) {
        double temperatureCurrent = 0;
        double humidityCurrent = 0;

        for (Map.Entry<String, String> entry : values.entrySet()) {
            switch (entry.getKey()) {
                case "TMP" -> temperatureCurrent = Double.parseDouble(entry.getValue());
                case "REH" -> humidityCurrent = Double.parseDouble(entry.getValue());
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
