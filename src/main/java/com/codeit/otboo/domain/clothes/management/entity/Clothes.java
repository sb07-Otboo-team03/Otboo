package com.codeit.otboo.domain.clothes.management.entity;

import com.codeit.otboo.domain.BaseUpdatableEntity;
import com.codeit.otboo.domain.binarycontent.entity.BinaryContent;
import com.codeit.otboo.domain.clothes.attribute.attributevalue.entity.ClothesAttributeValue;
import com.codeit.otboo.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Comparator;
import java.util.List;

@Entity
@Table(name = "clothes")
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
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
    private List<ClothesAttributeValue> values;

    public Clothes(
           String name,
           ClothesType type,
           User owner,
           BinaryContent binaryContent,
           List<ClothesAttributeValue> values
    ) {
        sortValues();
        this.name = name;
        this.type = type;
        this.owner = owner;
        this.binaryContent = binaryContent;
        this.values = values;
    }

    public void updateClothes(
            String name,
            ClothesType type,
            BinaryContent binaryContent,
            List<ClothesAttributeValue> values){
        super.touch();
        sortValues();
        this.name = name;
        this.type = type;
        this.binaryContent = binaryContent;
        this.values.clear();
        this.values.addAll(values);
    }
    private void sortValues(){
        values.sort(Comparator.comparing(
                attributeValue -> attributeValue.getAttributeDef().getName())
        );
    }
}