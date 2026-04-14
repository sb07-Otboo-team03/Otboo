package com.codeit.otboo.domain.feed.elasticsearch.event.listener;

import com.codeit.otboo.domain.feed.elasticsearch.event.FeedDeletedEvent;
import com.codeit.otboo.domain.feed.elasticsearch.event.FeedSyncEvent;
import com.codeit.otboo.domain.feed.elasticsearch.event.FeedUpdatedEvent;
import com.codeit.otboo.domain.feed.elasticsearch.event.LikeUpdatedEvent;
import com.codeit.otboo.domain.feed.elasticsearch.service.FeedDocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class FeedDocumentEventListener {

    private final FeedDocumentService feedSearchService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onFeedSync(FeedSyncEvent event) {
        feedSearchService.toDocument(event);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onFeedLiked(LikeUpdatedEvent event) {
        feedSearchService.updateLikeCount(event.feedId(), event.updatedLikeCount());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onFeedUpdated(FeedUpdatedEvent event) {
        feedSearchService.updateContent(event.feedId(), event.content());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onFeedDeleted(FeedDeletedEvent event) {
        feedSearchService.deleteFeed(event.feedId());
    }
}
