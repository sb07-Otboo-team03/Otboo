package com.codeit.otboo.domain.clothes.management.slice;

import com.codeit.otboo.domain.clothes.management.dto.query.ClothesSearchCondition;
import com.codeit.otboo.domain.clothes.management.entity.Clothes;
import com.codeit.otboo.domain.clothes.management.entity.ClothesType;
import com.codeit.otboo.domain.clothes.management.repository.ClothesRepository;
import com.codeit.otboo.domain.clothes.management.repository.ClothesRepositoryCustomImpl;
import com.codeit.otboo.domain.clothes.management.util.ClothesTestUtils;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.user.repository.UserRepository;
import com.codeit.otboo.global.config.TestJpaAuditing;
import com.codeit.otboo.global.config.TestQueryDslConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Slice;
import org.springframework.test.context.ActiveProfiles;

import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import({TestQueryDslConfig.class, TestJpaAuditing.class})
public class ClothesRepositoryCustomImplTest {
    @Autowired
    private ClothesRepository clothesRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClothesRepositoryCustomImpl clothesRepositoryCustom;

    @Autowired
    private TestEntityManager entityManager;

    @Nested
    @DisplayName("비어있는 리스트 조회")
    class emyClothesListView {
        @Test
        @DisplayName("""
            content는 빈 배열
            hasNext는 false를 반환한다.
        """)
        void findMyClothes_empty_Success() {
            // given
            User user = User.builder()
                            .email("test@aa.com")
                            .password("1234")
                            .build();
            ClothesTestUtils.createClothesList(user, entityManager,0);
            ClothesSearchCondition query = new ClothesSearchCondition(
                    null, null, 20, null, user.getId());

            // when
            Slice<Clothes> result = clothesRepositoryCustom.findMyClothesList(query);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            assertThat(result.hasNext()).isFalse();
        }
    }

    @Nested
    @DisplayName("limit 미만의 항목 수를 가진 리스트를 조회")
    class smallListView{
        @Test
        @DisplayName("""
               content 는 모든 항목
               hasNext는 false
               를 반환한다
        """)
        void findMyClothes_small_Success(){
            // given
            int totalCount = 15;
            User user = User.builder()
                    .email("test@aa.com")
                    .password("1234")
                    .build();
            ClothesTestUtils.createClothesList(user, entityManager,totalCount);
            ClothesSearchCondition query = new ClothesSearchCondition(
                    null, null, 20, null, user.getId());

            // when
            Slice<Clothes> result = clothesRepositoryCustom.findMyClothesList(query);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(totalCount);
            assertThat(result.hasNext()).isFalse();
        }
    }

