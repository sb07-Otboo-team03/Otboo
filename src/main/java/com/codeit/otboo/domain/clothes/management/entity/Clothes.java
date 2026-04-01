package com.codeit.otboo.domain.clothes.management.entity;

import com.codeit.otboo.domain.BaseUpdatableEntity;
import com.codeit.otboo.domain.binarycontent.entity.BinaryContent;
import com.codeit.otboo.domain.clothes.attribute.attributevalue.entity.ClothesAttributeValue;
import com.codeit.otboo.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;

@Entity
@Table(name = "clothes")
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@AllArgsConstructor
public class Clothes extends BaseUpdatableEntity {

    @Column(nullable = false, length = 30)
    private String name;

    @Column(nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private ClothesType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="image_id")
    BinaryContent binaryContent;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "clothes_attribute_mappings",
            joinColumns = @JoinColumn(name = "clothes_id"),
            inverseJoinColumns = @JoinColumn(name = "attribute_value_id")
    )
    private Set<ClothesAttributeValue> values;
}