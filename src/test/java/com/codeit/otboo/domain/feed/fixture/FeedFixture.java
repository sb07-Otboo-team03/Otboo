package com.codeit.otboo.domain.feed.fixture;

import com.codeit.otboo.domain.feed.dto.request.FeedCreateRequest;
import com.codeit.otboo.domain.feed.entity.Feed;
import com.codeit.otboo.domain.feed.entity.FeedWeather;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.weather.entity.PrecipitationType;
import com.codeit.otboo.domain.weather.entity.SkyStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FeedFixture {

    public static FeedCreateRequest dummyFeedRequest(UUID userId, UUID weatherId) {

        return new FeedCreateRequest(userId, weatherId, null, "content");
    }

    public static Feed create(FeedCreateRequest request) {

        User user = new User("otboo@a.a", "otboo123");
        ReflectionTestUtils.setField(user, "id", request.authorId());

        FeedWeather weather = FeedWeather.builder().weatherId(request.weatherId()).build();

        return new Feed(request.content(), user, weather, null);
    }

    public static List<Feed> createFeedCursor(int count) {

        List<Feed> feedList = new ArrayList<>();

        User user = new User("otboo@a.a", "otboo123");
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());

        for (int i = 0; i < count; i++) {
            Feed feed = new Feed("content" + i, user, null, null);
            ReflectionTestUtils.setField(feed, "id", UUID.randomUUID());
            ReflectionTestUtils.setField(feed, "createdAt", LocalDateTime.now().minusDays(i));
            ReflectionTestUtils.setField(feed, "likeCount", i);
            feedList.add(feed);
        }
        return feedList;
    }

    public static List<Feed> createFeed(int count, User user) {

        List<Feed> feedList = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            FeedWeather weather = FeedWeather.builder()
                    .weatherId(UUID.randomUUID())
                    .skyStatus(i % 2 == 0 ? SkyStatus.CLEAR : SkyStatus.CLOUDY)
                    .precipitationType(i % 2 == 0 ? PrecipitationType.NONE : PrecipitationType.RAIN)
                    .precipitationAmount(0.0)
                    .precipitationProbability(0.0)
                    .temperatureCurrent(0.0)
                    .temperatureComparedToDayBefore(0.0)
                    .temperatureMax(0.0)
                    .temperatureMin(0.0)
                    .build();

            Feed feed = new Feed(
                    i % 2 == 0 ? "가" + i : "나" + i,
                    user,
                    weather,
                    null);
            ReflectionTestUtils.setField(feed, "createdAt", LocalDateTime.now().minusDays(i));
            ReflectionTestUtils.setField(feed, "likeCount", i);
            feedList.add(feed);
        }
        return feedList;
    }
}