    @Nested
    @DisplayName("limit 이상의 항목 수를 가진 리스트를 조회")
    class largeListView{
        @Test
        @DisplayName("""
            각 페이지 정렬이 내림차순으로 잘 되어있고,
            첫 페이지 첫 번째 요소 > 중간 페이지 첫 번째 요소 > 마지막 페이지 첫 번째 요소이며
            첫번째 페이지는
                content 는 전체의 일부항목
                hasNext는 ture
           중간 페이지는
                content는 전체의 일부 항목
                hasNext 는 true
            마지막 페이지는
                content는 전체의 남은 항목
                hasNext 는 false
            을 반환한다.
        """)
        void findMyClothes_large_Success(){
            // given
            int totalCount = 48;
            int pageSize = 20;

            User user = User.builder()
                    .email("test@aa.com")
                    .password("1234")
                    .build();
            ClothesTestUtils.createClothesList(user, entityManager,totalCount);

            // [Page 1]
            // given
            ClothesSearchCondition query1 = new ClothesSearchCondition(
                    null,
                    null,
                    pageSize,
                    null,
                    user.getId()
            );

            // when
            Slice<Clothes> page1 = clothesRepositoryCustom.findMyClothesList(query1);

            // [Page 2]
            // given
            ClothesSearchCondition query2 = new ClothesSearchCondition(
                    page1.getContent().get(page1.getContent().size() - 1).getCreatedAt(),
                    page1.getContent().get(page1.getContent().size() - 1).getId(),
                    pageSize,
                    null,
                    user.getId()
            );

            // when
            Slice<Clothes> page2 = clothesRepositoryCustom.findMyClothesList(query2);

            // [Page 3]
            // given
            ClothesSearchCondition query3 = new ClothesSearchCondition(
                    page2.getContent().get(page2.getContent().size() - 1).getCreatedAt(),
                    page2.getContent().get(page2.getContent().size() - 1).getId(),
                    pageSize,
                    null,
                    user.getId()
            );

            // when
            Slice<Clothes> page3 = clothesRepositoryCustom.findMyClothesList(query3);

            // then
            // 각 페이지는 생성일 내림차순으로 정렬이 잘 되어있다.
            assertThat(page1.getContent()).isSortedAccordingTo(
                    Comparator.comparing(Clothes::getCreatedAt, Comparator.reverseOrder())
                            .thenComparing(Clothes::getId, Comparator.reverseOrder())
            );
            assertThat(page2.getContent()).isSortedAccordingTo(
                    Comparator.comparing(Clothes::getCreatedAt, Comparator.reverseOrder())
                            .thenComparing(Clothes::getId, Comparator.reverseOrder())
            );
            assertThat(page3.getContent()).isSortedAccordingTo(
                    Comparator.comparing(Clothes::getCreatedAt, Comparator.reverseOrder())
                            .thenComparing(Clothes::getId, Comparator.reverseOrder())
            );

            // 페이지들 간의 첫 요소 비교 (내림차순)
            assertThat(page1.getContent().get(0).getCreatedAt())
                    .isAfterOrEqualTo(page2.getContent().get(0).getCreatedAt());
            assertThat(page2.getContent().get(0).getCreatedAt())
                    .isAfterOrEqualTo(page3.getContent().get(0).getCreatedAt());

            // 각 페이지가 있는지 여부 검증
            assertThat(page1.hasNext()).isTrue();
            assertThat(page2.hasNext()).isTrue();
            assertThat(page3.hasNext()).isFalse();

            // 각 페이지 요소 수 검증
            assertThat(page1.getContent()).hasSize(pageSize);
            assertThat(page2.getContent()).hasSize(pageSize);
            assertThat(page3.getContent()).hasSize(totalCount % pageSize);
        }
    }

    @Nested
    @DisplayName("각 카테고리로 옷을 분류해서 볼 수 있다")
    class categoryClothesListView{
        // 원활한 테스트를 위해 pageSize와 totalsize를 통일
        int totalCount = 100;
        int pageSize = 100;

        @Test
        @DisplayName("선택된 카테고리에 포함된 항목만 조회된다")
        void findMyClothes_category_Success(){
            // given
            User user = User.builder()
                    .email("test@aa.com")
                    .password("1234")
                    .build();
            ClothesTestUtils.createClothesList(user, entityManager, totalCount);
            ClothesSearchCondition query = new ClothesSearchCondition(
                    null, null, pageSize, ClothesType.DRESS, user.getId());

            // when
            Slice<Clothes> result = clothesRepositoryCustom.findMyClothesList(query);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getContent().size()).isLessThanOrEqualTo(totalCount);
            assertThat(result.getContent())
                .allSatisfy(clothes ->
                        assertThat(clothes.getType()).isEqualTo(ClothesType.DRESS)
                );
        }

