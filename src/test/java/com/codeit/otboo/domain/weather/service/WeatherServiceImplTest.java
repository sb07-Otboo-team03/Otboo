package com.codeit.otboo.domain.weather.service;

import com.codeit.otboo.domain.weather.batch.dto.ForecastBatchResult;
import com.codeit.otboo.domain.weather.dto.response.WeatherAPILocationResponse;
import com.codeit.otboo.domain.weather.exception.KmaApiErrorException;
import com.codeit.otboo.global.util.KakaoLocalUtil;
import com.codeit.otboo.domain.weather.client.KmaWeatherClient;
import com.codeit.otboo.domain.weather.client.dto.KmaWeatherItem;
import com.codeit.otboo.domain.weather.dto.response.WeatherResponse;
import com.codeit.otboo.domain.weather.entity.LocationNameMap;
import com.codeit.otboo.domain.weather.entity.Weather;
import com.codeit.otboo.domain.weather.entity.YesterdayHourlyWeather;
import com.codeit.otboo.domain.weather.client.KmaWeatherMapper;
import com.codeit.otboo.domain.weather.dto.mapper.WeatherMapper;
import com.codeit.otboo.domain.weather.repository.LocationNameMapRepository;
import com.codeit.otboo.domain.weather.repository.WeatherRepository;
import com.codeit.otboo.domain.weather.repository.YesterdayHourlyWeatherRepository;
import com.codeit.otboo.global.util.KakaoLocalUtil.KakaoRegionType;
import com.codeit.otboo.global.util.KmaGridConverter;
import com.codeit.otboo.global.util.KmaGridConverter.GridResult;
import com.codeit.otboo.global.util.TimeProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("날씨 서비스 단위 테스트")
class WeatherServiceImplTest {

    @InjectMocks
    private WeatherServiceImpl weatherService;

    @Mock
    private WeatherForecastUpsertService weatherForecastUpsertService;
    @Mock
    private WeatherRedisCacheService weatherRedisCacheService;
    @Mock
    private WeatherRepository weatherRepository;
    @Mock
    private YesterdayHourlyWeatherRepository yesterdayHourlyWeatherRepository;
    @Mock
    private LocationNameMapRepository locationNameMapRepository;
    @Mock
    private WeatherMapper weatherMapper;
    @Mock
    private KmaWeatherClient kmaWeatherClient;
    @Mock
    private KmaWeatherMapper kmaWeatherMapper;
    @Mock
    private TimeProvider timeProvider;
    @Mock
    private KmaGridConverter kmaGridConverter;
    @Mock
    private KakaoLocalUtil kakaoLocalUtil;

    private LocalDateTime setFixedTime(int year, int month, int day, int hour, int minute) {
        LocalDateTime fixedNow = LocalDateTime.of(year, month, day, hour, minute);
        when(timeProvider.nowDateTime()).thenReturn(fixedNow);
        when(timeProvider.nowDate()).thenReturn(fixedNow.toLocalDate());
        when(timeProvider.nowTime()).thenReturn(fixedNow.toLocalTime());
        return fixedNow;
    }

    @Nested
    @DisplayName("getAll()")
    class GetAllTest {

        @SuppressWarnings("unchecked")
        private void mockCachePassThrough(int x, int y, LocalDateTime forecastAt) {
            when(weatherRedisCacheService.getOrLoad(
                    eq(x),
                    eq(y),
                    eq(forecastAt),
                    any()
            )).thenAnswer(invocation -> {
                Supplier<List<WeatherResponse>> loader = invocation.getArgument(3);
                return loader.get();
            });
        }

