package com.codeit.otboo.domain.profile.entity;

import com.codeit.otboo.domain.BaseUpdatableEntity;
import com.codeit.otboo.domain.binarycontent.entity.BinaryContent;
import com.codeit.otboo.domain.user.entity.User;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "profiles")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Profile extends BaseUpdatableEntity {

    @JsonBackReference
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name ="name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender gender;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Embedded
    private Location location;

    @Min(1)
    @Max(5)
    @Column(name = "temperature_sensitivity", nullable = false)
    private int temperatureSensitivity = 3;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="profile_image_id")
    private BinaryContent binaryContent;

    public void update(
            String name,
            Gender gender,
            LocalDate birthDate,
            Location location,
            Integer temperatureSensitivity,
            BinaryContent binaryContent
    ) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }

        if (gender != null) {
            this.gender = gender;
        }

        if (birthDate != null) {
            this.birthDate = birthDate;
        }

        if (location != null) {
            this.location = location;
        }

        if (temperatureSensitivity != null) {
            this.temperatureSensitivity = temperatureSensitivity;
        }

        if (binaryContent != null) {
            this.binaryContent = binaryContent;
        }
    }

    @Builder
    public Profile(User user, String name) {
        setUser(user);
        this.name = name;
    }

    private void setUser(User user) {
        this.user = user;
        user.setProfile(this);
    }
}
