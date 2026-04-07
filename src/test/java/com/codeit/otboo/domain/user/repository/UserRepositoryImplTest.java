package com.codeit.otboo.domain.user.repository;

import com.codeit.otboo.domain.profile.entity.Profile;
import com.codeit.otboo.domain.user.dto.request.UserSearchCondition;
import com.codeit.otboo.domain.user.entity.Role;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.global.config.TestJpaAuditing;
import com.codeit.otboo.global.config.TestQueryDslConfig;
import com.codeit.otboo.global.slice.dto.SortDirection;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Slice;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({TestQueryDslConfig.class, TestJpaAuditing.class})
@ActiveProfiles("test")
class UserRepositoryImplTest {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TestEntityManager entityManager;

    private void saveUser(int n) {
        List<User> userList = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            String keyword = "A"+i;
            if(i % 2 == 1)
                 keyword = "B"+i;

            String email = "test" + keyword + "@test.com";
            User user =  User
                    .builder()
                    .email(email)
                    .password("password")
                    .build();

            Profile profile = Profile.builder()
                    .name("test" + i)
                    .user(user)
                    .build();

            user.setProfile(profile);
            userList.add(user);
        }
        userList.forEach(entityManager::persist);
        entityManager.flush();

        LocalDateTime baseTime = LocalDateTime.of(2026, 4, 7, 14, 30, 0);
        EntityManager em = entityManager.getEntityManager();

        for (int i = 0; i < n; i++) {
            Role role = Role.USER;
            if(i % 2 == 1) role = Role.ADMIN;
            em.createQuery("update User u set u.createdAt = :createdAt, u.role = :role where u.id = :id")
                    .setParameter("createdAt", baseTime.minusDays(i))
                    .setParameter("role", role) // 쿼리문에 :role이 추가되어야 함
                    .setParameter("id", userList.get(i).getId())
                    .executeUpdate();
        }