        @Test
        @DisplayName("최초 조회 시 지역 정보와 날씨 정보를 저장하고 날씨를 반환한다")
        void getAll_savesLocationAndWeather_whenLocationNotExists() {
            //given
            double longitude = 126.8216;
            double latitude = 37.5295;

            int x = 57;
            int y = 126;

            when(locationNameMapRepository.findByLongitudeAndLatitude(longitude, latitude))
                    .thenReturn(Optional.empty());

            LocationNameMap savedLocation = mock(LocationNameMap.class);
            when(locationNameMapRepository.save(any(LocationNameMap.class)))
                    .thenReturn(savedLocation);

            LocalDateTime fixedNow = setFixedTime(2026, 3, 20, 11, 0);
            LocalDateTime forecastedAt = fixedNow.toLocalDate().atStartOfDay();
            LocalDateTime forecastAt = fixedNow.withMinute(0).withSecond(0).withNano(0);

            // Grid & 지역 정보
            when(kmaGridConverter.convertToGrid(latitude, longitude))
                    .thenReturn(new GridResult(x, y));

            when(kakaoLocalUtil.getAddressLevels(longitude, latitude, KakaoRegionType.H))
                    .thenReturn(List.of("경기도", "부천시 오정구", "고강본동", ""));

            List<LocalDateTime> targetTimes = IntStream.range(0, 4)
                    .mapToObj(forecastAt::plusDays)
                    .toList();

            List<Weather> loadedWeathers = List.of(
                    mock(Weather.class),
                    mock(Weather.class),
                    mock(Weather.class),
                    mock(Weather.class)
            );

            when(weatherRepository.findTargetWeathers(x, y, forecastedAt, targetTimes))
                    .thenReturn(List.of())          // 첫 조회: 없음
                    .thenReturn(loadedWeathers);    // 저장 후 재조회: 있음

            when(savedLocation.getX()).thenReturn(x);
            when(savedLocation.getY()).thenReturn(y);

            mockCachePassThrough(x, y, forecastAt);

            List<KmaWeatherItem> items = List.of(mock(KmaWeatherItem.class));
            List<Weather> mappedWeathers = List.of(mock(Weather.class));

            when(kmaWeatherClient.callWeatherApi(anyString(), eq("2300"), eq(x), eq(y), eq(1052)))
                    .thenReturn(items);
            when(kmaWeatherMapper.toWeathers(eq("2300"), eq(x), eq(y), eq(items), eq(false)))
                    .thenReturn(mappedWeathers);

            // 어제 날씨
            YesterdayHourlyWeather yesterdayHourlyWeather = mock(YesterdayHourlyWeather.class);
            when(yesterdayHourlyWeatherRepository.findByXAndYAndDateAndHour(any(), any(), any(), any()))
                    .thenReturn(Optional.of(yesterdayHourlyWeather));

            // DTO 변환
            List<WeatherResponse> expected = List.of(mock(WeatherResponse.class));
            when(weatherMapper.toDto(anyList(), eq(savedLocation), eq(yesterdayHourlyWeather)))
                    .thenReturn(expected);

            // when
            List<WeatherResponse> result = weatherService.getAll(longitude, latitude);

            // then
            assertThat(result).isEqualTo(expected);

            verify(kmaGridConverter, times(1)).convertToGrid(latitude, longitude);
            verify(kakaoLocalUtil, times(1)).getAddressLevels(longitude, latitude, KakaoRegionType.H);

            verify(locationNameMapRepository, times(1)).findByLongitudeAndLatitude(longitude, latitude);
            verify(locationNameMapRepository, times(1)).save(any(LocationNameMap.class));

            verify(weatherRedisCacheService).getOrLoad(eq(x), eq(y), eq(forecastAt), any());

            verify(weatherRepository, times(2)).findTargetWeathers(x, y, forecastedAt, targetTimes);

            verify(kmaWeatherClient, times(1)).callWeatherApi(anyString(), eq("2300"), eq(x), eq(y), eq(1052));
            verify(kmaWeatherMapper, times(1)).toWeathers(eq("2300"), eq(x), eq(y), eq(items), eq(false));
            verify(weatherForecastUpsertService, times(1)).upsert(eq(mappedWeathers));
            verify(yesterdayHourlyWeatherRepository, times(1)).findByXAndYAndDateAndHour(any(), any(), any(), any());
            verify(weatherMapper).toDto(anyList(), eq(savedLocation), eq(yesterdayHourlyWeather));
        }

