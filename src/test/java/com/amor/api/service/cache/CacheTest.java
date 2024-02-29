package com.amor.api.service.cache;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

class CacheTest {

    @Test
    @DisplayName("만료시간_체크")
    public void isExpired(){
      Cache<String> cache = Cache.<String>builder()
              .hitCount(1)
              .creationTime(LocalDateTime.of(2024, 2, 29, 1,0,0))
              .expirationTime(LocalDateTime.of(2024, 2, 29, 1,0,0))
              .data("1")
              .build();

        Assertions.assertTrue(cache.isExpired(LocalDateTime.of(2024, 2, 29, 1,0,1)));
        Assertions.assertTrue(cache.isNotExpired(LocalDateTime.of(2024, 2, 29, 0,59,59)));
        Assertions.assertFalse(cache.isExpired(LocalDateTime.of(2024, 2, 29, 0,59,59)));
        Assertions.assertFalse(cache.isNotExpired(LocalDateTime.of(2024, 2, 29, 1,0,1)));
        Assertions.assertFalse(cache.isNotExpired(null));
        Assertions.assertTrue(cache.isExpired(null));
    }

    @Test
    @DisplayName("초당 호출 회수 계산")
    public void calculateCallsPerSecond(){
        Cache<String> cache = Cache.<String>builder()
                .hitCount(10)
                .creationTime(LocalDateTime.of(2024, 2, 29, 1,0,0))
                .expirationTime(LocalDateTime.of(2024, 2, 29, 1,0,0))
                .data("1")
                .build();

        Assertions.assertEquals(10, cache.calculateCallsPerSecond(LocalDateTime.of(2024, 2, 29, 1, 0, 1)));
        Assertions.assertEquals(1, cache.calculateCallsPerSecond(LocalDateTime.of(2024, 2, 29, 1, 0, 10)));

    }
}