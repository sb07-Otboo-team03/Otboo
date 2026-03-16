package com.codeit.otboo.domain.feed.entity;


import com.codeit.otboo.domain.BaseUpdatableEntity;
import com.codeit.otboo.domain.comment.entity.Comment;
import com.codeit.otboo.domain.like.entity.Like;
import com.codeit.otboo.domain.profile.Profile;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.weather.entity.Weather;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    private User author; // name + profileUrl 은 profile 에 있어!!

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "weather_id")
    private Weather weather;

    @JsonManagedReference
    @Setter(AccessLevel.PROTECTED)
    @OneToMany(mappedBy = "feed", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @JsonManagedReference
    @Setter(AccessLevel.PROTECTED)
    @OneToMany(mappedBy = "feed", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Like> likes = new ArrayList<>();

//    @ManyToMany(mappedBy = "feeds")
//    private List<Clothes> clothesList = new ArrayList<>();

    public Feed(String content, int likeCount, int commentCount, User author, Weather weather) {
        this.content = content;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.author = author;
        this.weather = weather;
//        this.clothesList = clothesList;
    }
}
