package com.codeit.otboo.domain.weather.service;

import com.codeit.otboo.domain.weather.dto.response.WeatherAPILocationResponse;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("날씨 서비스 단위 테스트")
class WeatherServiceImplTest {

    @InjectMocks
    private WeatherServiceImpl weatherService;

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

            // 날씨 조회 여부 (처음 없음 → 이후 존재)
            for (int i = 0; i < 4; i++) {
                when(weatherRepository.findByForecastedAtAndForecastAtAndXAndY(
                        eq(forecastedAt),
                        eq(forecastAt.plusDays(i)),
                        eq(x),
                        eq(y)
                ))
                        .thenReturn(Optional.empty())
                        .thenReturn(Optional.of(mock(Weather.class)));
            }

            when(savedLocation.getX()).thenReturn(x);
            when(savedLocation.getY()).thenReturn(y);

            // saveOrUpdateWeather()
            List<KmaWeatherItem> items = List.of(mock(KmaWeatherItem.class));
            List<Weather> mappedWeathers = List.of(mock(Weather.class));
            when(kmaWeatherClient.callWeatherApi(anyString(), eq("2300"), eq(x), eq(y), eq(1052)))
                    .thenReturn(items);
            when(kmaWeatherMapper.toWeathers(eq("2300"), eq(x), eq(y), eq(items), eq(false)))
                    .thenReturn(mappedWeathers);

            // 어제 날씨
            YesterdayHourlyWeather yesterdayHourlyWeather = mock(YesterdayHourlyWeather.class);
            when(yesterdayHourlyWeatherRepository.findByDateAndHour(any(), any()))
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

            for (int i = 0; i < 4; i++) {
                verify(weatherRepository, times(2))
                        .findByForecastedAtAndForecastAtAndXAndY(
                                eq(forecastedAt),
                                eq(forecastAt.plusDays(i)),
                                eq(x),
                                eq(y)
                        );
            }

            verify(kmaWeatherClient, times(1)).callWeatherApi(anyString(), eq("2300"), eq(x), eq(y), eq(1052));
            verify(kmaWeatherMapper, times(1)).toWeathers(eq("2300"), eq(x), eq(y), eq(items), eq(false));
            verify(weatherRepository, times(1)).saveAll(eq(mappedWeathers));

            verify(yesterdayHourlyWeatherRepository, times(1)).findByDateAndHour(any(), any());
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

            // addWeathers()
            // 처음에는 새로운 지역이 들어와서 값이 없으므로 빈 값을 전달
            // 두번째는 날씨 데이터를 추가한 뒤에 호출되어 날씨 객체 값을 전달
            for (int i = 0; i < 4; i++) {
                when(weatherRepository.findByForecastedAtAndForecastAtAndXAndY(
                        eq(forecastedAt),
                        eq(forecastAt.plusDays(i)),
                        eq(x),
                        eq(y)
                ))
                        .thenReturn(Optional.empty())
                        .thenReturn(Optional.of(mock(Weather.class)));
            }

            when(location.getX()).thenReturn(x);
            when(location.getY()).thenReturn(y);

            // saveOrUpdateWeather()
            List<KmaWeatherItem> items = List.of(mock(KmaWeatherItem.class));
            List<Weather> mappedWeathers = List.of(mock(Weather.class));
            when(kmaWeatherClient.callWeatherApi(anyString(), eq("2300"), eq(x), eq(y), eq(1052)))
                    .thenReturn(items);
            when(kmaWeatherMapper.toWeathers(eq("2300"), eq(x), eq(y), eq(items), eq(false)))
                    .thenReturn(mappedWeathers);

            // 어제 습도, 온도 값 저장
            YesterdayHourlyWeather yesterdayHourlyWeather = mock(YesterdayHourlyWeather.class);
            when(yesterdayHourlyWeatherRepository.findByDateAndHour(any(), any()))
                    .thenReturn(Optional.of(yesterdayHourlyWeather));

            // 반환 값 dto 변환
            List<WeatherResponse> expected = List.of(mock(WeatherResponse.class));
            when(weatherMapper.toDto(anyList(), eq(location), eq(yesterdayHourlyWeather)))
                    .thenReturn(expected);

            // when
            List<WeatherResponse> result = weatherService.getAll(longitude, latitude);

            // then
            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(expected);

            verify(locationNameMapRepository, times(1)).findByLongitudeAndLatitude(longitude, latitude);
            verify(locationNameMapRepository, never()).save(any(LocationNameMap.class));

