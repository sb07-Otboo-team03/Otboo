package com.codeit.otboo.domain.clothes.management.unit.entity;

import com.codeit.otboo.domain.clothes.management.entity.ClothesType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ClothesTypeTest {
    @Nested
    @DisplayName("문자열을 Enum타입으로 변환")
    class FromString{
        @Test
        @DisplayName("문자열이 null 로 들어올 경우 null 이 반환된다.")
        void fromString_Success_null(){
            // given
            String value = null;

            // when
            ClothesType clothesType = ClothesType.fromString(value);

            // then
            assertThat(clothesType).isNull();
        }

        @Test
        @DisplayName("문자열의 대문자로 들어올 경우 Enum 타입이 반환된다")
        void fromString_Success_AllUpperCase(){
            // given
            String value = "ETC";

            // when
            ClothesType clothesType = ClothesType.fromString(value);

            // then
            assertThat(clothesType).isEqualTo(ClothesType.ETC);
        }

        @Test
        @DisplayName("문자열이 소문자로 들어와도 Enum 타입이 반환된다")
        void fromString_Success_smallCase(){
            // given
            String value = "etc";

            // when
            ClothesType clothesType = ClothesType.fromString(value);

            // then
            assertThat(clothesType).isEqualTo(ClothesType.ETC);
        }
    }
}
