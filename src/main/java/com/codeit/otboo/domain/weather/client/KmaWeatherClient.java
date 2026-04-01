package com.codeit.otboo.domain.weather.client;

import com.codeit.otboo.domain.weather.client.dto.KmaHeader;
import com.codeit.otboo.domain.weather.client.dto.KmaWeatherApiResponse;
import com.codeit.otboo.domain.weather.client.dto.KmaWeatherItem;
import com.codeit.otboo.domain.weather.entity.*;
import com.codeit.otboo.domain.weather.exception.KmaApiErrorException;
import com.codeit.otboo.domain.weather.exception.KmaApiInvalidResponseException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.*;

@Component
@RequiredArgsConstructor
public class KmaWeatherClient {

    private final RestClient restClient;

    @Value("${api.kma-api-key}")
    private String kmaApiKey;

    public List<KmaWeatherItem> callWeatherApi(String baseDate, String baseTime, int nx, int ny, int numOfRows) {
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

        return parseItems(response);
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
