package com.codeit.otboo.domain.feed.entity;

import com.codeit.otboo.domain.BaseUpdatableEntity;
import com.codeit.otboo.domain.clothes.management.entity.Clothes;
import com.codeit.otboo.domain.comment.entity.Comment;
import com.codeit.otboo.domain.like.entity.Like;
import com.codeit.otboo.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "feeds")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Feed extends BaseUpdatableEntity {
    @Column(nullable = false, length = 500)
    private String content;

    @Column(name = "like_count", nullable = false)
    private long likeCount = 0;

    @Column(name = "comment_count", nullable = false)
    private int commentCount = 0;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id")
    private User author;

    @Embedded
    private FeedWeather weather;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "clothes_feeds",
            joinColumns = @JoinColumn(name = "feed_id"),
            inverseJoinColumns = @JoinColumn(name = "clothes_id")
    )
    private List<Clothes> clothesList = new ArrayList<>();

    @Builder
    public Feed(String content, User author, FeedWeather weather, List<Clothes> clothesList) {
        this.content = content;
        this.author = author;
        this.weather = weather;
        this.clothesList = clothesList;
    }

    public void increaseComment() {
        this.commentCount++;
    }

    public void increaseLike() {
        this.likeCount++;
    }

    public void decreaseLike() {
        likeCount = Math.max(0, likeCount - 1);
    }

    // Protype X
    public void decreaseComment() {
        commentCount = Math.max(0, commentCount - 1);
    }

    public void updateContent(String content) {
        this.content = content;
    }
}