        @Test
        @DisplayName("지역 정보는 있고 날씨 정보가 없으면 API 호출 후 저장하고 반환한다")
        void getAll_savesWeather_whenLocationExistsButWeatherNotExists() {
            //given
            double longitude = 126.8216;
            double latitude = 37.5295;

            int x = 57;
            int y = 126;

            LocationNameMap location = mock(LocationNameMap.class);
            when(locationNameMapRepository.findByLongitudeAndLatitude(longitude, latitude))
                    .thenReturn(Optional.of(location));

            LocalDateTime fixedNow = setFixedTime(2026, 3, 20, 11, 0);
            LocalDateTime forecastedAt = fixedNow.toLocalDate().atStartOfDay();
            LocalDateTime forecastAt = fixedNow.withMinute(0).withSecond(0).withNano(0);

            when(location.getX()).thenReturn(x);
            when(location.getY()).thenReturn(y);

            mockCachePassThrough(x, y, forecastAt);

            List<LocalDateTime> targetTimes = IntStream.range(0, 4)
                    .mapToObj(forecastAt::plusDays)
                    .toList();

            List<Weather> loadedWeathers = List.of(
                    mock(Weather.class),
                    mock(Weather.class),
                    mock(Weather.class),
                    mock(Weather.class)
            );

            when(weatherRepository.findTargetWeathers(x, y, forecastedAt, targetTimes))
                    .thenReturn(List.of())          // 첫 조회: 없음
                    .thenReturn(loadedWeathers);    // 저장 후 재조회: 있음

            List<KmaWeatherItem> items = List.of(mock(KmaWeatherItem.class));
            List<Weather> mappedWeathers = List.of(mock(Weather.class));
            when(kmaWeatherClient.callWeatherApi(anyString(), eq("2300"), eq(x), eq(y), eq(1052)))
                    .thenReturn(items);
            when(kmaWeatherMapper.toWeathers(eq("2300"), eq(x), eq(y), eq(items), eq(false)))
                    .thenReturn(mappedWeathers);


            // 어제 습도, 온도 값 저장
            YesterdayHourlyWeather yesterdayHourlyWeather = mock(YesterdayHourlyWeather.class);
            when(yesterdayHourlyWeatherRepository.findByXAndYAndDateAndHour(any(), any(), any(), any()))
                    .thenReturn(Optional.of(yesterdayHourlyWeather));

            // 반환 값 dto 변환
            List<WeatherResponse> expected = List.of(mock(WeatherResponse.class));
            when(weatherMapper.toDto(anyList(), eq(location), eq(yesterdayHourlyWeather)))
                    .thenReturn(expected);

            // when
            List<WeatherResponse> result = weatherService.getAll(longitude, latitude);

            // then
            assertThat(result).isEqualTo(expected);

            verify(locationNameMapRepository, times(1)).findByLongitudeAndLatitude(longitude, latitude);
            verify(locationNameMapRepository, never()).save(any(LocationNameMap.class));

            verify(weatherRedisCacheService).getOrLoad(eq(x), eq(y), eq(forecastAt), any());
            verify(weatherRepository, times(2)).findTargetWeathers(x, y, forecastedAt, targetTimes);

            verify(kmaWeatherClient, times(1)).callWeatherApi(anyString(), eq("2300"), eq(x), eq(y), eq(1052));
            verify(kmaWeatherMapper, times(1)).toWeathers(eq("2300"), eq(x), eq(y), eq(items), eq(false));
            verify(weatherForecastUpsertService, times(1)).upsert(eq(mappedWeathers));

            verify(yesterdayHourlyWeatherRepository, times(1)).findByXAndYAndDateAndHour(any(), any(), any(), any());
            verify(weatherMapper, times(1)).toDto(anyList(), any(LocationNameMap.class), any(YesterdayHourlyWeather.class));
        }

