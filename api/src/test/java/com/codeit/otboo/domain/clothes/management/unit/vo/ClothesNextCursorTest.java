package com.codeit.otboo.domain.clothes.management.unit.vo;

import com.codeit.otboo.domain.clothes.management.entity.Clothes;
import com.codeit.otboo.domain.clothes.management.fixture.ClothesFixture;
import com.codeit.otboo.domain.clothes.management.vo.ClothesNextCursor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ClothesNextCursorTest {
    @Nested
    @DisplayName("slice의 hasNext가 false")
    class hasNextIsFalse{
        @Test
        @DisplayName("hasNext가 false 이면 cursor는 null, after는 null 을 반환한다")
        void hasNextIsFalse_Success(){
            // given
            Slice<Clothes> slice = new SliceImpl<>(
                    List.of(ClothesFixture.create(), ClothesFixture.create(), ClothesFixture.create()),
                    PageRequest.of(0, 20),
                    false);

            // when
            ClothesNextCursor clothesNextCursor = ClothesNextCursor.from(slice);

            // then
            assertThat(clothesNextCursor.getCursor()).isNull();
            assertThat(clothesNextCursor.getAfter()).isNull();
        }
    }

    @Nested
    @DisplayName("slice의 content가 비어있을 때")
    class contentIsEmpty{
        @Test
        @DisplayName("content가 empty 상태일 때 cursor는 null, after는 null을 반환한다")
        void contentIsEmpty_Success(){
            // given
            Slice<Clothes> slice = new SliceImpl<>(
                    List.of(), PageRequest.of(0, 20), true);

            // when
            ClothesNextCursor clothesNextCursor = ClothesNextCursor.from(slice);

            // then
            assertThat(clothesNextCursor.getCursor()).isNull();
            assertThat(clothesNextCursor.getAfter()).isNull();
        }
    }

    @Nested
    @DisplayName("다음 페이지가 존재할 때")
    class hasNextIsTrue{
        // 원활한 테스트 환경을 위해 페이지 사이즈를 2로 설정
        @Test
        @DisplayName("""
            hasNext가 true일 때
            cursor는 현재 페이지 마지막 요소의 생성일,
            after는 현재 페이지 마지막 요소의 UUID
            값을 반환한다.
        """)
        void hasNextIsTrue_Success(){
            // given
            Slice<Clothes> slice = new SliceImpl<>(
                List.of(ClothesFixture.create(), ClothesFixture.create(), ClothesFixture.create()),
                PageRequest.of(0, 2),
                true
            );
            Clothes pageLastClothes = slice.getContent().get(slice.getContent().size() - 1);


            // when
            ClothesNextCursor clothesNextCursor = ClothesNextCursor.from(slice);

            // then
            assertThat(clothesNextCursor.getCursor())
                    .isEqualTo(pageLastClothes.getCreatedAt().toString());
            assertThat(clothesNextCursor.getAfter()).isEqualTo(pageLastClothes.getId());
        }
    }
}
