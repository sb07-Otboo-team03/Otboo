package com.codeit.otboo.domain.feed.service;

import com.codeit.otboo.domain.clothes.management.entity.Clothes;
import com.codeit.otboo.domain.clothes.management.repository.ClothesRepository;
import com.codeit.otboo.domain.feed.dto.mapper.FeedMapper;
import com.codeit.otboo.domain.feed.dto.request.FeedCreateRequest;
import com.codeit.otboo.domain.feed.dto.request.FeedSearchCondition;
import com.codeit.otboo.domain.feed.dto.request.FeedSearchRequest;
import com.codeit.otboo.domain.feed.dto.request.FeedUpdateRequest;
import com.codeit.otboo.domain.feed.dto.response.FeedResponse;
import com.codeit.otboo.domain.feed.entity.Feed;
import com.codeit.otboo.domain.feed.entity.WeatherInformation;
import com.codeit.otboo.domain.feed.repository.FeedRepository;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.user.repository.UserRepository;
import com.codeit.otboo.domain.weather.entity.Weather;
import com.codeit.otboo.domain.weather.repository.WeatherRepository;
import com.codeit.otboo.global.slice.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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

    @Override
    @Transactional
    public FeedResponse createFeed(FeedCreateRequest request) {
        log.info("Feed 생성 요청 - userId={}", request.authorId());

        User author = userRepository.findById(request.authorId())
                .orElseThrow(() -> new IllegalArgumentException("authorId is invalid"));

        WeatherInformation weather = getWeatherInformation(request.weatherId());

        List<Clothes> clothesList = clothesRepository.findAllById(request.clothesIds());

        Feed feed = new Feed(request.content(), author, weather, clothesList);
        feedRepository.save(feed);
        log.info("Feed 생성 완료 - feedId={}", feed.getId());

        return FeedMapper.toDto(feed);
    }

    @Override
    public PageResponse<FeedResponse> getAllFeed(FeedSearchRequest request, UUID authorIdEqual) {
        log.info("Feed 목록 조회");

        User author = userRepository.findById(authorIdEqual)
                .orElseThrow(() -> new IllegalArgumentException("authorId is invalid"));

        FeedSearchCondition condition = FeedSearchCondition.from(request);

        Slice<Feed> feedPage = feedRepository.findAllByKeywordLike(condition);
        long totalCount = feedRepository.countTotalElements(condition);

        List<FeedResponse> data = feedPage.stream()
                .map(feed -> {
                    boolean likedByMe = feed.getLikes().stream()
                            .anyMatch(like -> like.getUser().getId().equals(authorIdEqual));
                    return FeedMapper.toDto(feed, likedByMe);
                }).toList();

        String nextCursor = null;
        UUID nextIdAfter = null;

        if (feedPage.hasNext() && !feedPage.isEmpty()) {
            List<Feed> content = feedPage.getContent();
            Feed lastFeed = content.get(data.size() - 1);

            nextCursor = switch (request.sortBy()) {
                case "createdAt" -> String.valueOf(lastFeed.getCreatedAt());
                default -> String.valueOf(lastFeed.getLikeCount());
            };
            nextIdAfter = lastFeed.getId();
        }


        return new PageResponse<>(data, nextCursor, nextIdAfter,
                feedPage.hasNext(), totalCount, request.sortBy(), request.sortDirection());
    }

    @Override
    @Transactional
    public FeedResponse updateFeed(UUID id, FeedUpdateRequest request) {
        log.info("Feed 수정 요청 - id={}", id);
        Feed feed = feedRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("id is invalid"));

        if (request.content() == null || request.content().isBlank()) {
            throw new IllegalArgumentException("content is required");
        }

        feed.updateContent(request.content());
        feedRepository.save(feed);

        log.info("Feed 수정 완료");

        return FeedMapper.toDto(feed);
    }

    @Override
    @Transactional
    public void deleteFeed(UUID id) {
        log.info("Feed 삭제 요청 - id={}", id);
        Feed feed = feedRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("id is invalid"));
        feedRepository.delete(feed);
        log.info("Feed 삭제 완료");
    }

    private WeatherInformation getWeatherInformation(UUID weatherId) {
        Weather weather = weatherRepository.findById(weatherId)
                .orElseThrow(() -> new IllegalArgumentException("weatherId is invalid"));

        return WeatherInformation.builder()
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
