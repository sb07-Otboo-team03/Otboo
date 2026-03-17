package com.codeit.otboo.domain.clothes.attribute.attributevalue.entity;

import com.codeit.otboo.domain.clothes.attribute.attributedef.entity.ClothesAttributeDef;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "clothes_attribute_values")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ClothesAttributeValue {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "selectable_value", nullable = false, length = 30)
    private String selectableValue;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "definition_id", nullable = false)
    private ClothesAttributeDef attributeDef;

}
