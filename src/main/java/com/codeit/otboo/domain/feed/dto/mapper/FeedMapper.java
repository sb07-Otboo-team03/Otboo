package com.codeit.otboo.domain.feed.dto.mapper;

import com.codeit.otboo.domain.clothes.management.entity.Clothes;
import com.codeit.otboo.domain.feed.dto.response.FeedOotdResponse;
import com.codeit.otboo.domain.feed.dto.response.FeedResponse;
import com.codeit.otboo.domain.feed.entity.Feed;
import com.codeit.otboo.domain.feed.entity.FeedWeather;
import com.codeit.otboo.domain.weather.dto.mapper.WeatherMapper;
import com.codeit.otboo.domain.weather.dto.response.PrecipitationResponse;
import com.codeit.otboo.domain.weather.dto.response.TemperatureResponse;
import com.codeit.otboo.domain.weather.dto.response.WeatherSummaryResponse;

import java.util.List;

public class FeedMapper {

    public static FeedResponse toDto(Feed feed) {
        return toDto(feed, false); // Feed 생성 시 기본 false
    }

    public static FeedResponse toDto(Feed feed, boolean likedByMe) {
        return FeedResponse.builder()
                .id(feed.getId())
                .createdAt(feed.getCreatedAt())
                .updatedAt(feed.getUpdatedAt())
//                .authorResponse(UserMapper.toDto(feed.getAuthor()))
                .weatherResponse(WeatherMapper.toSummaryDto(feed.getWeather()))
                .ootds(toOotdDto(feed.getClothesList()))
                .content(feed.getContent())
                .likeCount(feed.getLikeCount())
                .commentCount(feed.getCommentCount())
                .likedByMe(likedByMe)
                .build();
    }

    private static List<FeedOotdResponse> toOotdDto(List<Clothes> clothes) {
        return null;
    }
}