        @Test
        @DisplayName("카테고리가 선택되지 않으면 모든 항목이 조회된다")
        void findMyClothes_category_null_Success(){
            // given
            User user = User.builder()
                    .email("test@aa.com")
                    .password("1234")
                    .build();
            ClothesTestUtils.createClothesList(user, entityManager, totalCount);
            ClothesSearchCondition query = new ClothesSearchCondition(
                    null, null, pageSize, null, user.getId());

            // when
            Slice<Clothes> result = clothesRepositoryCustom.findMyClothesList(query);

            // then
            assertThat(result.getContent()).hasSize(totalCount);
        }
    }

    @Nested
    @DisplayName("전체 갯수")
    class totalCount{
        int totalCount = 100;

        @Test
        @DisplayName("type이 존재하면 해당 타입 개수만 반환한다")
        void findMyClothes_totalCount_Success(){
            // given
            User user = User.builder()
                    .email("test@test.com")
                    .password("1234")
                    .build();
            List<Clothes> clothesList = ClothesTestUtils.createClothesList(
                    user, entityManager, totalCount);
            ClothesType type = ClothesType.DRESS;
            long categoryClothesCount = clothesList.stream()
                    .filter(clothes -> clothes.getType().equals(type))
                    .count();

            // when
            Long count = clothesRepositoryCustom.totalCount(user.getId(), type);

            // then
            assertThat(count).isLessThanOrEqualTo(totalCount);
            assertThat(count).isEqualTo(categoryClothesCount);
        }

        @Test
        @DisplayName("type이 존재하지 않으면 모든 타입 개수를 반환한다")
        void findMyClothes_All_Success(){
            // given
            User user = User.builder()
                    .email("test@test.com")
                    .password("1234")
                    .build();
            ClothesTestUtils.createClothesList(user, entityManager, totalCount);

            // when
            Long count = clothesRepositoryCustom.totalCount(user.getId(), null);

            // then
            assertThat(count).isEqualTo(totalCount);
        }
    }

    @Nested
    @DisplayName("추가 케이스")
    class cursorNotNullAfterNull{
        @Test
        @DisplayName("""
            cursor가 not null, after가 null일 때
            주 커서에 대한 조건으로 정상 조회된다.
        """)
        void cursorNotNullAfterNull_Success(){
            // given
            int totalCount = 36;
            int pageSize = 20;

            User user = User.builder()
                    .email("test@aa.com")
                    .password("1234")
                    .build();
            ClothesTestUtils.createClothesList(user, entityManager,totalCount);

            // [Page 1]
            // given
            ClothesSearchCondition query1 = new ClothesSearchCondition(
                    null,
                    null,
                    pageSize,
                    null,
                    user.getId()
            );

            // when
            Slice<Clothes> page1 = clothesRepositoryCustom.findMyClothesList(query1);

            // [Page 2]
            // given
            ClothesSearchCondition query2 = new ClothesSearchCondition(
                    page1.getContent().get(page1.getContent().size() - 1).getCreatedAt(),
                    null,
                    pageSize,
                    null,
                    user.getId()
            );

            // when
            Slice<Clothes> page2 = clothesRepositoryCustom.findMyClothesList(query2);

            // then
            // 각 페이지는 생성일 내림차순으로 정렬이 잘 되어있다.
            assertThat(page1.getContent()).isSortedAccordingTo(
                    Comparator.comparing(Clothes::getCreatedAt, Comparator.reverseOrder())
            );
            assertThat(page2.getContent()).isSortedAccordingTo(
                    Comparator.comparing(Clothes::getCreatedAt, Comparator.reverseOrder())
            );

            // 페이지들 간의 첫 요소 비교 (내림차순)
            assertThat(page1.getContent().get(0).getCreatedAt())
                    .isAfterOrEqualTo(page2.getContent().get(0).getCreatedAt());

            // 각 페이지가 있는지 여부 검증
            assertThat(page1.hasNext()).isTrue();
            assertThat(page2.hasNext()).isFalse();

            // 각 페이지 요소 수 검증
            assertThat(page1.getContent()).hasSize(pageSize);
            assertThat(page2.getContent()).hasSize(totalCount % pageSize);
        }
    }
}