        @Test
        @DisplayName("조회된 날씨가 3개면 가장 가까운 3일 뒤 날씨를 추가한다")
        void getAll_addsClosestWeather_whenWeatherSizeIsThree() {
            // given
            double longitude = 126.8216;
            double latitude = 37.5295;

            int x = 57;
            int y = 126;

            LocationNameMap location = mock(LocationNameMap.class);
            when(location.getX()).thenReturn(x);
            when(location.getY()).thenReturn(y);

            when(locationNameMapRepository.findByLongitudeAndLatitude(longitude, latitude))
                    .thenReturn(Optional.of(location));

            LocalDateTime fixedNow = setFixedTime(2026, 3, 20, 11, 0);
            LocalDateTime forecastedAt = fixedNow.toLocalDate().atStartOfDay();
            LocalDateTime forecastAt = fixedNow.withMinute(0).withSecond(0).withNano(0);

            mockCachePassThrough(x, y, forecastAt);

            // addWeathers() 결과가 정확히 3개가 되도록 설정
            Weather day0Weather = mock(Weather.class);
            Weather day1Weather = mock(Weather.class);
            Weather day2Weather = mock(Weather.class);

            List<LocalDateTime> targetTimes = IntStream.range(0, 4)
                    .mapToObj(forecastAt::plusDays)
                    .toList();

            when(weatherRepository.findTargetWeathers(x, y, forecastedAt, targetTimes))
                    .thenReturn(new ArrayList<>(List.of(day0Weather, day1Weather, day2Weather)));

            // findClosestWeather()에서 사용할 후보들
            LocalDateTime target = forecastAt.plusDays(3);
            LocalDateTime start = target.minusHours(3);
            LocalDateTime end = target.plusHours(3);

            Weather candidateFar = mock(Weather.class);
            Weather candidateClosest = mock(Weather.class);

            when(candidateFar.getForecastAt()).thenReturn(target.minusHours(2));
            when(candidateClosest.getForecastAt()).thenReturn(target.minusHours(1));

            when(weatherRepository.findByXAndYAndForecastAtBetween(x, y, start, end))
                    .thenReturn(List.of(candidateFar, candidateClosest));

            YesterdayHourlyWeather yesterdayHourlyWeather = mock(YesterdayHourlyWeather.class);
            when(yesterdayHourlyWeatherRepository.findByXAndYAndDateAndHour(any(), any(), any(), any()))
                    .thenReturn(Optional.of(yesterdayHourlyWeather));

            List<WeatherResponse> expected = List.of(mock(WeatherResponse.class));

            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<Weather>> weatherListCaptor = ArgumentCaptor.forClass(List.class);

            when(weatherMapper.toDto(weatherListCaptor.capture(), eq(location), eq(yesterdayHourlyWeather)))
                    .thenReturn(expected);

            // when
            List<WeatherResponse> result = weatherService.getAll(longitude, latitude);

            // then
            assertThat(result).isEqualTo(expected);

            verify(locationNameMapRepository).findByLongitudeAndLatitude(longitude, latitude);
            verify(locationNameMapRepository, never()).save(any(LocationNameMap.class));
            verify(weatherRedisCacheService).getOrLoad(eq(x), eq(y), eq(forecastAt), any());

            verify(weatherRepository).findTargetWeathers(x, y, forecastedAt, targetTimes);
            verify(weatherRepository).findByXAndYAndForecastAtBetween(x, y, start, end);

            List<Weather> capturedWeathers = weatherListCaptor.getValue();
            assertThat(capturedWeathers).hasSize(4);
            assertThat(capturedWeathers).containsExactly(
                    day0Weather,
                    day1Weather,
                    day2Weather,
                    candidateClosest
            );

            verify(kmaWeatherClient, never()).callWeatherApi(anyString(), anyString(), anyInt(), anyInt(), anyInt());
        }

