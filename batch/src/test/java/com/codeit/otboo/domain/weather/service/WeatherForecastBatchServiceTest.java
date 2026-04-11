package com.codeit.otboo.domain.weather.service;

import com.codeit.otboo.batch.weather.forecast.model.ForecastBatchResult;
import com.codeit.otboo.batch.weather.forecast.service.WeatherForecastBatchService;
import com.codeit.otboo.domain.weather.client.KmaWeatherClient;
import com.codeit.otboo.domain.weather.client.KmaWeatherMapper;
import com.codeit.otboo.domain.weather.client.dto.KmaWeatherItem;
import com.codeit.otboo.domain.weather.entity.Weather;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WeatherForecastBatchServiceTest {

    @Mock
    private KmaWeatherClient kmaWeatherClient;

    @Mock
    private KmaWeatherMapper kmaWeatherMapper;

    @InjectMocks
    private WeatherForecastBatchService weatherForecastBatchService;

    @Nested
    @DisplayName("collect()")
    class CollectTest {

        @Test
        @DisplayName("좌표와 발표 시각 기준으로 날씨 정보를 수집하고 변환 결과를 반환한다")
        void collect_returnsForecastBatchResult() {
            // given
            int x = 57;
            int y = 126;
            String baseDate = "20260320";
            String baseTime = "1100";

            List<KmaWeatherItem> items = List.of(mock(KmaWeatherItem.class), mock(KmaWeatherItem.class));
            List<Weather> weathers = List.of(mock(Weather.class), mock(Weather.class));

            given(kmaWeatherClient.callWeatherApi(baseDate, baseTime, x, y, 1052))
                    .willReturn(items);
            given(kmaWeatherMapper.toWeathers(baseTime, x, y, items, true))
                    .willReturn(weathers);

            // when
            ForecastBatchResult result = weatherForecastBatchService.collect(x, y, baseDate, baseTime);

            // then
            verify(kmaWeatherClient).callWeatherApi(baseDate, baseTime, x, y, 1052);
            verify(kmaWeatherMapper).toWeathers(baseTime, x, y, items, true);

            assertThat(result.x()).isEqualTo(x);
            assertThat(result.y()).isEqualTo(y);
            assertThat(result.weathers()).containsExactlyElementsOf(weathers);
        }

        @Test
        @DisplayName("수집된 날씨가 없으면 빈 리스트를 포함한 결과를 반환한다")
        void collect_returnsEmptyWeatherList_whenMapperReturnsEmptyList() {
            // given
            int x = 57;
            int y = 126;
            String baseDate = "20260320";
            String baseTime = "1100";

            List<KmaWeatherItem> items = List.of(mock(KmaWeatherItem.class));

            given(kmaWeatherClient.callWeatherApi(baseDate, baseTime, x, y, 1052))
                    .willReturn(items);
            given(kmaWeatherMapper.toWeathers(baseTime, x, y, items, true))
                    .willReturn(List.of());

            // when
            ForecastBatchResult result = weatherForecastBatchService.collect(x, y, baseDate, baseTime);

            // then
            verify(kmaWeatherClient).callWeatherApi(baseDate, baseTime, x, y, 1052);
            verify(kmaWeatherMapper).toWeathers(baseTime, x, y, items, true);

            assertThat(result.x()).isEqualTo(x);
            assertThat(result.y()).isEqualTo(y);
            assertThat(result.weathers()).isEmpty();
        }
    }
}