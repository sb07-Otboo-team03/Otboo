package com.codeit.otboo.domain.feed.dto.mapper;

import com.codeit.otboo.domain.binarycontent.resolver.BinaryContentUrlResolver;
import com.codeit.otboo.domain.clothes.management.entity.Clothes;
import com.codeit.otboo.domain.feed.dto.response.FeedOotdResponse;
import com.codeit.otboo.domain.feed.dto.response.FeedResponse;
import com.codeit.otboo.domain.feed.entity.Feed;
import com.codeit.otboo.domain.user.mapper.UserMapper;
import com.codeit.otboo.domain.weather.dto.mapper.WeatherMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FeedMapper {

    private final UserMapper userMapper;
    private final WeatherMapper weatherMapper;
    private final BinaryContentUrlResolver binaryContentUrlResolver;

    public FeedResponse toDto(Feed feed) {
        return toDto(feed, false); // Feed 생성 시 기본 false
    }

    public FeedResponse toDto(Feed feed, boolean likedByMe) {
        return FeedResponse.builder()
                .id(feed.getId())
                .createdAt(feed.getCreatedAt())
                .updatedAt(feed.getUpdatedAt())
                .userResponse(userMapper.toSummaryDto(feed.getAuthor().getId(),
                    feed.getAuthor().getProfile().getName(),
                    feed.getAuthor().getProfile().getId()))
                .weatherResponse(weatherMapper.toSummaryDto(feed.getWeather()))
                .ootds(toOotdDto(feed.getClothesList()))
                .content(feed.getContent())
                .likeCount(feed.getLikeCount())
                .commentCount(feed.getCommentCount())
                .likedByMe(likedByMe)
                .build();
    }

    private static List<FeedOotdResponse> toOotdDto(List<Clothes> clothes) {
        return null;
        // TODO
    }
}
