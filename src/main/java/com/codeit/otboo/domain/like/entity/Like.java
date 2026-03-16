package com.codeit.otboo.domain.like.entity;

import com.codeit.otboo.domain.feed.entity.Feed;
import com.codeit.otboo.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(
    name = "likes",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "feed_id"})
    })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Like {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "feed_id")
    private Feed feed;

    @Builder
    public Like(User user, Feed feed) {
        this.user = user;
        setFeed(feed);
    }

    protected void setFeed(Feed feed) {
        this.feed = feed;
        feed.addLike(this);
    }
}
