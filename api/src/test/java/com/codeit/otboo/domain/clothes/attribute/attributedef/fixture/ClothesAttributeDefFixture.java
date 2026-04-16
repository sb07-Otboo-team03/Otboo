package com.codeit.otboo.domain.clothes.attribute.attributedef.fixture;

import com.codeit.otboo.domain.clothes.attribute.attributedef.entity.ClothesAttributeDef;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

public class ClothesAttributeDefFixture {
    private static int num = 1;

    public static ClothesAttributeDef create(){
        ClothesAttributeDef newDef = new ClothesAttributeDef("테스트 옷 속성" + num++);
        ReflectionTestUtils.setField(newDef, "id", UUID.randomUUID());
        return newDef;
    }

    public static ClothesAttributeDef create(UUID definitionId){
        ClothesAttributeDef newDef = new ClothesAttributeDef("테스트 옷 속성" + num++);
        ReflectionTestUtils.setField(newDef, "id", definitionId);
        return newDef;
    }
}
