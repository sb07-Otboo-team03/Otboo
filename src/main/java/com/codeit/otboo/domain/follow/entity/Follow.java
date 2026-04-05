package com.codeit.otboo.domain.follow.entity;

import com.codeit.otboo.domain.BaseEntity;
import com.codeit.otboo.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "follows"
    , uniqueConstraints = {
        @UniqueConstraint(columnNames = {"follower_id", "followee_id"})
    }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Follow extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "follower_id",
            nullable = true,
            foreignKey = @ForeignKey(
                    name = "fk_follows_followers",
                    foreignKeyDefinition = "FOREIGN KEY (follower_id) REFERENCES users(id) ON DELETE SET NULL"
            )
    )
    private User follower;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "followee_id",
            nullable = true,
            foreignKey = @ForeignKey(
                    name = "fk_follows_followees",
                    foreignKeyDefinition = "FOREIGN KEY (followee_id) REFERENCES users(id) ON DELETE SET NULL"
            )
    )
    private User followee;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    public Follow(User follower, User followee) {
        this.follower = follower;
        this.followee = followee;
    }
}
