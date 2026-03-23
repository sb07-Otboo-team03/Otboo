package com.codeit.otboo.domain.feed.fixture;

import com.codeit.otboo.domain.feed.dto.request.FeedCreateRequest;
import com.codeit.otboo.domain.feed.entity.Feed;
import com.codeit.otboo.domain.feed.entity.FeedWeather;
import com.codeit.otboo.domain.user.entity.User;
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
}
