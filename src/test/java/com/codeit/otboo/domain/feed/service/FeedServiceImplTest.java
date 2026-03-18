package com.codeit.otboo.domain.feed.service;

import com.codeit.otboo.domain.clothes.management.entity.Clothes;
import com.codeit.otboo.domain.clothes.management.entity.ClothesType;
import com.codeit.otboo.domain.clothes.management.repository.ClothesRepository;
import com.codeit.otboo.domain.feed.dto.mapper.FeedMapper;
import com.codeit.otboo.domain.feed.dto.request.FeedCreateRequest;
import com.codeit.otboo.domain.feed.dto.request.FeedUpdateRequest;
import com.codeit.otboo.domain.feed.dto.response.FeedResponse;
import com.codeit.otboo.domain.feed.entity.Feed;
import com.codeit.otboo.domain.feed.entity.WeatherInformation;
import com.codeit.otboo.domain.feed.repository.FeedRepository;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.user.repository.UserRepository;
import com.codeit.otboo.domain.weather.entity.Weather;
import com.codeit.otboo.domain.weather.repository.WeatherRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
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
    private FeedMapper feedMapper;

    @InjectMocks
    private FeedServiceImpl feedService;

    @BeforeEach
    void setUp() {
    }

    @Nested
    @DisplayName("피드 생성")
    class FeedCreate {

        @Test
        @DisplayName("피드를 생성할 수 있다.")
        void createFeed () {
            // given
            UUID userId = UUID.randomUUID();
            UUID weatherId = UUID.randomUUID();
            UUID clothesId = UUID.randomUUID();
            String content = "Feed 생성 테스트";

            FeedCreateRequest request = new FeedCreateRequest(userId, weatherId, List.of(clothesId), content);

            User user = new User("otboo@a.a", "otboo123");
            Weather weather = new Weather(null);
            Clothes clothes = new Clothes("상의", ClothesType.TOP, user, null);

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(weatherRepository.findById(weatherId)).willReturn(Optional.of(weather));
            given(clothesRepository.findAllById(List.of(clothesId))).willReturn(List.of(clothes));

            // when
            FeedResponse response = feedService.createFeed(request);

            // then
            verify(feedRepository, times(1)).save(any(Feed.class));
            assertThat(response).isNotNull();
            assertThat(response.content()).isEqualTo(content);
        }
    }

    @Nested
    @DisplayName("피드 수정")
    class FeedFind {
        @Test
        @DisplayName("피드 내용을 수정할 수 있다.")
        void searchFeedList() {
            // given
            UUID feedId = UUID.randomUUID();
            WeatherInformation weatherInformation = WeatherInformation.builder().build();
            Feed feed = Feed.builder().content("Old Feed Content").weather(weatherInformation).build();

            given(feedRepository.findById(any(UUID.class))).willReturn(Optional.of(feed));

            FeedUpdateRequest request = new FeedUpdateRequest("New Feed Content");
            // when
            FeedResponse response = feedService.updateFeed(feedId, request);
            
            // then
            assertThat(response.content()).isEqualTo("New Feed Content");
            
        }
    }

}