        @Test
        @DisplayName("어제 날씨 정보가 없으면 2일 전 23시 발표 데이터를 저장 후 반환한다")
        void getAll_savesYesterdayWeather_whenYesterdayWeatherNotExists() {
            // given
            double longitude = 126.8216;
            double latitude = 37.5295;

            int x = 57;
            int y = 126;

            LocationNameMap location = mock(LocationNameMap.class);

            when(locationNameMapRepository.findByLongitudeAndLatitude(longitude, latitude))
                    .thenReturn(Optional.of(location));

            LocalDateTime fixedNow = setFixedTime(2026, 3, 20, 11, 0);
            LocalDateTime forecastedAt = fixedNow.toLocalDate().atStartOfDay();
            LocalDateTime forecastAt = fixedNow.withMinute(0).withSecond(0).withNano(0);

            when(location.getX()).thenReturn(x);
            when(location.getY()).thenReturn(y);

            mockCachePassThrough(x, y, forecastAt);

            List<LocalDateTime> targetTimes = IntStream.range(0, 4)
                    .mapToObj(forecastAt::plusDays)
                    .toList();

            when(weatherRepository.findTargetWeathers(x, y, forecastedAt, targetTimes))
                    .thenReturn(List.of(
                            mock(Weather.class),
                            mock(Weather.class),
                            mock(Weather.class),
                            mock(Weather.class)
                    ));

            // 어제 날씨는 처음엔 없음 -> 저장 후 다시 조회하면 있음
            YesterdayHourlyWeather savedYesterdayWeather = mock(YesterdayHourlyWeather.class);
            when(yesterdayHourlyWeatherRepository.findByXAndYAndDateAndHour(any(), any(), any(), any()))
                    .thenReturn(Optional.empty())
                    .thenReturn(Optional.of(savedYesterdayWeather));

            String primaryBaseDate = fixedNow.toLocalDate().minusDays(2)
                    .format(DateTimeFormatter.ofPattern("yyyyMMdd"));

            List<KmaWeatherItem> yesterdayItems = List.of(mock(KmaWeatherItem.class));
            List<YesterdayHourlyWeather> mappedYesterdayWeathers = List.of(mock(YesterdayHourlyWeather.class));

            when(kmaWeatherClient.callWeatherApi(eq(primaryBaseDate), eq("2300"), eq(x), eq(y), eq(300)))
                    .thenReturn(yesterdayItems);

            when(kmaWeatherMapper.toYesterdayWeathers(x, y, yesterdayItems))
                    .thenReturn(mappedYesterdayWeathers);

            List<WeatherResponse> expected = List.of(mock(WeatherResponse.class));
            when(weatherMapper.toDto(anyList(), eq(location), eq(savedYesterdayWeather)))
                    .thenReturn(expected);

            // when
            List<WeatherResponse> result = weatherService.getAll(longitude, latitude);

            // then
            assertThat(result).isEqualTo(expected);

            verify(weatherRedisCacheService).getOrLoad(eq(x), eq(y), eq(forecastAt), any());
            verify(weatherRepository).findTargetWeathers(x, y, forecastedAt, targetTimes);

            verify(kmaWeatherClient).callWeatherApi(eq(primaryBaseDate), eq("2300"), eq(x), eq(y), eq(300));
            verify(kmaWeatherClient, never()).callWeatherApi(anyString(), eq("0200"), eq(x), eq(y), eq(300));
            verify(kmaWeatherMapper).toYesterdayWeathers(x, y, yesterdayItems);
            verify(yesterdayHourlyWeatherRepository).saveAll(mappedYesterdayWeathers);
        }