            for (int i = 0; i < 4; i++) {
                verify(weatherRepository, times(2))
                        .findByForecastedAtAndForecastAtAndXAndY(
                                eq(forecastedAt),
                                eq(forecastAt.plusDays(i)),
                                eq(x),
                                eq(y)
                        );
            }

            verify(kmaWeatherClient, times(1)).callWeatherApi(anyString(), eq("2300"), eq(x), eq(y), eq(1052));
            verify(kmaWeatherMapper, times(1)).toWeathers(eq("2300"), eq(x), eq(y), eq(items), eq(false));
            verify(weatherRepository, times(1)).saveAll(eq(mappedWeathers));

            verify(yesterdayHourlyWeatherRepository, times(1)).findByDateAndHour(any(), any());
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

            // addWeathers() 결과가 정확히 3개가 되도록 설정
            Weather day0Weather = mock(Weather.class);
            Weather day1Weather = mock(Weather.class);
            Weather day2Weather = mock(Weather.class);

            when(weatherRepository.findByForecastedAtAndForecastAtAndXAndY(
                    eq(forecastedAt), eq(forecastAt), eq(x), eq(y)
            )).thenReturn(Optional.of(day0Weather));

            when(weatherRepository.findByForecastedAtAndForecastAtAndXAndY(
                    eq(forecastedAt), eq(forecastAt.plusDays(1)), eq(x), eq(y)
            )).thenReturn(Optional.of(day1Weather));

            when(weatherRepository.findByForecastedAtAndForecastAtAndXAndY(
                    eq(forecastedAt), eq(forecastAt.plusDays(2)), eq(x), eq(y)
            )).thenReturn(Optional.of(day2Weather));

            when(weatherRepository.findByForecastedAtAndForecastAtAndXAndY(
                    eq(forecastedAt), eq(forecastAt.plusDays(3)), eq(x), eq(y)
            )).thenReturn(Optional.empty());

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
            when(yesterdayHourlyWeatherRepository.findByDateAndHour(any(), any()))
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
        @DisplayName("어제 날씨 정보가 없으면 저장 후 반환한다")
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

            // 오늘/미래 날씨는 이미 존재한다고 가정
            for (int i = 0; i < 4; i++) {
                when(weatherRepository.findByForecastedAtAndForecastAtAndXAndY(
                        eq(forecastedAt),
                        eq(forecastAt.plusDays(i)),
                        eq(x),
                        eq(y)
                )).thenReturn(Optional.of(mock(Weather.class)));
            }

            when(location.getX()).thenReturn(x);
            when(location.getY()).thenReturn(y);

            // 어제 날씨는 처음엔 없음 -> 저장 후 다시 조회하면 있음
            YesterdayHourlyWeather savedYesterdayWeather = mock(YesterdayHourlyWeather.class);
            when(yesterdayHourlyWeatherRepository.findByDateAndHour(any(), any()))
                    .thenReturn(Optional.empty())
                    .thenReturn(Optional.of(savedYesterdayWeather));

            List<KmaWeatherItem> yesterdayItems = List.of(mock(KmaWeatherItem.class));
            List<YesterdayHourlyWeather> mappedYesterdayWeathers = List.of(mock(YesterdayHourlyWeather.class));

            when(kmaWeatherClient.callWeatherApi(anyString(), eq("2300"), eq(x), eq(y), eq(300)))
                    .thenReturn(yesterdayItems);

            when(kmaWeatherMapper.toYesterdayWeathers(x, y, yesterdayItems))
                    .thenReturn(mappedYesterdayWeathers);

            List<WeatherResponse> expected = List.of(mock(WeatherResponse.class));
            when(weatherMapper.toDto(anyList(), eq(location), eq(savedYesterdayWeather)))
                    .thenReturn(expected);

            // when
            List<WeatherResponse> result = weatherService.getAll(longitude, latitude);

            // then
            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(expected);

            verify(locationNameMapRepository, times(1)).findByLongitudeAndLatitude(longitude, latitude);
            verify(locationNameMapRepository, never()).save(any(LocationNameMap.class));

            for (int i = 0; i < 4; i++) {
                verify(weatherRepository).findByForecastedAtAndForecastAtAndXAndY(
                        eq(forecastedAt),
                        eq(forecastAt.plusDays(i)),
                        eq(x),
                        eq(y)
                );
            }

            verify(yesterdayHourlyWeatherRepository, times(2)).findByDateAndHour(any(), any());
            verify(kmaWeatherClient, times(1)).callWeatherApi(anyString(), eq("2300"), eq(x), eq(y), eq(300));
            verify(kmaWeatherMapper, times(1)).toYesterdayWeathers(x, y, yesterdayItems);
            verify(yesterdayHourlyWeatherRepository,times(1)).saveAll(mappedYesterdayWeathers);

