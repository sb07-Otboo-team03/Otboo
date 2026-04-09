package com.codeit.otboo.domain.weather.service;

import com.codeit.otboo.domain.weather.dto.response.WeatherResponse;
import com.codeit.otboo.global.util.TimeProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeatherRedisCacheService {

    private static final String WEATHER_CACHE_PREFIX = "weather:";

    private final RedisTemplate<String, Object> redisTemplate;
    private final TimeProvider timeProvider;

    public List<WeatherResponse> getOrLoad(
            int x,
            int y,
            LocalDateTime forecastAt,
            Supplier<List<WeatherResponse>> loader
    ) {
        String key = generateKey(x, y, forecastAt);

        try {
            Object cached = redisTemplate.opsForValue().get(key); // redis에 key가 있는지 확인
            if (cached instanceof List<?> cachedList) { // redis에 값이 있으면(cache hit) 바로 반환
                @SuppressWarnings("unchecked")
                List<WeatherResponse> responses = (List<WeatherResponse>) cachedList;
                return responses;
            }
        } catch (Exception e) {
            log.warn("Redis 조회 실패. DB fallback으로 진행합니다. key={}", key, e);
            return loader.get();
        }

        // cache miss
        // 실제 DB 조회 로직 실행
        List<WeatherResponse> loaded = loader.get();

        try {
            Duration ttl = calculateTtlUntilNextHour();
            redisTemplate.opsForValue().set(key, loaded, ttl); // redis에 값 저장
        } catch (Exception e) {
            log.warn("Redis 저장 실패. 응답은 정상 반환하고 캐시 저장만 건너뜁니다. key={}", key, e);
        }

        return loaded;
    }

    // 특정 weather 캐시 삭제 메서드
    public void evictCurrentHour(int x, int y, LocalDateTime forecastAt) {
        redisTemplate.delete(generateKey(x, y, forecastAt));
    }

    // 모든 weather 캐시 삭제 메서드
    public void evictAllWeatherCache() {
        Set<String> keys = redisTemplate.keys(WEATHER_CACHE_PREFIX + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    // redis에서 사용할 key 생성 ex) weather:60:127:2026-04-08T17:00
    private String generateKey(int x, int y, LocalDateTime forecastAt) {
        return WEATHER_CACHE_PREFIX + x + ":" + y + ":" + forecastAt;
    }

    // 현재 시각부터 다음 정각까지 남은 시간을 TTL로 설정
    private Duration calculateTtlUntilNextHour() {
        LocalDateTime now = timeProvider.nowDateTime(); // 현재 시간
        LocalDateTime nextHour = now.plusHours(1) // 다음 정각
                .withMinute(0)
                .withSecond(0)
                .withNano(0);

        Duration ttl = Duration.between(now, nextHour);

        // 59분 59초와 같은 경계 시간에 만든 경우, ttl이 0이거나 음수가 될 가능성 존재
        // 그럴 경우 ttl 값을 1초로 설정
        return ttl.isZero() || ttl.isNegative()
                ? Duration.ofSeconds(1)
                : ttl;
    }
}
