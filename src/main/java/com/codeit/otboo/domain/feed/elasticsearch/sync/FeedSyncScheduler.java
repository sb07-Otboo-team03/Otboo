package com.codeit.otboo.domain.feed.elasticsearch.sync;

import com.codeit.otboo.domain.feed.elasticsearch.service.FeedDocumentService;
import com.codeit.otboo.domain.feed.entity.Feed;
import com.codeit.otboo.domain.feed.repository.FeedRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class FeedSyncScheduler {

    private final FeedRepository feedRepository;
    private final FeedDocumentService feedDocumentService;

    @Scheduled(cron = "0 0 1 * * *")
    @Transactional(readOnly = true)
    public void syncDailyFeed() {
        try {
            LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
            List<Feed> recentFeeds = feedRepository.findByUpdatedAtAfter(yesterday);

            if (recentFeeds.isEmpty()) return;

            feedDocumentService.syncAllFeeds(recentFeeds);
            log.debug("복구된 피드 수: {}", recentFeeds.size());
        } catch (Exception e) {
            log.warn("Elasticsearch 동기화 실패", e);
        }
    }
}
