package com.codeit.otboo.domain.feed.fixture;

import com.codeit.otboo.domain.feed.elasticsearch.document.FeedDocument;
import com.codeit.otboo.domain.feed.entity.Feed;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FeedDocumentFixture {

    public static List<FeedDocument> createFeedDocument(int n) {

        List<FeedDocument> docs = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            docs.add(FeedDocument.builder()
                    .id(UUID.randomUUID().toString())
                    .content("content " + i % 2)
                    .skyStatus(i % 2 == 0 ? "CLEAR" : "CLOUDY")
                    .precipitationType(i % 2 == 0 ? "NONE" : "RAIN")
                    .authorId(UUID.randomUUID().toString())
                    .createdAt(100L - i)
                    .likeCount((long) i)
                    .build());
        }

        return docs;
    }

    public static List<FeedDocument> createFeedDocument(List<Feed> feedList, int n) {

        List<FeedDocument> docs = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            Long epochMilli = feedList.get(i).getCreatedAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

            docs.add(FeedDocument.builder()
                    .id(feedList.get(i).getId().toString())
                    .createdAt(epochMilli)
                    .likeCount(feedList.get(i).getLikeCount())
                    .build());
        }

        return docs;
    }
}
