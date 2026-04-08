package com.codeit.otboo.domain.feed.dto.mapper;

import com.codeit.otboo.domain.clothes.attribute.attributevalue.entity.ClothesAttributeValue;
import com.codeit.otboo.domain.clothes.management.entity.Clothes;
import com.codeit.otboo.domain.feed.dto.response.FeedOotdResponse;
import com.codeit.otboo.domain.feed.dto.response.FeedResponse;
import com.codeit.otboo.domain.feed.entity.Feed;
import com.codeit.otboo.domain.user.mapper.UserMapper;
import com.codeit.otboo.domain.weather.dto.mapper.WeatherMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class FeedMapper {

    private final UserMapper userMapper;
    private final WeatherMapper weatherMapper;
    private final OotdMapper ootdMapper;

    public FeedResponse toDto(Feed feed) {
        return toDto(feed, false); // Feed 생성 시 기본 false
    }

    public FeedResponse toDto(Feed feed, boolean likedByMe) {
        return FeedResponse.builder()
                .id(feed.getId())
                .createdAt(feed.getCreatedAt())
                .updatedAt(feed.getUpdatedAt())
                .author(userMapper.toAuthorDto(feed.getAuthor().getId(),
                    feed.getAuthor().getProfile().getName(),
                    feed.getAuthor().getProfile().getBinaryContent().getId()))
                .weather(weatherMapper.toSummaryDto(feed.getWeather()))
                .ootds(toOotdDto(feed.getClothesList()))
                .content(feed.getContent())
                .likeCount(feed.getLikeCount())
                .commentCount(feed.getCommentCount())
                .likedByMe(likedByMe)
                .build();
    }

    private List<FeedOotdResponse> toOotdDto(List<Clothes> clothes) {
        return clothes.stream().map(c -> {
            List<ClothesAttributeValue> values = c.getValues();
            Map<UUID, List<String>> uuidListMap = groupSelectableValuesByAttributeId(values);
            return ootdMapper.toDto(c, uuidListMap);
        }).toList();
    }

    private Map<UUID, List<String>> groupSelectableValuesByAttributeId(
            List<ClothesAttributeValue> allSelectableValues
    ){
        if(allSelectableValues.isEmpty()) return Map.of();
        return allSelectableValues.stream()
                .collect(Collectors.groupingBy(
                        attributeValue -> attributeValue.getAttributeDef().getId(),
                        Collectors.mapping(ClothesAttributeValue::getSelectableValue, Collectors.toList())
                ));
    }
}
