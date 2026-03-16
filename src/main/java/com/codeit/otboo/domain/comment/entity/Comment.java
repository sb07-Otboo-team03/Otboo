package com.codeit.otboo.domain.comment.entity;

import com.codeit.otboo.domain.BaseEntity;
import com.codeit.otboo.domain.feed.entity.Feed;
import com.codeit.otboo.domain.user.entity.User;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "feed_id")
    private Feed feed;

    public Comment(String content, Feed feed, User author) {
        this.content = content;
        this.author = author;
        setFeed(feed);
    }

    protected void setFeed(Feed feed) {
        this.feed = feed;
        feed.getComments().add(this);
    }
}
