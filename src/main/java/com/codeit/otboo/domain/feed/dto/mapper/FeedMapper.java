package com.codeit.otboo.domain.feed.dto.mapper;

import com.codeit.otboo.domain.clothes.management.entity.Clothes;
import com.codeit.otboo.domain.feed.dto.response.FeedOotdResponse;
import com.codeit.otboo.domain.feed.dto.response.FeedResponse;
import com.codeit.otboo.domain.feed.entity.Feed;
import com.codeit.otboo.domain.feed.entity.FeedWeather;
import com.codeit.otboo.domain.weather.dto.response.PrecipitationResponse;
import com.codeit.otboo.domain.weather.dto.response.TemperatureResponse;
import com.codeit.otboo.domain.weather.dto.response.WeatherSummaryResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FeedMapper {

    public FeedResponse toDto(Feed feed) {
        return toDto(feed, false); // Feed 생성 시 기본 false
    }

    public FeedResponse toDto(Feed feed, boolean likedByMe) {
        return FeedResponse.builder()
                .id(feed.getId())
                .createdAt(feed.getCreatedAt())
                .updatedAt(feed.getUpdatedAt())
//                .authorResponse(UserMapper.toDto(feed.getAuthor()))
                .weatherResponse(toWeatherSummaryDto(feed.getWeather()))
                .ootds(toOotdDto(feed.getClothesList()))
                .content(feed.getContent())
                .likeCount(feed.getLikeCount())
                .commentCount(feed.getCommentCount())
                .likedByMe(likedByMe)
                .build();
    }

    private static WeatherSummaryResponse toWeatherSummaryDto(FeedWeather weather) {
        PrecipitationResponse precipitationResponse = new PrecipitationResponse(
                weather.getPrecipitationType(),
                weather.getPrecipitationAmount(),
                weather.getPrecipitationProbability()
        );

        TemperatureResponse temperatureResponse = new TemperatureResponse(
                weather.getTemperatureCurrent(),
                weather.getTemperatureComparedToDayBefore(),
                weather.getTemperatureMin(),
                weather.getTemperatureMax()
        );

        return new WeatherSummaryResponse(
                weather.getWeatherId(),
                weather.getSkyStatus(),
                precipitationResponse,
                temperatureResponse
        );
    }

    private static List<FeedOotdResponse> toOotdDto(List<Clothes> clothesList) {
        if (clothesList.isEmpty()) return List.of();
        return null;
    }
    /*
    UUID clothesId,
    String name,
    String imageUrl,
    ClothesType type,
    List<ClothesAttributeWithDefResponse> attributes

    ClothesAttributeWithDefResponse
    UUID definitionId,
    String definitionName,
    List<String> selectableValue,
    String value
     */
}