        entityManager.clear();
    }

    private String createCursor(User user, String sortBy) {
        if ("createdAt".equals(sortBy)) return String.valueOf(user.getCreatedAt());
        else return String.valueOf(user.getEmail());
    }

    @Nested
    @DisplayName("주 정렬 오름차순/내림차순 테스트")
    class MainSortTest {

        @ParameterizedTest
        @CsvSource({
                "createdAt, DESCENDING",
                "createdAt, ASCENDING",
                "email, DESCENDING",
                "email, ASCENDING"
        })
        @DisplayName("""
                주정렬: 생성일, Email
                보조정렬: ID
                순서: 내림차순, 오름차순
                이메일: 전체,
                권한(USER, ADMIN): 전체
                활성화 여부: 전체
                """)
        void searchUserBySort(String sortBy, SortDirection sortDirection) {
            // given
            saveUser(8);

            UserSearchCondition condition = UserSearchCondition.builder()
                    .limit(5)
                    .sortBy(sortBy)
                    .sortDirection(sortDirection)
                    .build();

            // firstPage
            Slice<User> firstPage = userRepository.findAllByKeywordLike(condition);
            List<User> firstPageList = firstPage.getContent();
            User lastUser = firstPageList.get(firstPageList.size() - 1);
            String lastCursor = createCursor(lastUser, sortBy);
            UUID lastId = lastUser.getId();

            assertThat(firstPage.hasNext()).isTrue();
            assertThat(firstPageList.size()).isEqualTo(5);

            // secondPage
            UserSearchCondition condition2
                    = UserSearchCondition.builder()
                    .cursor(lastCursor)
                    .idAfter(lastId)
                    .limit(5)
                    .sortBy(sortBy)
                    .sortDirection(sortDirection)
                    .build();
            Slice<User> secondPage = userRepository.findAllByKeywordLike(condition2);
            List<User> secondPageList = secondPage.getContent();

            assertThat(secondPage.hasNext()).isFalse();
            System.out.println("FirsPage: " + firstPageList.size() + " SecondPage: " + secondPageList.size() + "");
            assertThat(secondPageList.size()).isEqualTo(3);

            if ("createdAt".equals(sortBy)) {
                if (SortDirection.DESCENDING.equals(sortDirection))
                    assertThat(secondPageList.get(0).getCreatedAt()).isBefore(lastUser.getCreatedAt());
                else
                    assertThat(secondPageList.get(0).getCreatedAt()).isAfter(lastUser.getCreatedAt());
            } else {
                if (SortDirection.DESCENDING.equals(sortDirection))
                    assertThat(secondPageList.get(0).getEmail()).isLessThan(lastUser.getEmail());
                else
                    assertThat(secondPageList.get(0).getEmail()).isGreaterThan(lastUser.getEmail());
            }
        }

        @Test
        @DisplayName("""
                cursor 혹은 idAfter가 null이라면 전체 조회
                """)
        void searchUserBySort_NullCursorOrIdAfter() {
            // given
            saveUser(8);
            UserSearchCondition condition1 = UserSearchCondition.builder()
                    .limit(5)
                    .cursor(LocalDateTime.now().toString())
                    .sortBy("createdAt")
                    .sortDirection(SortDirection.DESCENDING)
                    .build();

            Slice<User> page1 = userRepository.findAllByKeywordLike(condition1);
            List<User> firstPageList = page1.getContent();
            assertThat(firstPageList.size()).isEqualTo(5);

            User lastUser = firstPageList.get(firstPageList.size() - 1);
            String lastCursor = createCursor(lastUser, "createdAt");
            UUID lastId = lastUser.getId();

            UserSearchCondition condition2 = UserSearchCondition.builder()
                    .limit(5)
                    .idAfter(lastId)
                    .cursor(lastCursor)
                    .sortBy("createdAt")
                    .sortDirection(SortDirection.DESCENDING)
                    .build();

            Slice<User> page2 = userRepository.findAllByKeywordLike(condition2);
            List<User> lastPageList = page2.getContent();
            assertThat(lastPageList.size()).isEqualTo(3);
        }

    }

    @Nested
    @DisplayName("검색어로 조회")
    class SearchKeywordTest {

        @ParameterizedTest
        @CsvSource(value = {
                "A", "null", "''"
        }, nullValues = "null")
        @DisplayName("""
                주정렬: 생성일
                보조정렬: id
                순서: 내림차순
                이메일 검색어: A, (null 혹은 공백)
                권한(USER, ADMIN): 전체
                활성화 여부: 전체
                """)
        void searchUserByEmail(String keyword) {
            // given
            saveUser(5);
            UserSearchCondition condition
                    = UserSearchCondition.builder()
                    .limit(5)
                    .sortBy("createdAt")
                    .sortDirection(SortDirection.DESCENDING)
                    .emailLike(keyword)
                    .build();

            Slice<User> page = userRepository.findAllByKeywordLike(condition);
            List<User> list = page.getContent();

            // then
            if (keyword == null || keyword.isBlank())
                assertThat(list.size()).isEqualTo(5);
            else {
                assertThat(list.size()).isEqualTo(3);
                assertThat(list.get(0).getEmail()).contains("A");
            }
        }
    }

    @Nested
    @DisplayName("필터링")
    class PermissionTest {
        
        @Test
        @DisplayName("""
                주정렬: 생성일
                보조정렬: id
                순서: 내림차순
                이메일 검색어: A, (null 혹은 공백)
                권한(USER, ADMIN): USER
                활성화 여부: 전체
                """)
        void SearchUserByRole() {
            // given
            saveUser(10);
            UserSearchCondition condition
                    = UserSearchCondition.builder()
                    .limit(5)
                    .sortBy("createdAt")
                    .sortDirection(SortDirection.DESCENDING)
                    .roleEqual(Role.USER)
                    .build();
            // when
            Slice<User> page = userRepository.findAllByKeywordLike(condition);

            // then
            List<User> list = page.getContent();
            assertThat(list.size()).isEqualTo(5);
            list.forEach(user -> assertThat(user.getRole()).isEqualTo(Role.USER));
        }
    }

    @Nested
    @DisplayName("총 갯수")
    class ToTalCountTest {
        @Test
        @DisplayName("피드의 총 갯수")
        void countTotal() {
            // given
            saveUser(10);
            UserSearchCondition condition = UserSearchCondition.builder()
                    .limit(5)
                    .sortBy("createdAt")
                    .sortDirection(SortDirection.DESCENDING)
                    .build();
            // when
            long total = userRepository.countTotalElements(condition);

            // then
            assertThat(total).isEqualTo(10);
        }
    }

}