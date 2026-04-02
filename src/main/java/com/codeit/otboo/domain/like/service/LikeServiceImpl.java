package com.codeit.otboo.domain.like.service;

import com.codeit.otboo.domain.feed.entity.Feed;
import com.codeit.otboo.domain.feed.exception.FeedNotFoundException;
import com.codeit.otboo.domain.feed.repository.FeedRepository;
import com.codeit.otboo.domain.like.entity.Like;
import com.codeit.otboo.domain.like.exception.LikeAlreadyExistsException;
import com.codeit.otboo.domain.like.exception.LikeNotFoundException;
import com.codeit.otboo.domain.like.repository.LikeRepository;
import com.codeit.otboo.domain.notification.dto.NotificationDto;
import com.codeit.otboo.domain.notification.dto.NotificationLevel;
import com.codeit.otboo.domain.notification.entity.Notification;
import com.codeit.otboo.domain.notification.mapper.NotificationMapper;
import com.codeit.otboo.domain.notification.repository.NotificationRepository;
import com.codeit.otboo.domain.sse.event.SseEvent;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.user.exception.UserNotFoundException;
import com.codeit.otboo.domain.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LikeServiceImpl implements LikeService {

    private final FeedRepository feedRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final NotificationMapper notificationMapper;
    private final NotificationRepository notificationRepository;

    @Override
    @Transactional
    public void feedLike(UUID feedId, UUID userId) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new FeedNotFoundException(feedId));

        User user = userRepository.findById(userId)
                        .orElseThrow(() -> new UserNotFoundException(userId));

        if (likeRepository.existsByFeedIdAndUserId(feedId, userId))
            throw new LikeAlreadyExistsException(feedId, userId);

        Like like = new Like(user, feed);
        LocalDateTime now = LocalDateTime.now();
        likeRepository.save(like);
        feed.increaseLike();

        Notification notification = Notification.builder()
            .title(user.getProfile().getName() + "님이 내 피드를 좋아합니다.")
            .content(feed.getContent())
            .level(NotificationLevel.INFO)
            .receiver(feed.getAuthor())
            .build();

        notificationRepository.save(notification);

        NotificationDto notificationDto = notificationMapper.toEventDto(notification);
        eventPublisher.publishEvent( new SseEvent(List.of(notificationDto)));

    }

    @Override
    @Transactional
    public void feedUnlike(UUID feedId, UUID userId) {
        Like like = likeRepository.findByFeedIdAndUserId(feedId, userId)
                .orElseThrow(() -> new LikeNotFoundException(feedId, userId));

        Feed feed = like.getFeed();
        likeRepository.delete(like);
        feed.decreaseLike();
    }
}
