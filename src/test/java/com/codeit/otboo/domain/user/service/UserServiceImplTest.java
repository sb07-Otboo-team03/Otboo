package com.codeit.otboo.domain.user.service;

import com.codeit.otboo.domain.binarycontent.entity.BinaryContent;
import com.codeit.otboo.domain.binarycontent.fixture.BinaryContentFixture;
import com.codeit.otboo.domain.binarycontent.resolver.BinaryContentUrlResolver;
import com.codeit.otboo.domain.profile.ProfileFixture;
import com.codeit.otboo.domain.profile.dto.response.ProfileResponse;
import com.codeit.otboo.domain.profile.entity.Profile;
import com.codeit.otboo.domain.profile.fixture.ProfileResponseFixture;
import com.codeit.otboo.domain.user.dto.request.UpdatePasswordRequest;
import com.codeit.otboo.domain.user.dto.request.UserCreateRequest;
import com.codeit.otboo.domain.user.dto.request.UserSearchRequest;
import com.codeit.otboo.domain.user.dto.response.UserResponse;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.user.exception.UserException;
import com.codeit.otboo.domain.user.exception.UserNotFoundException;
import com.codeit.otboo.domain.user.fixture.UserCreateRequestFixture;
import com.codeit.otboo.domain.user.fixture.UserFixture;
import com.codeit.otboo.domain.user.fixture.UserResponseFixture;
import com.codeit.otboo.domain.user.mapper.ProfileMapper;
import com.codeit.otboo.domain.user.mapper.UserMapper;
import com.codeit.otboo.domain.user.repository.TemporaryPasswordRepository;
import com.codeit.otboo.domain.user.repository.UserRepository;
import com.codeit.otboo.global.slice.dto.CursorResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ProfileMapper profileMapper;

    @Mock
    private BinaryContentUrlResolver binaryContentUrlResolver;

    @Mock
    private TemporaryPasswordRepository temporaryPasswordRepository;

    @InjectMocks
    private UserServiceImpl userService;


    @Nested
    @DisplayName("유저 생성")
    class UserCreate {

        @DisplayName("정상 회원가입")
        @Test
        void createUser_success() {
            // given
            UserCreateRequest userCreateRequest = UserCreateRequestFixture.create();
            UUID userId = UUID.randomUUID();
            String encodedPassword = "encodedPassword";

            User savedUser = UserFixture.create(userId, userCreateRequest.email(), encodedPassword);
            UserResponse userResponse = UserResponseFixture.create(savedUser);

            given(userRepository.existsByEmail(userCreateRequest.email())).willReturn(false);
            given(passwordEncoder.encode(any())).willReturn(encodedPassword);
            given(userRepository.save(any(User.class))).willReturn(savedUser);
            given(userMapper.toDto(any(User.class))).willReturn(userResponse);

            // when
            UserResponse result = userService.createUser(userCreateRequest);

            // then
            assertThat(result).isEqualTo(userResponse);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            then(userRepository).should().existsByEmail(userCreateRequest.email());
            then(passwordEncoder).should().encode(userCreateRequest.password());
            then(userRepository).should().save(userCaptor.capture());
            then(userMapper).should().toDto(savedUser);

            User capturedUser = userCaptor.getValue();
            assertThat(capturedUser.getEmail()).isEqualTo(userCreateRequest.email());
            assertThat(capturedUser.getPassword()).isEqualTo(encodedPassword);
            assertThat(capturedUser.getProfile()).isNotNull();
            assertThat(capturedUser.getProfile().getName()).isEqualTo(userCreateRequest.name());
        }

        @DisplayName("회원 가입 실패 - 중복된 이메일")
        @Test
        void createUser_fail() {
            // given
            UserCreateRequest userCreateRequest = UserCreateRequestFixture.create();
            given(userRepository.existsByEmail(userCreateRequest.email())).willReturn(true);

            // when
            assertThatThrownBy(() -> userService.createUser(userCreateRequest))
                    .isInstanceOf(UserException.class);

            // then
            then(userRepository).should().existsByEmail(userCreateRequest.email());
            then(passwordEncoder).should(never()).encode(any());
            then(userRepository).should(never()).save(any());
        }

    }

    @Nested
    @DisplayName("프로필 조회")
    class GetProfile {
        @Test
        @DisplayName("프로필 조회 성공")
        void getProfile_success() {
            // given
            UUID userId = UUID.randomUUID();
            UUID binaryContentId = UUID.randomUUID();

            User user = UserFixture.create(userId, "test@codeit.com", "password123!");
            Profile profile = ProfileFixture.create(user);
            BinaryContent binaryContent = BinaryContentFixture.create(binaryContentId);

            user.setProfile(profile);
            profile.update(null, null, null, null, null, binaryContent);

            ProfileResponse profileResponse = ProfileResponseFixture.create(user, "test-image-url");

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(binaryContentUrlResolver.resolve(binaryContentId)).willReturn("test-image-url");
            given(profileMapper.toDto(user, "test-image-url")).willReturn(profileResponse);

            // when
            ProfileResponse result = userService.getProfile(userId);

            // then
            assertThat(result).isEqualTo(profileResponse);
            then(userRepository).should().findById(userId);
            then(binaryContentUrlResolver).should().resolve(binaryContentId);
            then(profileMapper).should().toDto(user, "test-image-url");
        }

        @Test
        @DisplayName("프로필 조회 성공 - 이미지 없는 경우")
        void getProfile_success_nonProfile() {
            // given
            UUID userId = UUID.randomUUID();
            User user = UserFixture.create(userId, "test@codeit.com", "password123!");
            Profile profile = ProfileFixture.create(user);
            user.setProfile(profile);
            profile.update(null, null, null, null, null, null);
            ProfileResponse profileResponse = ProfileResponseFixture.create(user, null);

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(profileMapper.toDto(user, null)).willReturn(profileResponse);

            // when
            ProfileResponse result = userService.getProfile(userId);

            // then
            assertThat(result).isEqualTo(profileResponse);
            then(binaryContentUrlResolver).should(never()).resolve(any());
            then(profileMapper).should().toDto(user, null);
        }


        @Test
        @DisplayName("프로필 조회 실패 - 존재하지 않는 유저 ")
        void getProfile_fail_userNotFound() {
            // given
            UUID userId = UUID.randomUUID();

            given(userRepository.findById(userId)).willReturn(Optional.empty());

            // when
            assertThatThrownBy(() -> userService.getProfile(userId))
                    .isInstanceOf(UserNotFoundException.class);

            // then
            then(binaryContentUrlResolver).shouldHaveNoInteractions();
            then(profileMapper).shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("유저 생성")
    class UserPasswordUpdate {
        @Test
        @DisplayName("유저 비밀번호 변경 성공")
        void passwordUpdate_success() {
            // given
            String password = "new_password";
            String encodedPassword = "encoded_password";
            UpdatePasswordRequest updatePasswordRequest = new UpdatePasswordRequest(password);
            User user = UserFixture.create("testEmail", password);
            UUID userId = user.getId();
            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(passwordEncoder.encode(password)).willReturn(encodedPassword);
            willDoNothing().given(temporaryPasswordRepository).deleteByUserId(userId);

            // when
            userService.updateUserPassword(userId, updatePasswordRequest);
            
            // then
            then(userRepository).should().findById(userId);
            then(passwordEncoder).should().encode(password);
            then(temporaryPasswordRepository).should().deleteByUserId(userId);

            assertThat(user.getPassword()).isEqualTo(encodedPassword);
            
        }

        @Test
        @DisplayName("유저 비밀번호 변경 실패 - 존재하는 유저 없음.")
        void passwordUpdate_fail_userNotFound() {
            // given
            String password = "new_password";
            UpdatePasswordRequest updatePasswordRequest = new UpdatePasswordRequest(password);
            UUID userId = UUID.randomUUID();

            given(userRepository.findById(userId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.updateUserPassword(userId, updatePasswordRequest))
                    .isInstanceOf(UserNotFoundException.class);

            // then
            then(userRepository).should().findById(userId);
            then(passwordEncoder).should(never()).encode(password);
            then(temporaryPasswordRepository).should(never()).deleteByUserId(userId);
        }
    }

    @Nested
    @DisplayName("유저 조회")
    class UserSearch {
        @ParameterizedTest
        @CsvSource({
                "createdAt",
                "email"
        })
        @DisplayName("""
                마지막 기사로부터 다음 페이지의 커서를 생성한다.
                sortBy = createdAt, email
                pageSize = 5
                sortDirection = "DESCENDING" (Default)
                emailLike, roleEqual, locked = null (전체검색)
                """)
        void convertUserCursorByOrderBy(String sortBy) {
            // given
            UserSearchRequest userSearchRequest = new UserSearchRequest(null, null, 5, sortBy, null, null, null, null);
            List<User> userList = UserFixture.createUserCursor(6);

            Slice<User> slice = new SliceImpl<>(userList, PageRequest.of(0, 5), true);

            given(userRepository.findAllByKeywordLike(any())).willReturn(slice);
            given(userRepository.countTotalElements(any())).willReturn(6L);
            given(userMapper.toDto(any(User.class))).willReturn(null);

            // when
            CursorResponse<UserResponse> result = userService.getAllUsers(userSearchRequest);

            // then
            assertThat(result.hasNext()).isTrue();
            User lastFeed = userList.get(5);
            assertThat(result.nextIdAfter()).isEqualTo(userList.get(5).getId());
            if("createdAt".equals(sortBy))
                assertThat(result.nextCursor()).isEqualTo(String.valueOf(lastFeed.getCreatedAt()));
            else
                assertThat(result.nextCursor()).isEqualTo(String.valueOf(lastFeed.getEmail()));
        }

        @ParameterizedTest
        @CsvSource({
                "createdAt",
                "email"
        })
        @DisplayName("""
                마지막 페이지를 조회하면, nextCursor, nextAfter는 null을 반환
                sortBy = createdAt, email
                pageSize = 5
                sortDirection = "DESCENDING" (Default)
                emailLike, roleEqual, locked = null (전체검색)
                """)
        void convertUserCursor_NextNonPage(String sortBy) {
            // given
            UserSearchRequest userSearchRequest = new UserSearchRequest(null, null, 5, sortBy, null, null, null, null);
            List<User> userCursor = UserFixture.createUserCursor(3);

            Slice<User> slice = new SliceImpl<>(userCursor, PageRequest.of(0, 5), false);

            given(userRepository.findAllByKeywordLike(any())).willReturn(slice);
            given(userRepository.countTotalElements(any())).willReturn(3L);
            given(userMapper.toDto(any(User.class))).willReturn(null);

            // when
            CursorResponse<UserResponse> result = userService.getAllUsers(userSearchRequest);

            // then
            assertThat(result.hasNext()).isFalse();
            assertThat(result.nextIdAfter()).isNull();
        }

        @Test
        @DisplayName("검색 결과가 없으면 DB를 조회하지 않고 반환한다.")
        void searchEmptyResult_nextNonPage() {
            // given
            UserSearchRequest userSearchRequest = new UserSearchRequest(null, null, 5, "createdAt", null, null, null, null);
            Slice<User> emptySlice = new SliceImpl<>(List.of(), PageRequest.of(0, 5), false);
            given(userRepository.findAllByKeywordLike(any())).willReturn(emptySlice);

            // when
            CursorResponse<UserResponse> result = userService.getAllUsers(userSearchRequest);

            // then
            assertThat(result.hasNext()).isFalse();
            assertThat(result.nextIdAfter()).isNull();
            assertThat(result.data()).isEmpty();
            assertThat(result.nextCursor()).isNull();


        }
    }
}