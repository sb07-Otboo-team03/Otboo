package com.codeit.otboo.domain.feed.service;

import com.codeit.otboo.domain.clothes.management.entity.Clothes;
import com.codeit.otboo.domain.clothes.management.repository.ClothesRepository;
import com.codeit.otboo.domain.comment.repository.CommentRepository;
import com.codeit.otboo.domain.feed.dto.mapper.FeedMapper;
import com.codeit.otboo.domain.feed.dto.request.*;
import com.codeit.otboo.domain.feed.dto.response.FeedResponse;
import com.codeit.otboo.domain.feed.elasticsearch.document.FeedDocument;
import com.codeit.otboo.domain.feed.elasticsearch.event.FeedDeletedEvent;
import com.codeit.otboo.domain.feed.elasticsearch.event.FeedSyncEvent;
import com.codeit.otboo.domain.feed.elasticsearch.event.FeedUpdatedEvent;
import com.codeit.otboo.domain.feed.elasticsearch.service.FeedDocumentService;
import com.codeit.otboo.domain.feed.entity.Feed;
import com.codeit.otboo.domain.feed.entity.FeedWeather;
import com.codeit.otboo.domain.feed.exception.FeedNotFoundException;
import com.codeit.otboo.domain.feed.repository.FeedRepository;
import com.codeit.otboo.domain.follow.repository.FollowRepository;
import com.codeit.otboo.domain.like.repository.LikeRepository;
import com.codeit.otboo.domain.sse.event.FeedCreatedEvent;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.user.exception.UserNotFoundException;
import com.codeit.otboo.domain.user.repository.UserRepository;
import com.codeit.otboo.domain.weather.entity.Weather;
import com.codeit.otboo.domain.weather.entity.YesterdayHourlyWeather;
import com.codeit.otboo.domain.weather.exception.WeatherNotFoundException;
import com.codeit.otboo.domain.weather.exception.YesterdayWeatherNotFoundException;
import com.codeit.otboo.domain.weather.repository.WeatherRepository;
import com.codeit.otboo.domain.weather.repository.YesterdayHourlyWeatherRepository;
import com.codeit.otboo.global.slice.dto.CursorResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Slice;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class FeedServiceImpl implements FeedService {

    private final FeedRepository feedRepository;
    private final UserRepository userRepository;
    private final WeatherRepository weatherRepository;
    private final ClothesRepository clothesRepository;
    private final LikeRepository likeRepository;
    private final FeedMapper feedMapper;
    private final CommentRepository commentRepository;
    private final FollowRepository followRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final YesterdayHourlyWeatherRepository yesterdayHourlyWeatherRepository;
    private final FeedDocumentService feedDocumentService;

    @Override
    @Transactional
    @PreAuthorize("#request.authorId() == authentication.principal.userResponse.id()")
    public FeedResponse createFeed(FeedCreateRequest request) {
        User author = userRepository.findById(request.authorId())
                .orElseThrow(() -> new UserNotFoundException(request.authorId()));

        FeedWeather weather = getWeatherInformation(request.weatherId());

        List<Clothes> clothesList = clothesRepository.findAllById(request.clothesIds());

        Feed feed = new Feed(request.content(), author, weather, clothesList);
        feedRepository.save(feed);

        eventPublisher.publishEvent(new FeedSyncEvent(feed.getId(),
                feed.getContent(),
                feed.getWeather().getSkyStatus().name(),
                feed.getWeather().getPrecipitationType().name(),
                feed.getAuthor().getId(),
                feed.getCreatedAt(),
                feed.getLikeCount()));

        Set<UUID> followerIds = followRepository.findAllFollowerIdsByFolloweeId(author.getId());
        String title = author.getProfile().getName() + "님이 새로운 피드를 작성했어요.";
        String content = feed.getContent();
        List<UUID> receiverIds = followerIds.stream().toList();
        eventPublisher.publishEvent(new FeedCreatedEvent(title, content, receiverIds));

        return feedMapper.toDto(feed);
    }

    @Override
    public CursorResponse<FeedResponse> getAllFeed(FeedSearchRequest request, UUID userId) {
        FeedSearchCondition condition = FeedSearchCondition.from(request);

        try {
            return getFeedsFromElasticsearch(condition, userId);
        } catch (Exception e) {
            log.warn("Elasticsearch 실패", e);
            return getFeedsFromDatabase(condition, userId);
        }
    }

    @Override
    @Transactional
    public FeedResponse updateFeed(UUID id, FeedUpdateRequest request, UUID authorId) {
        Feed feed = feedRepository.findById(id)
                .orElseThrow(() -> new FeedNotFoundException(id));

        if (!feed.getAuthor().getId().equals(authorId))
            throw new AccessDeniedException(String.format("피드 권한 없음 [feedId: %s, authorId: %s, requestUserId: %s]",
                    id, feed.getAuthor().getId(), authorId));

        feed.updateContent(request.content());
        eventPublisher.publishEvent(new FeedUpdatedEvent(feed.getId(), feed.getContent()));

        return feedMapper.toDto(feed);
    }

    @Override
    @Transactional
    public void deleteFeed(UUID id, UUID authorId) {
        Feed feed = feedRepository.findById(id)
                .orElseThrow(() -> new FeedNotFoundException(id));

        if (!feed.getAuthor().getId().equals(authorId))
            throw new AccessDeniedException(String.format("피드 권한 없음 [feedId: %s, authorId: %s, requestUserId: %s]",
                    id, feed.getAuthor().getId(), authorId));

        likeRepository.deleteAllByFeedId(id);
        commentRepository.deleteAllByFeedId(id);
        feedRepository.delete(feed);
        eventPublisher.publishEvent(new FeedDeletedEvent(id));
    }

    private FeedWeather getWeatherInformation(UUID weatherId) {
        Weather weather = weatherRepository.findById(weatherId)
                .orElseThrow(() -> new WeatherNotFoundException(weatherId));

        LocalDateTime forecastAt = weather.getForecastAt();
        LocalDate date = forecastAt.toLocalDate().minusDays(1);
        LocalTime time = forecastAt.toLocalTime().withMinute(0).withSecond(0).withNano(0);

        // 현재 시간의 오늘 날씨 정보에 대응하는 어제 날씨 정보 저장
        YesterdayHourlyWeather yesterdayWeather =
                yesterdayHourlyWeatherRepository.findByXAndYAndDateAndHour(weather.getX(), weather.getY(), date, time)
                        .orElseThrow(() -> new YesterdayWeatherNotFoundException(weather.getX(), weather.getY(), date, time));

        return FeedWeather.builder()
                .weatherId(weather.getId())
                .skyStatus(weather.getSkyStatus())
                .precipitationType(weather.getPrecipitationType())
                .precipitationAmount(weather.getPrecipitationAmount())
                .precipitationProbability(weather.getPrecipitationProbability())
                .temperatureCurrent(weather.getTemperatureCurrent())
                .temperatureComparedToDayBefore(weather.getTemperatureCurrent() - yesterdayWeather.getTemperature())
                .temperatureMin(weather.getTemperatureMin())
                .temperatureMax(weather.getTemperatureMax())
                .build();
    }

    private CursorResponse<FeedResponse> getFeedsFromElasticsearch(FeedSearchCondition condition, UUID userId) {
        SearchHits<FeedDocument> searchHits = feedDocumentService.getAllByElasticsearch(condition);
        List<FeedDocument> content = searchHits.stream()
                .map(SearchHit::getContent)
                .toList();
        if (content.isEmpty())
            return new CursorResponse<>(List.of(), null, null,
                    false, 0L, condition.sortBy(), condition.sortDirection());

        boolean hasNext = content.size() > condition.limit();
        if (hasNext) content = content.subList(0, condition.limit());
        long totalCount = searchHits.getTotalHits();

        List<UUID> feedIds = content.stream().map(doc -> UUID.fromString(doc.getId())).toList();
        List<Feed> feeds = feedRepository.findAllById(feedIds);

        Map<UUID, Feed> feedMap = feeds.stream().collect(Collectors.toMap(Feed::getId, f -> f));
        Set<UUID> likedFeedIds = likeRepository.findFeedIdsByUserIdAndFeedIdIn(userId, feedIds);

        List<FeedResponse> data = feedIds.stream()
                .map(feedMap::get)
                .map(feed -> feedMapper.toDto(feed, likedFeedIds.contains(feed.getId())))
                .toList();

        String nextCursor = null;
        UUID nextIdAfter = null;
        if (hasNext) {
            FeedDocument lastDoc = content.get(content.size() - 1);
            nextCursor = condition.sortBy().equals("createdAt") ?
                    String.valueOf(lastDoc.getCreatedAt()) :
                    String.valueOf(lastDoc.getLikeCount());
            nextIdAfter = UUID.fromString(lastDoc.getId());
        }

        return new CursorResponse<>(data, nextCursor, nextIdAfter,
                hasNext, totalCount, condition.sortBy(), condition.sortDirection());

    }

    private CursorResponse<FeedResponse> getFeedsFromDatabase(FeedSearchCondition condition, UUID userId) {
        Slice<Feed> feedPage = feedRepository.findAllByKeywordLike(condition);
        List<Feed> content = feedPage.getContent();
        if (content.isEmpty())
            return new CursorResponse<>(List.of(), null, null,
                    false, 0L, condition.sortBy(), condition.sortDirection());

        long totalCount = feedRepository.countTotalElements(condition);

        List<UUID> feedIds = content.stream().map(Feed::getId).toList();
        Set<UUID> likedFeedIds = likeRepository.findFeedIdsByUserIdAndFeedIdIn(userId, feedIds);

        List<FeedResponse> data = content.stream()
                .map(feed -> {
                    boolean likedByMe = likedFeedIds.contains(feed.getId());
                    return feedMapper.toDto(feed, likedByMe);
                }).toList();

        String nextCursor = null;
        UUID nextIdAfter = null;

        if (feedPage.hasNext()) {
            Feed lastFeed = content.get(data.size() - 1);

            nextCursor = condition.sortBy().equals("createdAt") ?
                    String.valueOf(lastFeed.getCreatedAt()) :
                    String.valueOf(lastFeed.getLikeCount());
            nextIdAfter = lastFeed.getId();
        }

        return new CursorResponse<>(data, nextCursor, nextIdAfter,
                feedPage.hasNext(), totalCount, condition.sortBy(), condition.sortDirection());
    }
}
