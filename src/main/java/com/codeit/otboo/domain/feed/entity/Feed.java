package com.codeit.otboo.domain.feed.entity;

import com.codeit.otboo.domain.BaseUpdatableEntity;
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
    private int likeCount = 0;

    @Column(name = "comment_count", nullable = false)
    private int commentCount = 0;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id")
    private User author;

    @Embedded
    private WeatherInformation weather;

    @OneToMany(mappedBy = "feed", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "feed", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Like> likes = new ArrayList<>();

    @Builder
    public Feed(String content, User author, WeatherInformation weather) {
        this.content = content;
        this.author = author;
        this.weather = weather;
    }

    public void addComment(Comment comment) {
        this.comments.add(comment);
        this.commentCount++;
    }
    public void addLike(Like like) {
        this.likes.add(like);
        this.likeCount++;
    }
    public void removeLike(Like like) {
        this.likes.remove(like);
        this.likeCount--;
    }

    // Protype X
    public void removeComment(Comment comment) {
        this.comments.remove(comment);
        this.commentCount--;
    }
}
