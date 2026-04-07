package com.codeit.otboo.domain.weather.service;

import com.codeit.otboo.domain.weather.entity.Weather;
import com.codeit.otboo.domain.weather.repository.WeatherRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WeatherForecastUpsertServiceTest {

    @Mock
    private WeatherRepository weatherRepository;

    @InjectMocks
    private WeatherForecastUpsertService weatherForecastUpsertService;

    @Nested
    @DisplayName("upsert()")
    class UpsertTest {

        @Test
        @DisplayName("기존 날씨 정보가 없으면 새 날씨 정보를 저장한다")
        void upsert_savesNewWeather_whenWeatherDoesNotExist() {
            // given
            Weather newWeather = mock(Weather.class);
            LocalDateTime forecastedAt = LocalDateTime.of(2026, 3, 20, 0, 0);
            LocalDateTime forecastAt = LocalDateTime.of(2026, 3, 20, 12, 0);

            given(newWeather.getForecastedAt()).willReturn(forecastedAt);
            given(newWeather.getForecastAt()).willReturn(forecastAt);
            given(newWeather.getX()).willReturn(57);
            given(newWeather.getY()).willReturn(126);

            given(weatherRepository.findByForecastedAtAndForecastAtAndXAndY(
                    forecastedAt,
                    forecastAt,
                    57,
                    126
            )).willReturn(Optional.empty());

            // when
            weatherForecastUpsertService.upsert(List.of(newWeather));

            // then
            verify(weatherRepository).findByForecastedAtAndForecastAtAndXAndY(
                    forecastedAt,
                    forecastAt,
                    57,
                    126
            );

            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<Weather>> captor = ArgumentCaptor.forClass(List.class);
            verify(weatherRepository).saveAll(captor.capture());

            List<Weather> savedList = captor.getValue();
            assertThat(savedList).hasSize(1);
            assertThat(savedList).containsExactly(newWeather);
        }

        @Test
        @DisplayName("기존 날씨 정보가 있으면 값을 갱신한 후 저장한다")
        void upsert_updatesExistingWeather_whenWeatherExists() {
            // given
            Weather newWeather = mock(Weather.class);
            Weather savedWeather = mock(Weather.class);

            LocalDateTime forecastedAt = LocalDateTime.of(2026, 3, 20, 0, 0);
            LocalDateTime forecastAt = LocalDateTime.of(2026, 3, 20, 12, 0);

            given(newWeather.getForecastedAt()).willReturn(forecastedAt);
            given(newWeather.getForecastAt()).willReturn(forecastAt);
            given(newWeather.getX()).willReturn(57);
            given(newWeather.getY()).willReturn(126);

            given(weatherRepository.findByForecastedAtAndForecastAtAndXAndY(
                    forecastedAt,
                    forecastAt,
                    57,
                    126
            )).willReturn(Optional.of(savedWeather));

            // when
            weatherForecastUpsertService.upsert(List.of(newWeather));

            // then
            verify(savedWeather).update(newWeather);

            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<Weather>> captor = ArgumentCaptor.forClass(List.class);
            verify(weatherRepository).saveAll(captor.capture());

            List<Weather> savedList = captor.getValue();
            assertThat(savedList).hasSize(1);
            assertThat(savedList).containsExactly(savedWeather);
        }

        @Test
        @DisplayName("여러 날씨 정보를 forecastAt, forecastedAt 순으로 정렬 후 저장한다")
        void upsert_sortsWeatherListBeforeSaving() {
            // given
            Weather weather1 = mock(Weather.class);
            Weather weather2 = mock(Weather.class);

            LocalDateTime forecastedAt1 = LocalDateTime.of(2026, 3, 20, 0, 0);
            LocalDateTime forecastedAt2 = LocalDateTime.of(2026, 3, 21, 0, 0);
            LocalDateTime forecastAt1 = LocalDateTime.of(2026, 3, 20, 15, 0);
            LocalDateTime forecastAt2 = LocalDateTime.of(2026, 3, 20, 12, 0);

            given(weather1.getForecastedAt()).willReturn(forecastedAt1);
            given(weather1.getForecastAt()).willReturn(forecastAt1);
            given(weather1.getX()).willReturn(57);
            given(weather1.getY()).willReturn(126);

            given(weather2.getForecastedAt()).willReturn(forecastedAt2);
            given(weather2.getForecastAt()).willReturn(forecastAt2);
            given(weather2.getX()).willReturn(57);
            given(weather2.getY()).willReturn(126);

            given(weatherRepository.findByForecastedAtAndForecastAtAndXAndY(
                    forecastedAt1, forecastAt1, 57, 126
            )).willReturn(Optional.empty());

            given(weatherRepository.findByForecastedAtAndForecastAtAndXAndY(
                    forecastedAt2, forecastAt2, 57, 126
            )).willReturn(Optional.empty());

            // when
            weatherForecastUpsertService.upsert(List.of(weather1, weather2));

            // then
            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<Weather>> captor = ArgumentCaptor.forClass(List.class);
            verify(weatherRepository).saveAll(captor.capture());

            List<Weather> savedList = captor.getValue();
            assertThat(savedList).containsExactly(weather2, weather1);
        }
    }
}