            verify(weatherMapper, times(1)).toDto(anyList(), eq(location), eq(savedYesterdayWeather));
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

    @Nested
    @DisplayName("updateCurrentWeather()")
    class UpdateCurrentWeatherTest {

        @Test
        @DisplayName("저장된 모든 위치의 현재 날씨를 갱신한다")
        void updateCurrentWeather_updatesAllLocations() {
            // given

            LocalDateTime fixedNow = LocalDateTime.of(2026, 3, 20, 11, 0);
            when(timeProvider.nowDate()).thenReturn(fixedNow.toLocalDate());
            when(timeProvider.nowTime()).thenReturn(fixedNow.toLocalTime());

            LocationNameMap location1 = mock(LocationNameMap.class);
            when(location1.getX()).thenReturn(57);
            when(location1.getY()).thenReturn(126);

            LocationNameMap location2 = mock(LocationNameMap.class);
            when(location2.getX()).thenReturn(60);
            when(location2.getY()).thenReturn(127);

            when(locationNameMapRepository.findAll())
                    .thenReturn(List.of(location1, location2));

            List<KmaWeatherItem> items1 = List.of(mock(KmaWeatherItem.class));
            List<KmaWeatherItem> items2 = List.of(mock(KmaWeatherItem.class));

            when(kmaWeatherClient.callWeatherApi(eq("20260320"), anyString(), eq(57), eq(126), eq(1052)))
                    .thenReturn(items1);
            when(kmaWeatherClient.callWeatherApi(eq("20260320"), anyString(), eq(60), eq(127), eq(1052)))
                    .thenReturn(items2);

            Weather weather1 = mock(Weather.class);
            LocalDateTime weather1ForecastedAt = LocalDateTime.of(2026, 3, 20, 0, 0);
            LocalDateTime weather1ForecastAt = LocalDateTime.of(2026, 3, 20, 12, 0);

            when(weather1.getForecastedAt()).thenReturn(weather1ForecastedAt);
            when(weather1.getForecastAt()).thenReturn(weather1ForecastAt);
            when(weather1.getX()).thenReturn(57);
            when(weather1.getY()).thenReturn(126);

            Weather weather2 = mock(Weather.class);
            LocalDateTime weather2ForecastedAt = LocalDateTime.of(2026, 3, 20, 0, 0);
            LocalDateTime weather2ForecastAt = LocalDateTime.of(2026, 3, 20, 12, 0);

            when(weather2.getForecastedAt()).thenReturn(weather2ForecastedAt);
            when(weather2.getForecastAt()).thenReturn(weather2ForecastAt);
            when(weather2.getX()).thenReturn(60);
            when(weather2.getY()).thenReturn(127);

            when(kmaWeatherMapper.toWeathers(anyString(), eq(57), eq(126), eq(items1), eq(true)))
                    .thenReturn(List.of(weather1));
            when(kmaWeatherMapper.toWeathers(anyString(), eq(60), eq(127), eq(items2), eq(true)))
                    .thenReturn(List.of(weather2));

            when(weatherRepository.findByForecastedAtAndForecastAtAndXAndY(
                    eq(weather1ForecastedAt),
                    eq(weather1ForecastAt),
                    eq(57),
                    eq(126)
            )).thenReturn(Optional.empty());

            when(weatherRepository.findByForecastedAtAndForecastAtAndXAndY(
                    eq(weather2ForecastedAt),
                    eq(weather2ForecastAt),
                    eq(60),
                    eq(127)
            )).thenReturn(Optional.empty());

            // when
            weatherService.updateCurrentWeather();

            // then
            verify(locationNameMapRepository).findAll();

            verify(kmaWeatherClient).callWeatherApi(eq("20260320"), anyString(), eq(57), eq(126), eq(1052));
            verify(kmaWeatherClient).callWeatherApi(eq("20260320"), anyString(), eq(60), eq(127), eq(1052));

            verify(kmaWeatherMapper).toWeathers(anyString(), eq(57), eq(126), eq(items1), eq(true));
            verify(kmaWeatherMapper).toWeathers(anyString(), eq(60), eq(127), eq(items2), eq(true));

            verify(weatherRepository).findByForecastedAtAndForecastAtAndXAndY(
                    eq(weather1ForecastedAt),
                    eq(weather1ForecastAt),
                    eq(57),
                    eq(126)
            );

            verify(weatherRepository).findByForecastedAtAndForecastAtAndXAndY(
                    eq(weather2ForecastedAt),
                    eq(weather2ForecastAt),
                    eq(60),
                    eq(127)
            );

            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<Weather>> captor = ArgumentCaptor.forClass(List.class);

            verify(weatherRepository, times(2)).saveAll(captor.capture());

            List<List<Weather>> savedLists = captor.getAllValues();
            assertThat(savedLists).hasSize(2);
            assertThat(savedLists.get(0)).containsExactly(weather1);
            assertThat(savedLists.get(1)).containsExactly(weather2);
        }

