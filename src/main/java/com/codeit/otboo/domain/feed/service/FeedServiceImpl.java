package com.codeit.otboo.domain.feed.service;

import com.codeit.otboo.domain.clothes.management.entity.Clothes;
import com.codeit.otboo.domain.clothes.management.repository.ClothesRepository;
import com.codeit.otboo.domain.comment.repository.CommentRepository;
import com.codeit.otboo.domain.feed.dto.mapper.FeedMapper;
import com.codeit.otboo.domain.feed.dto.request.FeedCreateRequest;
import com.codeit.otboo.domain.feed.dto.request.FeedSearchCondition;
import com.codeit.otboo.domain.feed.dto.request.FeedSearchRequest;
import com.codeit.otboo.domain.feed.dto.request.FeedUpdateRequest;
import com.codeit.otboo.domain.feed.dto.response.FeedResponse;
import com.codeit.otboo.domain.feed.entity.Feed;
import com.codeit.otboo.domain.feed.entity.FeedWeather;
import com.codeit.otboo.domain.feed.exception.FeedNotFoundException;
import com.codeit.otboo.domain.feed.repository.FeedRepository;
import com.codeit.otboo.domain.like.repository.LikeRepository;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.user.exception.UserNotFoundException;
import com.codeit.otboo.domain.user.repository.UserRepository;
import com.codeit.otboo.domain.weather.entity.Weather;
import com.codeit.otboo.domain.weather.repository.WeatherRepository;
import com.codeit.otboo.global.exception.ErrorCode;
import com.codeit.otboo.global.exception.OtbooException;
import com.codeit.otboo.global.slice.dto.CursorResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class FeedServiceImpl implements FeedService{

    private final FeedRepository feedRepository;
    private final UserRepository userRepository;
    private final WeatherRepository weatherRepository;
    private final ClothesRepository clothesRepository;
    private final LikeRepository likeRepository;
    private final FeedMapper feedMapper;
    private final CommentRepository commentRepository;

    @Override
    @Transactional
    @PreAuthorize("#request.authorId() == authentication.principal.id")
    public FeedResponse createFeed(FeedCreateRequest request) {

        User author = userRepository.findById(request.authorId())
                .orElseThrow(() -> new UserNotFoundException(request.authorId()));

        FeedWeather weather = getWeatherInformation(request.weatherId());

        List<Clothes> clothesList = clothesRepository.findAllById(request.clothesIds());

        Feed feed = new Feed(request.content(), author, weather, clothesList);
        feedRepository.save(feed);
        log.debug("Feed 생성 완료 - feedId={}", feed.getId());

        return feedMapper.toDto(feed);
    }

    @Override
    public CursorResponse<FeedResponse> getAllFeed(FeedSearchRequest request, UUID authorIdEqual) {
        log.debug("Feed 목록 조회");

        if (!userRepository.existsById(authorIdEqual))
            throw new UserNotFoundException(authorIdEqual);

        FeedSearchCondition condition = FeedSearchCondition.from(request);

        Slice<Feed> feedPage = feedRepository.findAllByKeywordLike(condition);
        List<Feed> content = feedPage.getContent();
        if (content.isEmpty())
            return new CursorResponse<>(List.of(), null, null,
                    false, 0L, request.sortBy(), request.sortDirection());

        long totalCount = feedRepository.countTotalElements(condition);

        List<UUID> feedIds = content.stream().map(Feed::getId).toList();
        Set<UUID> likedFeedIds = likeRepository.findFeedIdsByUserIdAndFeedIdIn(authorIdEqual, feedIds);

        List<FeedResponse> data = content.stream()
                .map(feed -> {
                    boolean likedByMe = likedFeedIds.contains(feed.getId());
                    return feedMapper.toDto(feed, likedByMe);
                }).toList();

        String nextCursor = null;
        UUID nextIdAfter = null;

        if (feedPage.hasNext()) {
            Feed lastFeed = content.get(data.size() - 1);

            nextCursor = request.sortBy().equals("createdAt") ?
                    String.valueOf(lastFeed.getCreatedAt()) :
                    String.valueOf(lastFeed.getLikeCount());
            nextIdAfter = lastFeed.getId();
        }

        return new CursorResponse<>(data, nextCursor, nextIdAfter,
                feedPage.hasNext(), totalCount, request.sortBy(), request.sortDirection());
    }

    @Override
    @Transactional
    public FeedResponse updateFeed(UUID id, FeedUpdateRequest request, UUID authorId) {
        log.debug("Feed 수정 요청 - id={}", id);
        Feed feed = feedRepository.findById(id)
                .orElseThrow(() -> new FeedNotFoundException(id));

        if (!feed.getAuthor().getId().equals(authorId))
            throw new IllegalArgumentException("Feed author is not the same as the request author");

        feed.updateContent(request.content());
        log.debug("Feed 수정 완료");

        return feedMapper.toDto(feed);
    }

    @Override
    @Transactional
    public void deleteFeed(UUID id, UUID authorId) {
        log.debug("Feed 삭제 요청 - id={}", id);
        Feed feed = feedRepository.findById(id)
                .orElseThrow(() -> new FeedNotFoundException(id));

        if (!feed.getAuthor().getId().equals(authorId))
            throw new IllegalArgumentException("Feed author is not the same as the request author");

        likeRepository.deleteAllByFeedId(id);
        commentRepository.deleteAllByFeedId(id);
        feedRepository.delete(feed);

        log.debug("Feed 삭제 완료");
    }

    private FeedWeather getWeatherInformation(UUID weatherId) {
        Weather weather = weatherRepository.findById(weatherId)
                .orElseThrow(() -> new IllegalArgumentException("weatherId is invalid"));

        return FeedWeather.builder()
                .weatherId(weather.getId())
                .skyStatus(weather.getSkyStatus())
                .precipitationType(weather.getPrecipitationType())
                .precipitationAmount(weather.getPrecipitationAmount())
                .precipitationProbability(weather.getPrecipitationProbability())
                .temperatureCurrent(weather.getTemperatureCurrent())
                //.temperatureComparedToDayBefore(weather.getTemperatureCurrent() - weather.getTemperatureYesterday())
                .temperatureMin(weather.getTemperatureMin())
                .temperatureMax(weather.getTemperatureMax())
                .build();
    }
}
