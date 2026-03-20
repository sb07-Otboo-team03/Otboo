package com.codeit.otboo.domain.clothes.management.fixture;

import com.codeit.otboo.domain.binarycontent.entity.BinaryContent;
import com.codeit.otboo.domain.clothes.management.dto.request.ClothesCreateRequest;
import com.codeit.otboo.domain.clothes.management.entity.Clothes;
import com.codeit.otboo.domain.clothes.management.entity.ClothesType;
import com.codeit.otboo.domain.user.entity.User;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

public class ClothesFixture {
    public static Clothes create(){
        Clothes newClothes = new Clothes(
                "옷",
                ClothesType.ETC,
                new User("tester@test.com", "qwer123$"),
                null
        );
        ReflectionTestUtils.setField(newClothes, "id", UUID.randomUUID());
        return newClothes;
    }
    
    public static Clothes create(String name, ClothesType type, User user, BinaryContent binaryContent){
        Clothes newClothes = new Clothes(
                name,
                type,
                user,
                binaryContent
        );
        ReflectionTestUtils.setField(newClothes, "id", UUID.randomUUID());
        return newClothes;
    }

    public static Clothes create(ClothesCreateRequest request, BinaryContent binaryContent){
        User newUser = new User("tester@test.com", "qwer123$");
        ReflectionTestUtils.setField(newUser, "id", request.ownerId());
        Clothes newClothes = new Clothes(
                request.name(),
                request.type(),
                newUser,
                binaryContent
        );
        ReflectionTestUtils.setField(newClothes, "id", UUID.randomUUID());
        return newClothes;
    }
}