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

    @OneToMany(mappedBy = "feed", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "feed", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Like> likes = new ArrayList<>();

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

    public Comment addComment(String content, User author) {
        Comment comment = new Comment(content, this, author);
        this.comments.add(comment);
        this.commentCount++;

        return comment;
    }

    public Like addLike(User user) {
        Like like = new Like(user, this);
        this.likes.add(like);
        this.likeCount++;

        return like;
    }

    public void removeLike(Like like) {
        if (likes.remove(like))
            likeCount = Math.max(0, likeCount - 1);
    }

    // Protype X
    public void removeComment(Comment comment) {
        if (comments.remove(comment))
            commentCount = Math.max(0, commentCount - 1);
    }

    public void updateContent(String content) {
        this.content = content;
    }
}