        @Test
        @DisplayName("기존 날씨가 있으면 값을 갱신한 후 저장한다")
        void updateCurrentWeather_updatesExistingWeather() {
            // given
            LocalDateTime fixedNow = LocalDateTime.of(2026, 3, 20, 12, 0);
            when(timeProvider.nowDate()).thenReturn(fixedNow.toLocalDate());
            when(timeProvider.nowTime()).thenReturn(fixedNow.toLocalTime());

            LocationNameMap location = mock(LocationNameMap.class);
            when(location.getX()).thenReturn(57);
            when(location.getY()).thenReturn(126);

            when(locationNameMapRepository.findAll()).thenReturn(List.of(location));

            List<KmaWeatherItem> items = List.of(mock(KmaWeatherItem.class));
            when(kmaWeatherClient.callWeatherApi(eq("20260320"), anyString(), eq(57), eq(126), eq(1052)))
                    .thenReturn(items);

            Weather newWeather = mock(Weather.class);
            LocalDateTime newWeatherForecastedAt = LocalDateTime.of(2026, 3, 20, 0, 0);
            LocalDateTime newWeatherForecastAt = LocalDateTime.of(2026, 3, 20, 12, 0);

            when(newWeather.getForecastedAt()).thenReturn(newWeatherForecastedAt);
            when(newWeather.getForecastAt()).thenReturn(newWeatherForecastAt);
            when(newWeather.getX()).thenReturn(57);
            when(newWeather.getY()).thenReturn(126);

            when(kmaWeatherMapper.toWeathers(anyString(), eq(57), eq(126), eq(items), eq(true)))
                    .thenReturn(List.of(newWeather));

            Weather savedWeather = mock(Weather.class);
            when(weatherRepository.findByForecastedAtAndForecastAtAndXAndY(
                    eq(newWeatherForecastedAt),
                    eq(newWeatherForecastAt),
                    eq(57),
                    eq(126)
            )).thenReturn(Optional.of(savedWeather));

            // when
            weatherService.updateCurrentWeather();

            // then
            verify(locationNameMapRepository).findAll();

            verify(kmaWeatherClient).callWeatherApi(eq("20260320"), anyString(), eq(57), eq(126), eq(1052));
            verify(kmaWeatherMapper).toWeathers(anyString(), eq(57), eq(126), eq(items), eq(true));

            verify(weatherRepository).findByForecastedAtAndForecastAtAndXAndY(
                    eq(newWeatherForecastedAt),
                    eq(newWeatherForecastAt),
                    eq(57),
                    eq(126)
            );

            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<Weather>> captor = ArgumentCaptor.forClass(List.class);

            verify(weatherRepository).saveAll(captor.capture());

            List<Weather> savedList = captor.getValue();
            assertThat(savedList).hasSize(1);
            assertThat(savedList).containsExactly(savedWeather);
        }

        @Test
        @DisplayName("저장된 위치가 없으면 아무 작업도 하지 않는다")
        void updateCurrentWeather_doesNothing_whenNoLocations() {
            // given
            when(locationNameMapRepository.findAll()).thenReturn(List.of());

            // when
            weatherService.updateCurrentWeather();

            // then
            verify(locationNameMapRepository).findAll();
            verifyNoInteractions(kmaWeatherClient); // 해당 mock을 실행하면 fail
            verifyNoInteractions(kmaWeatherMapper);
            verify(weatherRepository, never()).saveAll(anyList());
        }
    }

    @Nested
    @DisplayName("deleteYesterdayWeather()")
    class DeleteYesterdayWeatherTest {

