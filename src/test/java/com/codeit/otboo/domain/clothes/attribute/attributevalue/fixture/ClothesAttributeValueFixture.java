package com.codeit.otboo.domain.clothes.attribute.attributevalue.fixture;

import com.codeit.otboo.domain.clothes.attribute.attributedef.entity.ClothesAttributeDef;
import com.codeit.otboo.domain.clothes.attribute.attributedef.fixture.ClothesAttributeDefFixture;
import com.codeit.otboo.domain.clothes.attribute.attributevalue.entity.ClothesAttributeValue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ClothesAttributeValueFixture {
    private static int num = 1;

    public static ClothesAttributeValue create(){
        ClothesAttributeDef newDef = ClothesAttributeDefFixture.create();
        return ClothesAttributeValue.builder()
                .id(UUID.randomUUID())
                .selectableValue("속성 선택지" + num++)
                .attributeDef(newDef)
                .build();
    }

    public static ClothesAttributeValue create(UUID definitionId, String selectableValue){
        ClothesAttributeDef newDef = ClothesAttributeDefFixture.create(definitionId);
        return ClothesAttributeValue.builder()
                .id(UUID.randomUUID())
                .selectableValue(selectableValue)
                .attributeDef(newDef)
                .build();
    }

    public static ClothesAttributeValue create(ClothesAttributeDef definition, String selectableValue){
        return ClothesAttributeValue.builder()
                .id(UUID.randomUUID())
                .selectableValue(selectableValue)
                .attributeDef(definition)
                .build();
    }

    public static List<ClothesAttributeValue> createList(ClothesAttributeDef definition){
        List<ClothesAttributeValue> list = new ArrayList<>();
        for(int i = 1; i <= 5; i++){
            list.add(
                create(definition, definition.getName() + "의 선택가능 값" + i)
            );
        }
        return list;
    }
}
