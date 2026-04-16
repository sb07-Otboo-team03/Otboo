package com.codeit.otboo.domain.clothes.attribute.attributedef.entity;

import com.codeit.otboo.domain.BaseUpdatableEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "clothes_attribute_defs")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ClothesAttributeDef extends BaseUpdatableEntity {

    @Column(length = 30)
    private String name;

    public ClothesAttributeDef(String name) {
        this.name = name;
    }

    public void updateClothesAttributeDefName(String name) {
        this.name = name;
    }
}