        @Test
        @DisplayName("어제 날씨의 온도/습도를 저장한 뒤 오늘 이전 날씨 데이터를 삭제한다")
        void deleteYesterdayWeather_savesYesterdayHourlyWeatherAndDeletesOldWeather() {
            // given
            LocalDate today = LocalDate.of(2026, 3, 20);
            when(timeProvider.nowDate()).thenReturn(today);

            LocalDate yesterday = today.minusDays(1);
            LocalDateTime todayStart = today.atStartOfDay();
            LocalDateTime yesterdayStart = yesterday.atStartOfDay();
            LocalDateTime yesterdayEnd = yesterday.plusDays(1).atStartOfDay();

            Weather weather1 = mock(Weather.class);
            when(weather1.getX()).thenReturn(57);
            when(weather1.getY()).thenReturn(126);
            when(weather1.getForecastAt()).thenReturn(LocalDateTime.of(2026, 3, 19, 9, 0));
            when(weather1.getTemperatureCurrent()).thenReturn(10.5);
            when(weather1.getHumidityCurrent()).thenReturn(60.0);

            Weather weather2 = mock(Weather.class);
            when(weather2.getX()).thenReturn(57);
            when(weather2.getY()).thenReturn(126);
            when(weather2.getForecastAt()).thenReturn(LocalDateTime.of(2026, 3, 19, 12, 0));
            when(weather2.getTemperatureCurrent()).thenReturn(12.0);
            when(weather2.getHumidityCurrent()).thenReturn(55.0);

            when(weatherRepository.findYesterdayWeather(yesterdayStart, yesterdayStart, yesterdayEnd))
                    .thenReturn(List.of(weather2, weather1));

            // when
            weatherService.deleteYesterdayWeather();

            // then
            verify(timeProvider).nowDate();

            verify(weatherRepository).findYesterdayWeather(
                    eq(yesterdayStart),
                    eq(yesterdayStart),
                    eq(yesterdayEnd)
            );

            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<YesterdayHourlyWeather>> captor = ArgumentCaptor.forClass(List.class);

            verify(yesterdayHourlyWeatherRepository).saveAll(captor.capture());

            List<YesterdayHourlyWeather> savedList = captor.getValue();
            assertThat(savedList).hasSize(2);

            // 정렬 확인: 09:00 이 먼저 와야 함
            assertThat(savedList.get(0).getX()).isEqualTo(57);
            assertThat(savedList.get(0).getY()).isEqualTo(126);
            assertThat(savedList.get(0).getDate()).isEqualTo(LocalDate.of(2026, 3, 19));
            assertThat(savedList.get(0).getHour()).isEqualTo(LocalTime.of(9, 0));
            assertThat(savedList.get(0).getTemperature()).isEqualTo(10.5);
            assertThat(savedList.get(0).getHumidity()).isEqualTo(60.0);

            assertThat(savedList.get(1).getX()).isEqualTo(57);
            assertThat(savedList.get(1).getY()).isEqualTo(126);
            assertThat(savedList.get(1).getDate()).isEqualTo(LocalDate.of(2026, 3, 19));
            assertThat(savedList.get(1).getHour()).isEqualTo(LocalTime.of(12, 0));
            assertThat(savedList.get(1).getTemperature()).isEqualTo(12.0);
            assertThat(savedList.get(1).getHumidity()).isEqualTo(55.0);

            verify(weatherRepository).deleteByForecastedAtBefore(todayStart);
        }

        @Test
        @DisplayName("어제 날씨가 없어도 빈 리스트를 저장하고 오늘 이전 날씨 데이터를 삭제한다")
        void deleteYesterdayWeather_whenNoYesterdayWeather_deletesOldWeather() {
            // given
            LocalDate today = LocalDate.of(2026, 3, 20);
            when(timeProvider.nowDate()).thenReturn(today);

            LocalDate yesterday = today.minusDays(1);
            LocalDateTime todayStart = today.atStartOfDay();
            LocalDateTime yesterdayStart = yesterday.atStartOfDay();
            LocalDateTime yesterdayEnd = yesterday.plusDays(1).atStartOfDay();

            when(weatherRepository.findYesterdayWeather(yesterdayStart, yesterdayStart, yesterdayEnd))
                    .thenReturn(List.of());

            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<YesterdayHourlyWeather>> captor = ArgumentCaptor.forClass(List.class);

            // when
            weatherService.deleteYesterdayWeather();

            // then
            verify(weatherRepository).findYesterdayWeather(
                    eq(yesterdayStart),
                    eq(yesterdayStart),
                    eq(yesterdayEnd)
            );

            verify(yesterdayHourlyWeatherRepository).saveAll(captor.capture());
            assertThat(captor.getValue()).isEmpty();

            verify(weatherRepository).deleteByForecastedAtBefore(todayStart);
        }
    }
}