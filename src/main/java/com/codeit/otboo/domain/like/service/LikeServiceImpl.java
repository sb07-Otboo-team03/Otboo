package com.codeit.otboo.domain.like.service;

import com.codeit.otboo.domain.feed.entity.Feed;
import com.codeit.otboo.domain.feed.repository.FeedRepository;
import com.codeit.otboo.domain.like.entity.Like;
import com.codeit.otboo.domain.like.repository.LikeRepository;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LikeServiceImpl implements LikeService {

    private final FeedRepository feedRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;

    @Override
    @Transactional
    public void feedLike(UUID feedId, UUID userId) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new IllegalArgumentException("feedId is invalid"));

        User user = userRepository.findById(userId)
                        .orElseThrow(() -> new IllegalArgumentException("userId is invalid"));

        if (likeRepository.existsByFeedIdAndUserId(feedId, userId))
            throw new IllegalArgumentException("already liked");

        Like like = new Like(user, feed);
        likeRepository.save(like);
        feed.increaseLike();
    }

    @Override
    @Transactional
    public void feedUnlike(UUID feedId, UUID userId) {
        Like like = likeRepository.findByFeedIdAndUserId(feedId, userId)
                .orElseThrow(() -> new IllegalArgumentException("like is invalid"));

        Feed feed = like.getFeed();
        likeRepository.delete(like);
        feed.decreaseLike();
    }
}