        @Test
        @DisplayName("어제 날씨 정보가 없고 2일 전 23시 발표 조회가 03 오류면 1일 전 02시 발표로 fallback 저장 후 반환한다")
        void getAll_savesYesterdayWeather_withFallback_whenPrimaryBaseTimeNotFound() {
            // given
            double longitude = 126.8216;
            double latitude = 37.5295;

            int x = 57;
            int y = 126;

            LocationNameMap location = mock(LocationNameMap.class);

            when(locationNameMapRepository.findByLongitudeAndLatitude(longitude, latitude))
                    .thenReturn(Optional.of(location));

            LocalDateTime fixedNow = setFixedTime(2026, 3, 20, 19, 0);
            LocalDateTime forecastedAt = fixedNow.toLocalDate().atStartOfDay();
            LocalDateTime forecastAt = fixedNow.withMinute(0).withSecond(0).withNano(0);

            when(location.getX()).thenReturn(x);
            when(location.getY()).thenReturn(y);

            mockCachePassThrough(x, y, forecastAt);

            List<LocalDateTime> targetTimes = IntStream.range(0, 4)
                    .mapToObj(forecastAt::plusDays)
                    .toList();

            when(weatherRepository.findTargetWeathers(x, y, forecastedAt, targetTimes))
                    .thenReturn(List.of(
                            mock(Weather.class),
                            mock(Weather.class),
                            mock(Weather.class),
                            mock(Weather.class)
                    ));

            YesterdayHourlyWeather savedYesterdayWeather = mock(YesterdayHourlyWeather.class);
            when(yesterdayHourlyWeatherRepository.findByXAndYAndDateAndHour(any(), any(), any(), any()))
                    .thenReturn(Optional.empty())
                    .thenReturn(Optional.of(savedYesterdayWeather));

            String primaryBaseDate = fixedNow.toLocalDate().minusDays(2)
                    .format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String fallbackBaseDate = fixedNow.toLocalDate().minusDays(1)
                    .format(DateTimeFormatter.ofPattern("yyyyMMdd"));

            List<KmaWeatherItem> fallbackItems = List.of(mock(KmaWeatherItem.class));
            List<YesterdayHourlyWeather> mappedYesterdayWeathers = List.of(mock(YesterdayHourlyWeather.class));

            when(kmaWeatherClient.callWeatherApi(eq(primaryBaseDate), eq("2300"), eq(x), eq(y), eq(300)))
                    .thenThrow(new KmaApiErrorException("03", "NO_DATA"));

            when(kmaWeatherClient.callWeatherApi(eq(fallbackBaseDate), eq("0200"), eq(x), eq(y), eq(300)))
                    .thenReturn(fallbackItems);

            when(kmaWeatherMapper.toYesterdayWeathers(x, y, fallbackItems))
                    .thenReturn(mappedYesterdayWeathers);

            List<WeatherResponse> expected = List.of(mock(WeatherResponse.class));
            when(weatherMapper.toDto(anyList(), eq(location), eq(savedYesterdayWeather)))
                    .thenReturn(expected);

            // when
            List<WeatherResponse> result = weatherService.getAll(longitude, latitude);

            // then
            assertThat(result).isEqualTo(expected);

            verify(weatherRedisCacheService).getOrLoad(eq(x), eq(y), eq(forecastAt), any());
            verify(weatherRepository).findTargetWeathers(x, y, forecastedAt, targetTimes);

            verify(kmaWeatherClient).callWeatherApi(eq(primaryBaseDate), eq("2300"), eq(x), eq(y), eq(300));
            verify(kmaWeatherClient).callWeatherApi(eq(fallbackBaseDate), eq("0200"), eq(x), eq(y), eq(300));
            verify(kmaWeatherMapper).toYesterdayWeathers(x, y, fallbackItems);
            verify(yesterdayHourlyWeatherRepository).saveAll(mappedYesterdayWeathers);
            verify(yesterdayHourlyWeatherRepository, times(2))
                    .findByXAndYAndDateAndHour(any(), any(), any(), any());
        }

