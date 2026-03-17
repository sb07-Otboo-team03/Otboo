package com.codeit.otboo.domain.profile.entity;

import com.codeit.otboo.domain.BaseUpdatableEntity;
import com.codeit.otboo.domain.binarycontent.entity.BinaryContent;
import com.codeit.otboo.domain.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "profiles")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Profile extends BaseUpdatableEntity {

    @OneToOne(fetch = FetchType.LAZY)
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
    BinaryContent binaryContent;


    @Builder
    public Profile(User user, String name) {
        this.user = user;
        this.name = name;
    }

}
