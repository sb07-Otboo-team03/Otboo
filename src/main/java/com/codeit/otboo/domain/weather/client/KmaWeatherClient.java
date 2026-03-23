package com.codeit.otboo.domain.weather.client;

import com.codeit.otboo.domain.weather.client.dto.KmaWeatherApiResponse;
import com.codeit.otboo.domain.weather.client.dto.KmaWeatherItem;
import com.codeit.otboo.domain.weather.entity.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class KmaWeatherClient {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final String KMA_API_KEY = System.getenv("KMA_API_KEY");
    private static final String BASE_URL = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst";

    public static String callWeatherApi(String baseDate, String baseTime, int nx, int ny, int numOfRows) {
        try {
            StringBuilder urlBuilder = new StringBuilder(BASE_URL);

            urlBuilder.append("?serviceKey=").append(URLEncoder.encode(KMA_API_KEY, "UTF-8"));
            urlBuilder.append("&numOfRows=").append(numOfRows);
            urlBuilder.append("&pageNo=1");
            urlBuilder.append("&dataType=JSON");
            urlBuilder.append("&base_date=").append(baseDate);
            urlBuilder.append("&base_time=").append(baseTime);
            urlBuilder.append("&nx=").append(nx);
            urlBuilder.append("&ny=").append(ny);

            URL url = new URL(urlBuilder.toString());

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader rd;
            if (conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
                rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            } else {
                rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            }

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }

            rd.close();
            conn.disconnect();

            return sb.toString();

        } catch (Exception e) {
            throw new RuntimeException("기상청 API 호출 실패", e);
        }
    }

    public static List<List<Weather>> getWeathers(String baseTime, int nx, int ny, String json, boolean isScheduling) {
        try {
            // json 정보 정제
            KmaWeatherApiResponse result = objectMapper.readValue(json, KmaWeatherApiResponse.class);
            List<KmaWeatherItem> items = result.response().body().items().item();

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

            // 날짜 별로 분리
            String fcstDate1, fcstDate2, fcstDate3, fcstDate4, fcstDate5;

            ZoneId seoul = ZoneId.of("Asia/Seoul");

            if(baseTime.equals("2300") && isScheduling) {
                fcstDate1 = LocalDate.now(seoul).plusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                fcstDate2 = LocalDate.now(seoul).plusDays(2).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                fcstDate3 = LocalDate.now(seoul).plusDays(3).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                fcstDate4 = LocalDate.now(seoul).plusDays(4).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                fcstDate5 = LocalDate.now(seoul).plusDays(5).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            } else {
                fcstDate1 = LocalDate.now(seoul).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                fcstDate2 = LocalDate.now(seoul).plusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                fcstDate3 = LocalDate.now(seoul).plusDays(2).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                fcstDate4 = LocalDate.now(seoul).plusDays(3).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                fcstDate5 = LocalDate.now(seoul).plusDays(4).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            }

            // 날짜 별로 데이터 정제
            List<Weather> weatherDate1 = refineWeatherInfo(fcstDate1, filtered, nx, ny);
            List<Weather> weatherDate2 = refineWeatherInfo(fcstDate2, filtered, nx, ny);
            List<Weather> weatherDate3 = refineWeatherInfo(fcstDate3, filtered, nx, ny);
            List<Weather> weatherDate4 = refineWeatherInfo(fcstDate4, filtered, nx, ny);
            List<Weather> weatherDate5 = refineWeatherInfo(fcstDate5, filtered, nx, ny);

            List<List<Weather>> weathers = new ArrayList<>();
            weathers.add(weatherDate1);
            weathers.add(weatherDate2);
            weathers.add(weatherDate3);
            weathers.add(weatherDate4);
            weathers.add(weatherDate5);
            weathers.forEach(list -> list.sort(Comparator.comparing(Weather::getForecastedAt))); // 예보 시간 순으로 정렬

            return weathers;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<Weather> refineWeatherInfo(String fcstDate, List<KmaWeatherItem> filtered, int nx, int ny) {

        // fcstDate의 값을 가진 데이터만 필터링
        List<KmaWeatherItem> filteredDate = filtered.stream()
                .filter(i -> fcstDate.equals(i.fcstDate()))
                .toList();

        // 값이 없는 경우 빈 리스트 반환
        if (filteredDate.isEmpty()) {
            return Collections.emptyList();
        }

        // KEY : 예측 시간 (0800, 0900 ...)
        // VALUE : 카테고리, 값 (WSD, 20)
        Map<String, Map<String, String>> grouped = new HashMap<>();

        for (KmaWeatherItem item : filteredDate) {
            String key = item.fcstTime(); // 예측 시간을 키로 지정
            Map<String, String> value = grouped.getOrDefault(key, new HashMap<>());
            value.put(item.category(), item.fcstValue()); // 카테고리와 값을 value로 지정
            grouped.put(key, value);
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
        List<Weather> weathers = new ArrayList<>();

        // 시간 별로 날씨 엔티티 생성
        for (String key : grouped.keySet()) {
            Weather weather = new Weather(nx, ny);

            String dateTimeStr = fcstDate + key;
            LocalDateTime forecastAt = LocalDateTime.parse(dateTimeStr, formatter);

            double temperatureCurrent = 0;
            Double temperatureMax = null;
            Double temperatureMin = null;
            double windSpeed = 0;
            WindAsWord windAsWord;
            SkyStatus skyStatus = SkyStatus.CLEAR;
            PrecipitationType precipitationType = PrecipitationType.NONE;
            double precipitationAmount = 0;
            double precipitationProbability = 0;
            double humidityCurrent = 0;

            for (Map.Entry<String, String> entry : grouped.get(key).entrySet()) {
                switch (entry.getKey()) {
                    case "TMP" -> temperatureCurrent = Double.parseDouble(entry.getValue());
                    case "WSD" -> windSpeed = Double.parseDouble(entry.getValue());
                    case "SKY" -> skyStatus = SkyStatus.from(Integer.parseInt(entry.getValue()));
                    case "PTY" -> precipitationType = PrecipitationType.from(Integer.parseInt(entry.getValue()));
                    case "PCP" -> precipitationAmount = parsePcpDouble(entry.getValue());
                    case "POP" -> precipitationProbability = Double.parseDouble(entry.getValue());
                    case "REH" -> humidityCurrent = Double.parseDouble(entry.getValue());
                }
            }

            // TODO: 최저온도, 최고온도가 null인 경우 어떻게 저장할지 고민해보기

            // 0600에 최저 온도 값을 가짐
            // 특정 발표 시간대에 값을 가지고 있지 않은 경우 존재
            if (grouped.get("0600") != null) {
                if (grouped.get("0600").get("TMN") != null) {
                    temperatureMin = Double.parseDouble(grouped.get("0600").get("TMN"));
                }
            }

            // 1500에 최고 온도 값을 가짐
            if (grouped.get("1500") != null) {
                if (grouped.get("1500").get("TMX") != null) {
                    temperatureMax = Double.parseDouble(grouped.get("1500").get("TMX"));
                }
            }
            windAsWord = WindAsWord.from(windSpeed);

            weather.update(
                    LocalDate.now().atStartOfDay(),
                    forecastAt,
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

            weathers.add(weather);
        }

        return weathers;
    }

    private static double parsePcpDouble(String value) {
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
    public static List<YesterdayHourlyWeather> getYesterdayWeathers(int nx, int ny, String json) {
        try {
            // json 정보 정제
            KmaWeatherApiResponse result = objectMapper.readValue(json, KmaWeatherApiResponse.class);
            List<KmaWeatherItem> items = result.response().body().items().item();

            // 필요한 정보만 필터링
            List<KmaWeatherItem> filtered = items.stream()
                    .filter(i -> {
                        String c = i.category();
                        return  "REH".equals(c) || // 습도
                                "TMP".equals(c);   // 1시간 기온
                    })
                    .toList();

            ZoneId seoul = ZoneId.of("Asia/Seoul");
            String fcstDate = LocalDate.now(seoul).minusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd"));

            List<YesterdayHourlyWeather> weathers = refineYesterdayWeatherInfo(fcstDate, filtered, nx, ny);

            weathers.sort(Comparator.comparing(YesterdayHourlyWeather::getHour)); // 예보 시간 순으로 정렬

            return weathers;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<YesterdayHourlyWeather> refineYesterdayWeatherInfo(String fcstDate, List<KmaWeatherItem> filtered, int nx, int ny) {
        // fcstDate의 값을 가진 데이터만 필터링
        List<KmaWeatherItem> filteredDate = filtered.stream()
                .filter(i -> fcstDate.equals(i.fcstDate()))
                .toList();

        // 값이 없는 경우 빈 리스트 반환
        if (filteredDate.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, Map<String, String>> grouped = new HashMap<>();

        for (KmaWeatherItem item : filteredDate) {
            String key = item.fcstTime(); // 예측 시간을 키로 지정
            Map<String, String> value = grouped.getOrDefault(key, new HashMap<>());
            value.put(item.category(), item.fcstValue()); // 카테고리와 값을 value로 지정
            grouped.put(key, value);
        }

        List<YesterdayHourlyWeather> yesterdayHourlyWeathers = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HHmm");

        for (String key : grouped.keySet()) {

            double temperatureCurrent = 0;
            double humidityCurrent = 0;

            for (Map.Entry<String, String> entry : grouped.get(key).entrySet()) {
                switch (entry.getKey()) {
                    case "TMP" -> temperatureCurrent = Double.parseDouble(entry.getValue());
                    case "REH" -> humidityCurrent = Double.parseDouble(entry.getValue());
                }
            }

            LocalTime hour = LocalTime.parse(key, formatter);

            YesterdayHourlyWeather yesterdayHourlyWeather = new YesterdayHourlyWeather(
                    nx,
                    ny,
                    LocalDate.now().minusDays(1),
                    hour,
                    temperatureCurrent,
                    humidityCurrent
            );

            yesterdayHourlyWeathers.add(yesterdayHourlyWeather);
        }

        return yesterdayHourlyWeathers;
    }
}
