package com.codeit.otboo.domain.follow.entity;

import com.codeit.otboo.domain.BaseEntity;
import com.codeit.otboo.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "follows")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Follow extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "follower_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_follows_followers",
                    foreignKeyDefinition = "FOREIGN KEY (follower_id) REFERENCES users(id) ON DELETE SET NULL"
            )
    )
    private User follower;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "followee_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_follows_followees",
                    foreignKeyDefinition = "FOREIGN KEY (followee_id) REFERENCES users(id) ON DELETE SET NULL"
            )
    )
    private User followee;

    public Follow(User follower, User followee) {
        this.follower = follower;
        this.followee = followee;
    }
}
