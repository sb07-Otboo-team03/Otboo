package com.codeit.otboo.domain.clothes.management.fixture;

import com.codeit.otboo.domain.binarycontent.entity.BinaryContent;
import com.codeit.otboo.domain.binarycontent.fixture.BinaryContentFixture;
import com.codeit.otboo.domain.clothes.attribute.attributevalue.entity.ClothesAttributeValue;
import com.codeit.otboo.domain.clothes.attribute.attributevalue.fixture.ClothesAttributeValueFixture;
import com.codeit.otboo.domain.clothes.management.dto.request.ClothesCreateRequest;
import com.codeit.otboo.domain.clothes.management.entity.Clothes;
import com.codeit.otboo.domain.clothes.management.entity.ClothesType;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.user.fixture.UserFixture;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ClothesFixture {

    private static int num = 1;

    public static Clothes create(){
        Clothes newClothes = new Clothes(
                "옷" + num,
                ClothesType.ETC,
                UserFixture.create(),
                BinaryContentFixture.create(),
                List.of(
                        ClothesAttributeValueFixture.create(),
                        ClothesAttributeValueFixture.create(),
                        ClothesAttributeValueFixture.create()
                )
        );
        ReflectionTestUtils.setField(newClothes, "id", UUID.randomUUID());
        return newClothes;
    }
    
    public static Clothes create(
            String name,
            ClothesType type,
            User user,
            BinaryContent binaryContent,
            List<ClothesAttributeValue> values){
        Clothes newClothes = new Clothes(
                name,
                type,
                user,
                binaryContent,
                values
        );
        ReflectionTestUtils.setField(newClothes, "id", UUID.randomUUID());
        return newClothes;
    }
    public static Clothes create(ClothesCreateRequest request){
        User newUser = UserFixture.create();
        ReflectionTestUtils.setField(newUser, "id", request.ownerId());
        Clothes newClothes = new Clothes(
                request.name(),
                request.type(),
                newUser,
                null,
                request.attributes().stream()
                        .map(attributeRequest ->
                                ClothesAttributeValueFixture.create(
                                        attributeRequest.definitionId(), attributeRequest.value())
                        ).collect(Collectors.toList())
        );
        ReflectionTestUtils.setField(newClothes, "id", UUID.randomUUID());
        return newClothes;
    }

    public static Clothes create(ClothesCreateRequest request, BinaryContent binaryContent){
        User newUser = UserFixture.create();
        ReflectionTestUtils.setField(newUser, "id", request.ownerId());
        Clothes newClothes = new Clothes(
                request.name(),
                request.type(),
                newUser,
                binaryContent,
                request.attributes().stream()
                    .map(attributeRequest ->
                        ClothesAttributeValueFixture.create(
                                attributeRequest.definitionId(), attributeRequest.value())
                    ).collect(Collectors.toList())
        );
        ReflectionTestUtils.setField(newClothes, "id", UUID.randomUUID());
        return newClothes;
    }
}