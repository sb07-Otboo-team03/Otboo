package com.codeit.otboo.domain.feed.service;

import com.codeit.otboo.domain.clothes.management.entity.Clothes;
import com.codeit.otboo.domain.clothes.management.entity.ClothesType;
import com.codeit.otboo.domain.clothes.management.repository.ClothesRepository;
import com.codeit.otboo.domain.comment.repository.CommentRepository;
import com.codeit.otboo.domain.feed.dto.mapper.FeedMapper;
import com.codeit.otboo.domain.feed.dto.request.FeedCreateRequest;
import com.codeit.otboo.domain.feed.dto.request.FeedSearchRequest;
import com.codeit.otboo.domain.feed.dto.request.FeedUpdateRequest;
import com.codeit.otboo.domain.feed.dto.response.FeedResponse;
import com.codeit.otboo.domain.feed.entity.Feed;
import com.codeit.otboo.domain.feed.entity.FeedWeather;
import com.codeit.otboo.domain.feed.exception.FeedNotFoundException;
import com.codeit.otboo.domain.feed.fixture.FeedFixture;
import com.codeit.otboo.domain.feed.repository.FeedRepository;
import com.codeit.otboo.domain.follow.repository.FollowRepository;
import com.codeit.otboo.domain.like.repository.LikeRepository;
import com.codeit.otboo.domain.profile.entity.Profile;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.user.exception.UserNotFoundException;
import com.codeit.otboo.domain.user.repository.UserRepository;
import com.codeit.otboo.domain.weather.entity.Weather;
import com.codeit.otboo.domain.weather.repository.WeatherRepository;
import com.codeit.otboo.global.exception.ErrorCode;
import com.codeit.otboo.global.slice.dto.CursorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeedServiceImplTest {

    @Mock
    private FeedRepository feedRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private WeatherRepository weatherRepository;
    @Mock
    private ClothesRepository clothesRepository;
    @Mock
    private LikeRepository likeRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private FollowRepository followRepository;
    @Mock
    private FeedMapper feedMapper;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private FeedServiceImpl feedService;

    private User user;
    
    @BeforeEach
    void setup() {
        UUID authorId = UUID.randomUUID();
        user = new User("otboo@a.a", "otboo123");
        ReflectionTestUtils.setField(user, "id", authorId);
        new Profile(user, "user");
    }

    @Nested
    @DisplayName("피드 생성")
    class FeedCreate {

        @ParameterizedTest
        @CsvSource(value = {
            "null", "6de3b7bc-b14c-4608-a343-111111111111"
        }, nullValues = "null")
        @DisplayName("피드를 생성할 수 있다.")
        void createFeed_Success(UUID id) {
            // given
            UUID userId = user.getId();
            UUID weatherId = UUID.randomUUID();
            UUID clothesId = UUID.randomUUID();
            String content = "Feed 생성 테스트";

            FeedCreateRequest request = new FeedCreateRequest(userId, weatherId, List.of(clothesId), content);
            FeedResponse dto = FeedResponse.builder().content(request.content()).build();
            
            Weather weather = new Weather(null, null, null, null, null, null, null, null, null, null, null, null, null, null);
            Clothes clothes = new Clothes("상의", ClothesType.TOP, user, null);

            Set<UUID> receiverId = id == null ? Set.of() : Set.of(id);

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(weatherRepository.findById(weatherId)).willReturn(Optional.of(weather));
            given(clothesRepository.findAllById(List.of(clothesId))).willReturn(List.of(clothes));
            given(feedMapper.toDto(any(Feed.class))).willReturn(dto);
            given(followRepository.findAllFollowerIdsByFolloweeId(userId)).willReturn(receiverId);

            // when
            FeedResponse response = feedService.createFeed(request);

            // then
            verify(feedRepository, times(1)).save(any(Feed.class));
            assertThat(response).isNotNull();
            assertThat(response.content()).isEqualTo(content);
        }

        @Test
        @DisplayName("존재하지 않는 유저Id면 예외를 반환한다.")
        void createFeed_Fail_NotFoundUser() {
            // given
            UUID userId = user.getId();

            given(userRepository.findById(userId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> feedService.createFeed(
                    new FeedCreateRequest(userId, null, null, null)))
                    .isInstanceOf(UserNotFoundException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.USER_NOT_FOUND);
        }

        @Test
        @DisplayName("존재하지 않는 날씨Id면 예외를 반환한다.")
        void createFeed_Fail_NotFoundWeather() {
            // given
            UUID userId = user.getId();
            UUID weatherId = UUID.randomUUID();

            given(userRepository.findById(userId)).willReturn(Optional.of(new User("otboo@a.a", "otboo123")));
            given(weatherRepository.findById(weatherId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> feedService.createFeed(
                    new FeedCreateRequest(userId, weatherId, null, null)))
                    .isInstanceOf(IllegalArgumentException.class); // TODO
        }
    }

    @Nested
    @DisplayName("피드 조회")
    class FeedSearch {

        @ParameterizedTest
        @CsvSource({
                "createdAt",
                "likeCount"
        })
        @DisplayName("""
                마지막 기사로부터 다음 페이지의 커서를 생성한다.
                sortBy = createdAt, likeCount
                pageSize = 5
                sortDirection = "DESCENDING" (Default)
                keywordLike, skyStatus, PrecipitationTypeEqual = null (전체검색)
                """)
        void convertFeedCursorByOrderBy(String sortBy) {
            // given
            UUID userId = user.getId();
            FeedSearchRequest request = new FeedSearchRequest(null, null, 5, sortBy, null, null, null, null);
            List<Feed> feedList = FeedFixture.createFeedCursor(6);
            if ("likeCount".equals(sortBy)) Collections.reverse(feedList);

            Slice<Feed> slice = new SliceImpl<>(feedList, PageRequest.of(0, 5), true);

            given(userRepository.existsById(userId)).willReturn(true);
            given(feedRepository.findAllByKeywordLike(any())).willReturn(slice);
            given(feedRepository.countTotalElements(any())).willReturn(6L);
            given(likeRepository.findFeedIdsByUserIdAndFeedIdIn(any(), any())).willReturn(Set.of());

            given(feedMapper.toDto(any(Feed.class), anyBoolean())).willReturn(null);
            // when
            CursorResponse<FeedResponse> response = feedService.getAllFeed(request, userId);

            // then
            assertThat(response.hasNext()).isTrue();
            Feed lastFeed = feedList.get(5);
            assertThat(response.nextIdAfter()).isEqualTo(feedList.get(5).getId());
            if ("createdAt".equals(sortBy))
                assertThat(response.nextCursor()).isEqualTo(String.valueOf(lastFeed.getCreatedAt()));
            else
                assertThat(response.nextCursor()).isEqualTo(String.valueOf(lastFeed.getLikeCount()));
        }

        @Test
        @DisplayName("마지막 페이지 조회 후, nextCursor와 nextAfter가 null을 반환한다.")
        void searchLastPage_ReturnNoNextPage() {
            // given
            UUID userId = user.getId();
            FeedSearchRequest request = new FeedSearchRequest(null, null, 5, null, null, null, null, null);
            List<Feed> feedList = FeedFixture.createFeedCursor(5);

            Slice<Feed> slice = new SliceImpl<>(feedList, PageRequest.of(0, 5), false);

            given(userRepository.existsById(userId)).willReturn(true);
            given(feedRepository.findAllByKeywordLike(any())).willReturn(slice);
            given(feedRepository.countTotalElements(any())).willReturn(5L);
            given(likeRepository.findFeedIdsByUserIdAndFeedIdIn(any(), any())).willReturn(Set.of());

            given(feedMapper.toDto(any(Feed.class), anyBoolean())).willReturn(null);
            // when
            CursorResponse<FeedResponse> response = feedService.getAllFeed(request, userId);

            // then
            assertThat(response.hasNext()).isFalse();
            assertThat(response.nextIdAfter()).isNull();
        }

        @Test
        @DisplayName("검색 결과가 없으면 DB조회를 하지 않고 반환한다.")
        void searchEmptyResult_ReturnNullCursorAndAfter() {
            UUID userId = user.getId();
            String keyword = "hello world";
            FeedSearchRequest request = new FeedSearchRequest(null, null, 5, null, null, keyword, null, null);
            Slice<Feed> emptySlice = new SliceImpl<>(List.of(), PageRequest.of(0, 5), false);

            given(userRepository.existsById(userId)).willReturn(true);
            given(feedRepository.findAllByKeywordLike(any())).willReturn(emptySlice);

            // when
            CursorResponse<FeedResponse> response = feedService.getAllFeed(request, userId);

            // then
            assertThat(response.hasNext()).isFalse();
            assertThat(response.nextCursor()).isNull();
            assertThat(response.nextIdAfter()).isNull();
            assertThat(response.data()).isEmpty();

            verify(feedRepository, never()).countTotalElements(any());
            verify(likeRepository, never()).findByFeedIdAndUserId(any(), any());
        }

        @Test
        @DisplayName("피드를 조회하는 유저의 id가 존재하지 않으면 예외를 반환한다.")
        void searchFeedList_Fail_NotFoundUser() {
            // given
            UUID userId = user.getId();

            given(userRepository.existsById(userId)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> feedService.getAllFeed(null, userId))
                    .isInstanceOf(UserNotFoundException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.USER_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("피드 수정")
    
    class FeedFind {
        @Test
        @DisplayName("피드 내용을 수정할 수 있다.")
        void searchFeedList_Success() {
            // given
            UUID userId = user.getId();
            UUID feedId = UUID.randomUUID();
            String newContent = "New Feed Content";

            FeedWeather weatherInformation = FeedWeather.builder().build();
            Feed feed = Feed.builder().content("Old Feed Content").weather(weatherInformation)
                    .author(user).build();
            FeedResponse dto = FeedResponse.builder().id(feedId).content(newContent).build();

            given(feedRepository.findById(feedId)).willReturn(Optional.of(feed));
            given(feedMapper.toDto(any(Feed.class))).willReturn(dto);

            FeedUpdateRequest request = new FeedUpdateRequest(newContent);

            // when
            FeedResponse response = feedService.updateFeed(feedId, request, userId);

            // then
            verify(feedMapper, times(1)).toDto(any(Feed.class));
            assertThat(response.content()).isEqualTo("New Feed Content");
        }

        @Test
        @DisplayName("존재하지 않는 피드Id면 예외를 반환한다.")
        void searchFeedList_Fail_NotFoundFeed() {
            // given
            UUID feedId = UUID.randomUUID();
            String newContent = "New Feed Content";

            given(feedRepository.findById(feedId)).willReturn(Optional.empty());

            FeedUpdateRequest request = new FeedUpdateRequest(newContent);

            // when & then
            assertThatThrownBy(() -> feedService.updateFeed(
                    feedId, new FeedUpdateRequest(newContent), user.getId()))
                    .isInstanceOf(FeedNotFoundException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.FEED_NOT_FOUND);
        }
        
        @Test
        @DisplayName("자신의 피드만 수정 가능하다")
        void updateFeed_Fail_NotAuthor() {
            // given
            UUID userId = UUID.randomUUID();
            UUID feedId = UUID.randomUUID();
            String newContent = "New Feed Content";

            FeedWeather weatherInformation = FeedWeather.builder().build();
            Feed feed = Feed.builder().content("Old Feed Content").weather(weatherInformation)
                    .author(user).build();

            FeedUpdateRequest request = new FeedUpdateRequest(newContent);

            given(feedRepository.findById(feedId)).willReturn(Optional.of(feed));

            // when & then
            assertThatThrownBy(() -> feedService.updateFeed(feedId, request, userId))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("피드 삭제")
    class FeedDelete {

        @Test
        @DisplayName("피드를 삭제할 수 있다.")
        void deleteFeed_Success() {
            // given
            UUID feedId = UUID.randomUUID();
            Feed feed = Feed.builder().author(user).build();

            given(feedRepository.findById(feedId)).willReturn(Optional.of(feed));

            // when
            feedService.deleteFeed(feedId, user.getId());

            // then
            verify(feedRepository, times(1)).delete(feed);
            verify(likeRepository, times(1)).deleteAllByFeedId(feedId);
            verify(commentRepository, times(1)).deleteAllByFeedId(feedId);
        }

        @Test
        @DisplayName("존재하지 않는 피드Id라면 예외를 반환한다.")
        void deleteFeed_Fail_NotFoundFeed() {
            // given
            UUID userId = UUID.randomUUID();
            UUID feedId = UUID.randomUUID();

            given(feedRepository.findById(feedId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> feedService.deleteFeed(feedId, userId))
                    .isInstanceOf(FeedNotFoundException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.FEED_NOT_FOUND);
        }

        @Test
        @DisplayName("자신의 피드만 삭제 가능하다.")
        void deleteFeed_Fail_NotAuthor() {
            // given
            UUID userId = UUID.randomUUID();

            UUID feedId = UUID.randomUUID();
            Feed feed = Feed.builder().author(user).build();

            given(feedRepository.findById(feedId)).willReturn(Optional.of(feed));

            // when & then
            assertThatThrownBy(() -> feedService.deleteFeed(feedId, userId))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

}