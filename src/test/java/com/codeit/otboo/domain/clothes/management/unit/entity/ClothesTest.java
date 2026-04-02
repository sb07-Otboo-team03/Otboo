package com.codeit.otboo.domain.clothes.management.unit.entity;

import com.codeit.otboo.domain.binarycontent.entity.BinaryContent;
import com.codeit.otboo.domain.binarycontent.fixture.BinaryContentFixture;
import com.codeit.otboo.domain.clothes.attribute.attributevalue.entity.ClothesAttributeValue;
import com.codeit.otboo.domain.clothes.attribute.attributevalue.fixture.ClothesAttributeValueFixture;
import com.codeit.otboo.domain.clothes.management.entity.Clothes;
import com.codeit.otboo.domain.clothes.management.entity.ClothesType;
import com.codeit.otboo.domain.clothes.management.fixture.ClothesFixture;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.user.fixture.UserFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ClothesTest {
    @Nested
    @DisplayName("옷 생성")
    class createClothes{
        @Test
        @DisplayName("""
            성공: 유효한 파라미터들과 values 리스트가 들어올 경우
            해당 파라미터들과 속성 이름 오름차순으로 정렬된 values로 옷이 생성된다.
        """)
        void createClothes_Success(){
            // given
            User user = UserFixture.create();
            BinaryContent binaryContent = BinaryContentFixture.create();
            List<ClothesAttributeValue> values = List.of(
                    ClothesAttributeValueFixture.create("다람이", "값1"),
                    ClothesAttributeValueFixture.create("가람이", "값1"),
                    ClothesAttributeValueFixture.create("나람이", "값1")
            );
            String name = "셔츠";
            ClothesType type = ClothesType.TOP;

            // when
            Clothes clothes = new Clothes(
                    name, type, user, binaryContent, values);

            // then
            assertThat(clothes.getName()).isEqualTo(name);
            assertThat(clothes.getType()).isEqualTo(type);
            assertThat(clothes.getValues()).isSortedAccordingTo(
                Comparator.comparing(
    attributeValue -> attributeValue.getAttributeDef().getName()
                )
            );
        }
    }
    
    @Nested
    @DisplayName("옷 수정")
    class updateClothes{
        @Test
        @DisplayName("""
            성공: 유효한 파라미터들과 value 리스트가 들어올 경우
            기존 value들이 삭제되고
            해당 파라미터들과 속성 이름 오름차순으로  정렬된 value 로 옷이 수정된다.
        """)
        void updateClothes_Success(){
            // given
            Clothes clothes = ClothesFixture.create();
            String name = "새 옷";
            ClothesType type = ClothesType.ETC;
            BinaryContent binaryContent = BinaryContentFixture.create();
            List<ClothesAttributeValue> values = List.of(
                    ClothesAttributeValueFixture.create("애플", "값1"),
                    ClothesAttributeValueFixture.create("동동주", "값1"),
                    ClothesAttributeValueFixture.create("도토리", "값1")
            );

            // when
            clothes.updateClothes(name, type, binaryContent, values);

            // then
            assertThat(clothes.getName()).isEqualTo(name);
            assertThat(clothes.getType()).isEqualTo(type);
            assertThat(clothes.getValues()).isSortedAccordingTo(
                Comparator.comparing(
                    attributeValue -> attributeValue.getAttributeDef().getName()
                )
            );
        }
    }
}
