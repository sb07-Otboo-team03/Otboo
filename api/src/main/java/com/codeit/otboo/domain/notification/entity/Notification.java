package com.codeit.otboo.domain.notification.entity;

import com.codeit.otboo.domain.BaseEntity;
import com.codeit.otboo.domain.notification.dto.NotificationLevel;
import com.codeit.otboo.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notifications")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Builder
public class Notification extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 500)
    private String content;

    @Column(nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private NotificationLevel level;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "receiver_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_notifications_receivers",
                    foreignKeyDefinition = "FOREIGN KEY (receiver_id) REFERENCES users(id) ON DELETE CASCADE"
            )
    )
    private User receiver;

    public Notification(String title, String content, NotificationLevel level, User receiver) {
        this.title = title;
        this.content = content;
        this.level = level;
        this.receiver = receiver;
    }
}
