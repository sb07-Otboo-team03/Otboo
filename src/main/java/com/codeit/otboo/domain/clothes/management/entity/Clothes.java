package com.codeit.otboo.domain.clothes.management.entity;

import com.codeit.otboo.domain.BaseEntity;
import com.codeit.otboo.domain.BaseUpdatableEntity;
import com.codeit.otboo.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "clothes")
@Getter
@Builder
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@AllArgsConstructor
public class Clothes extends BaseEntity {

    @Column(nullable = false, length = 30)
    private String name;

    @Column
    private String imageUrl;

    @Column(nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private ClothesType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    // JoinTable
}