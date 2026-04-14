package com.codeit.otboo.domain.weather.client;

import com.codeit.otboo.domain.weather.client.dto.KmaHeader;
import com.codeit.otboo.domain.weather.client.dto.KmaWeatherApiResponse;
import com.codeit.otboo.domain.weather.client.dto.KmaWeatherItem;
import com.codeit.otboo.domain.weather.exception.KmaApiErrorException;
import com.codeit.otboo.domain.weather.exception.KmaApiInvalidResponseException;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class KmaWeatherClient {

    private static final String API_NAME = "getVilageFcst";

    private final RestClient restClient;
    private final MeterRegistry meterRegistry;

    @Value("${api.kma-api-key}")
    private String kmaApiKey;

    public List<KmaWeatherItem> callWeatherApi(String baseDate, String baseTime, int nx, int ny, int numOfRows) {
        Timer.Sample sample = Timer.start(meterRegistry);

        try {
            KmaWeatherApiResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
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

            List<KmaWeatherItem> items = parseItems(response);

            meterRegistry.counter(
                    "weather.kma.api.success",
                    "api", API_NAME
            ).increment();

            return items;
        } catch (Exception e) {
            meterRegistry.counter(
                    "weather.kma.api.failure",
                    "api", API_NAME,
                    "exception", e.getClass().getSimpleName()
            ).increment();

            log.error(
                    "KMA API 호출 실패 - baseDate={}, baseTime={}, nx={}, ny={}",
                    baseDate, baseTime, nx, ny, e
            );

            throw e;
        } finally {
            sample.stop(
                    Timer.builder("weather.kma.api.duration")
                            .description("KMA 날씨 API 호출 시간")
                            .tag("api", API_NAME)
                            .register(meterRegistry)
            );
        }
    }

    private List<KmaWeatherItem> parseItems(KmaWeatherApiResponse response) {
        if (response == null || response.response() == null || response.response().header() == null) {
            throw new KmaApiInvalidResponseException("header is null");
        }

        KmaHeader header = response.response().header();
        if (!"00".equals(header.resultCode())) {
            throw new KmaApiErrorException(header.resultCode(), header.resultMsg());
        }

        if (response.response().body() == null) {
            throw new KmaApiInvalidResponseException("body is null");
        }

        if (response.response().body().items() == null || response.response().body().items().item() == null) {
            return Collections.emptyList();
        }

        return response.response().body().items().item();
    }
}
