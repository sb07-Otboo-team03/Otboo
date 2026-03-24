package com.codeit.otboo.domain.weather.service;

import com.codeit.otboo.domain.weather.client.KmaWeatherClient;
import com.codeit.otboo.domain.weather.client.KmaWeatherMapper;
import com.codeit.otboo.domain.weather.client.dto.KmaWeatherItem;
import com.codeit.otboo.domain.weather.dto.mapper.WeatherMapper;
import com.codeit.otboo.domain.weather.dto.response.WeatherAPILocationResponse;
import com.codeit.otboo.domain.weather.dto.response.WeatherResponse;
import com.codeit.otboo.domain.weather.entity.LocationNameMap;
import com.codeit.otboo.domain.weather.entity.Weather;
import com.codeit.otboo.domain.weather.entity.YesterdayHourlyWeather;
import com.codeit.otboo.domain.weather.repository.LocationNameMapRepository;
import com.codeit.otboo.domain.weather.repository.WeatherRepository;
import com.codeit.otboo.domain.weather.repository.YesterdayHourlyWeatherRepository;
import com.codeit.otboo.global.util.KakaoLocalUtil;
import com.codeit.otboo.global.util.KakaoLocalUtil.KakaoRegionType;
import com.codeit.otboo.global.util.KmaGridConverter;
import com.codeit.otboo.global.util.KmaGridConverter.GridResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeatherServiceImpl implements WeatherService{

    private final WeatherRepository weatherRepository;
    private final YesterdayHourlyWeatherRepository yesterdayHourlyWeatherRepository;
    private final LocationNameMapRepository locationNameMapRepository;

    private final WeatherMapper weatherMapper;

    private final KmaWeatherClient kmaWeatherClient;
    private final KmaWeatherMapper kmaWeatherMapper;

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    @Override
    @Transactional
    public List<WeatherResponse> getAll(double longitude, double latitude) {

        // 1. 위도, 경도로 지역명 조회
        LocationNameMap location = locationNameMapRepository.findByLongitudeAndLatitude(longitude, latitude)
                .orElse(null);

        // 레포지토리에 저장되어 있지 않은 경우 KAKAO API를 사용
        // 레포지토리에 위도, 경도와 지역명을 매핑한 데이터 저장
        if (location == null) {
            GridResult gridResult = KmaGridConverter.convertToGrid(latitude, longitude);
            List<String> addressLevels = KakaoLocalUtil.getAddressLevels(longitude, latitude, KakaoRegionType.H);

            LocationNameMap locationNameMap = new LocationNameMap(
                    gridResult.nx(),
                    gridResult.ny(),
                    latitude,
                    longitude,
                    addressLevels.get(0),
                    addressLevels.get(1),
                    addressLevels.get(2),
                    addressLevels.get(3)
            );

            location = locationNameMapRepository.save(locationNameMap);
        }

        LocalDateTime forecastedAt = LocalDate.now(SEOUL).atStartOfDay(); // 저장 날짜
        LocalDateTime forecastAt = LocalDateTime.now(SEOUL) // 날씨 조회 시간
                .withMinute(0)
                .withSecond(0)
                .withNano(0);

        List<Weather> weathers = new ArrayList<>();
        addWeathers(forecastedAt, forecastAt, location, weathers); // 조회 시간에 맞추어 (오늘, 내일, 모레, 4일뒤, 5일뒤) 데이터 조회 및 리스트에 추가

        int x = location.getX();
        int y = location.getY();

        if (weathers.isEmpty()) {
            insertNewLocationWeather(x, y); // DB에 날씨 데이터 추가
            addWeathers(forecastedAt, forecastAt, location, weathers);
        }

        // TODO: +5일뒤 데이터를 어떻게 저장할지 고민해봐야함.
        // TODO: 00시 데이터만 가진 날짜는 어떻게 전달할지 고민해봐야함.
        // 1400까지 +3일뒤 데이터는 3시간 간격 + 4일뒤 00시 데이터만 존재
        // 1700부터 +4일뒤 데이터는 3시간 간격 + 5일뒤 00시 데이터 추가

        if (weathers.size() == 3) { // 1400 발표까지
            findClosestWeather(forecastAt, location, weathers, forecastedAt, weathers.size());
        } else if (forecastAt.getHour() >= 17 && weathers.size() == 4) { // 1700발표 이후
            findClosestWeather(forecastAt, location, weathers, forecastedAt, weathers.size());
        }

        // 어제 온도, 습도 정보 조회
        YesterdayHourlyWeather yesterdayHourlyWeather = yesterdayHourlyWeatherRepository.findByDateAndHour(
                        LocalDate.now(SEOUL).minusDays(1),
                        LocalTime.now(SEOUL).withMinute(0).withSecond(0).withNano(0))
                .orElseGet(() -> { // 없으면 새로 저장하고 조회
                    addYesterdayWeatherInfo(x, y);
                    return yesterdayHourlyWeatherRepository.findByDateAndHour(
                            LocalDate.now(SEOUL).minusDays(1),
                            LocalTime.now(SEOUL).withMinute(0).withSecond(0).withNano(0)
                    ).orElseThrow(() -> new RuntimeException("YesterdayHourlyWeather is not found"));
                });


        return weatherMapper.toDto(weathers, location, yesterdayHourlyWeather);
    }

    private void insertNewLocationWeather(int x, int y) {
        String baseDate = LocalDate.now(SEOUL).minusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String baseTime = "2300";
        saveOrUpdateWeather(baseDate, baseTime, x, y, false);
    }

    private void addYesterdayWeatherInfo(int x, int y) {

        // 어제 정보를 00시 ~ 23시 전부 불러오기 위해 2일 전 23시 발표 정보 조회
        String baseDate = LocalDate.now(SEOUL).minusDays(2).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String baseTime = "2300";

        List<KmaWeatherItem> items = kmaWeatherClient.callWeatherApi(baseDate, baseTime, x, y, 300);
        List<YesterdayHourlyWeather> yesterdayWeathers = kmaWeatherMapper.toYesterdayWeathers(x, y, items);

        yesterdayHourlyWeatherRepository.saveAll(yesterdayWeathers);
    }

    private void findClosestWeather(LocalDateTime forecastAt, LocationNameMap location, List<Weather> weathers, LocalDateTime forecastedAt, int size) {
        // 3일 뒤 데이터는 현재 시간과 제일 가까운 시간의 정보 저장
        LocalDateTime forecastAt2 = forecastAt.plusDays(size); // 람다식 사용을 위해 변수 값 복사
        LocalDateTime start = forecastAt.plusDays(size).minusHours(3);
        LocalDateTime end = forecastAt.plusDays(size).plusHours(3);

        Weather weather3 = weatherRepository.findByXAndYAndForecastAtBetween(location.getX(), location.getY(), start, end).stream()
                .min(Comparator.comparing(w -> Math.abs(Duration.between(w.getForecastAt(), forecastAt2).toMinutes())))
                .orElseThrow(() -> new IllegalArgumentException("weather is not found"));

        weathers.add(weather3);
    }

    private void addWeathers(LocalDateTime forecastedAt, LocalDateTime forecastAt, LocationNameMap location, List<Weather> weathers) {
        for (int i = 0; i < 5; i++) {
            weatherRepository.findByForecastedAtAndForecastAtAndXAndY(
                            forecastedAt,
                            forecastAt.plusDays(i),
                            location.getX(),
                            location.getY())
                    .ifPresent(weathers::add);
        }
    }

    @Transactional(readOnly = true)
    public WeatherAPILocationResponse getLocation(double longitude, double latitude) {

        GridResult gridResult = KmaGridConverter.convertToGrid(latitude, longitude);

        return new WeatherAPILocationResponse(
                latitude,
                longitude,
                gridResult.nx(),
                gridResult.ny(),
                KakaoLocalUtil.getAddressLevels(longitude, latitude, KakaoRegionType.H)
        );
    }

    // 3시간 단위 기상 발표에 맞추어 스케줄링
    @Scheduled(cron = "0 15 2,5,8,11,14,17,20,23 * * *")
    @Transactional
    public void updateCurrentWeather() {
        List<LocationNameMap> locations = locationNameMapRepository.findAll();

        for(LocationNameMap location : locations) {
            updateWeather(location.getX(), location.getY());
        }
    }

    public void updateWeather(int x, int y) {
        String baseDate = LocalDate.now(SEOUL).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String baseTime = getBaseTimeForWeather();
        saveOrUpdateWeather(baseDate, baseTime, x, y, true);
    }

    private void saveOrUpdateWeather(String baseDate, String baseTime, int x, int y, boolean isScheduling) {
        List<KmaWeatherItem> items = kmaWeatherClient.callWeatherApi(baseDate, baseTime, x, y, 1052);
        List<Weather> weathers = kmaWeatherMapper.toWeathers(baseTime, x, y, items, isScheduling);

        List<Weather> weatherList = new ArrayList<>();

        for (Weather w : weathers) {
            Weather savedWeather = weatherRepository.findByForecastedAtAndForecastAtAndXAndY(
                    w.getForecastedAt(),
                    w.getForecastAt(),
                    w.getX(),
                    w.getY()
            ).orElse(null);

            if (savedWeather != null) {
                log.debug("존재하는 날씨 정보를 업데이트 합니다. ForecastedAt = {}, ForecastAt = {}",
                        w.getForecastedAt(), w.getForecastAt());

                savedWeather.update(w);
                weatherList.add(savedWeather);
            } else {
                weatherList.add(w);
            }
        }

        weatherList.sort(
                Comparator.comparing(Weather::getForecastAt)
                        .thenComparing(Weather::getForecastedAt)
        );

        weatherRepository.saveAll(weatherList);
    }

    public static String getBaseTimeForWeather() {
        int hour = LocalTime.now(SEOUL).getHour();

        if (hour < 2) return "2300";
        else if (hour < 5) return "0200";
        else if (hour < 8) return "0500";
        else if (hour < 11) return "0800";
        else if (hour < 14) return "1100";
        else if (hour < 17) return "1400";
        else if (hour < 20) return "1700";
        else if (hour < 23) return "2000";
        else return "2300";
    }
}
