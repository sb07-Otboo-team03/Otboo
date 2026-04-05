package com.codeit.otboo.domain.user.entity;

import com.codeit.otboo.domain.BaseUpdatableEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "temporary_passwords")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TemporaryPassword extends BaseUpdatableEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "expired", nullable = false)
    private boolean expired;

    @Builder
    public TemporaryPassword(User user, String password, LocalDateTime expiresAt, boolean expired) {
        this.user = user;
        this.password = password;
        this.expiresAt = expiresAt;
        this.expired = expired;
    }

    public void update(String encoded, LocalDateTime expiresAt) {
        this.password = encoded;
        this.expiresAt = expiresAt;
        this.expired = false;
    }

    public boolean isValid() {
        return !expired && expiresAt.isAfter(LocalDateTime.now());
    }

    public void expire() {
        this.expired = true;
    }



}