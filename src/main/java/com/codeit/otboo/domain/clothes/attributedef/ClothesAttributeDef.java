package com.codeit.otboo.domain.clothes.attributedef;

import com.codeit.otboo.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "clothes_attribute_defs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ClothesAttributeDef extends BaseEntity {

    @Column(length = 30)
    private String name;

    public ClothesAttributeDef(String name) {
        this.name = name;
    }
}