        @Test
        @DisplayName("어제 날씨 정보가 없고 2일 전 23시 발표 조회가 03이 아닌 오류면 예외를 그대로 던진다")
        void getAll_throwsException_whenPrimaryYesterdayWeatherFetchFailsWithNon03Error() {
            // given
            double longitude = 126.8216;
            double latitude = 37.5295;

            int x = 57;
            int y = 126;

            LocationNameMap location = mock(LocationNameMap.class);

            when(locationNameMapRepository.findByLongitudeAndLatitude(longitude, latitude))
                    .thenReturn(Optional.of(location));

            LocalDateTime fixedNow = setFixedTime(2026, 3, 20, 19, 0);
            LocalDateTime forecastedAt = fixedNow.toLocalDate().atStartOfDay();
            LocalDateTime forecastAt = fixedNow.withMinute(0).withSecond(0).withNano(0);

            when(location.getX()).thenReturn(x);
            when(location.getY()).thenReturn(y);

            mockCachePassThrough(x, y, forecastAt);

            List<LocalDateTime> targetTimes = IntStream.range(0, 4)
                    .mapToObj(forecastAt::plusDays)
                    .toList();

            when(weatherRepository.findTargetWeathers(x, y, forecastedAt, targetTimes))
                    .thenReturn(List.of(
                            mock(Weather.class),
                            mock(Weather.class),
                            mock(Weather.class),
                            mock(Weather.class)
                    ));

            when(yesterdayHourlyWeatherRepository.findByXAndYAndDateAndHour(any(), any(), any(), any()))
                    .thenReturn(Optional.empty());

            String primaryBaseDate = fixedNow.toLocalDate().minusDays(2)
                    .format(DateTimeFormatter.ofPattern("yyyyMMdd"));

            when(kmaWeatherClient.callWeatherApi(eq(primaryBaseDate), eq("2300"), eq(x), eq(y), eq(300)))
                    .thenThrow(new KmaApiErrorException("99", "INTERNAL_ERROR"));

            // when & then
            assertThatThrownBy(() -> weatherService.getAll(longitude, latitude))
                    .isInstanceOf(KmaApiErrorException.class);

            verify(weatherRedisCacheService).getOrLoad(eq(x), eq(y), eq(forecastAt), any());
            verify(weatherRepository).findTargetWeathers(x, y, forecastedAt, targetTimes);

            verify(kmaWeatherClient).callWeatherApi(eq(primaryBaseDate), eq("2300"), eq(x), eq(y), eq(300));
            verify(kmaWeatherClient, never()).callWeatherApi(anyString(), eq("0200"), eq(x), eq(y), eq(300));
            verify(yesterdayHourlyWeatherRepository, never()).saveAll(anyList());
        }
    }

    @Test
    @DisplayName("경도와 위도를 기반으로 격자 좌표와 주소 정보를 반환한다")
    void getLocation_returnsLocationInfo() {
        // given
        double longitude = 126.8216;
        double latitude = 37.5295;

        GridResult gridResult = new GridResult(57, 126);
        List<String> addressLevels = List.of("경기도", "부천시 오정구", "고강본동", "");

        // Grid & 주소 mock
        when(kmaGridConverter.convertToGrid(latitude, longitude))
                .thenReturn(gridResult);

        when(kakaoLocalUtil.getAddressLevels(longitude, latitude, KakaoRegionType.H))
                .thenReturn(addressLevels);

        // when
        WeatherAPILocationResponse result = weatherService.getLocation(longitude, latitude);

        // then
        assertThat(result.latitude()).isEqualTo(latitude);
        assertThat(result.longitude()).isEqualTo(longitude);
        assertThat(result.x()).isEqualTo(57);
        assertThat(result.y()).isEqualTo(126);

        verify(kmaGridConverter).convertToGrid(latitude, longitude);
        verify(kakaoLocalUtil).getAddressLevels(longitude, latitude, KakaoRegionType.H);
    }
}