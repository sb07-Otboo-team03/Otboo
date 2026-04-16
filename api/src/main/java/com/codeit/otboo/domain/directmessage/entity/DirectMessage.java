package com.codeit.otboo.domain.directmessage.entity;

import com.codeit.otboo.domain.BaseEntity;
import com.codeit.otboo.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
    name = "direct_messages"
    , uniqueConstraints = {
    @UniqueConstraint(columnNames = {"sender_id", "receiver_id"})
}
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DirectMessage extends BaseEntity {
    @Column(name="content", nullable = false, length = 255)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "sender_id",
        nullable = false,
        foreignKey = @ForeignKey(
            name = "fk_direct_messages_senders",
            foreignKeyDefinition = "FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE"
        )
    )
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "receiver_id",
        nullable = false,
        foreignKey = @ForeignKey(
            name = "fk_direct_messages_receivers",
            foreignKeyDefinition = "FOREIGN KEY (receiver_id) REFERENCES users(id) ON DELETE CASCADE"
        )
    )
    private User receiver;

    public DirectMessage(User sender, User receiver, String content) {
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
    }
}
