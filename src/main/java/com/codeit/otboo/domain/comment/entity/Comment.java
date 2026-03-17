package com.codeit.otboo.domain.comment.entity;

import com.codeit.otboo.domain.BaseEntity;
import com.codeit.otboo.domain.feed.entity.Feed;
import com.codeit.otboo.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Entity
@Table(name = "comments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseEntity {

    @Column(nullable = false, length = 255)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)  // null 허용
    @JoinColumn(name = "author_id")
    private User author;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "feed_id")
    private Feed feed;

    @Builder
    public Comment(String content, Feed feed, User author) {
        this.content = content;
        this.author = author;
        this.feed = feed;
    }
}
