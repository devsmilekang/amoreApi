package com.amor.api.service.cache;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

class CacheServiceTest {

    @Test
    @DisplayName("만료된 캐시는 다음 캐시 put 동작에서 삭제")
    void evictCache_expired() throws InterruptedException {
        //given
        CacheService cacheService = new CacheService();
        cacheService.put("1", "value1", Duration.ofDays(1));
        cacheService.put("2", "value2", Duration.ofSeconds(1));
        Thread.sleep(1001);

        //when
        cacheService.evictCache();

        //then
        ConcurrentHashMap<String, Cache<Object>> cache = cacheService.getCache();
        Assertions.assertEquals("value1", cache.get("1").getData());
        Assertions.assertNull(cache.get("2"));

    }

    @Test
    @DisplayName("만료된_캐시가_없을_때_히트카운트/시간_로직_삭제")
    void evictCache_non_expired_remove_one_cache() throws InterruptedException {
        //given
        CacheService cacheService = new CacheService();
        cacheService.setCacheMaxSize(3);

        cacheService.put("1", "value1", Duration.ofDays(1));
        Thread.sleep(5001);
        cacheService.put("2", "value2", Duration.ofDays(1));
        Thread.sleep(1001);
        cacheService.put("3", "value2", Duration.ofDays(1));
        Thread.sleep(1001);

        cacheService.get("1");
        cacheService.get("1");
        cacheService.get("1");
        cacheService.get("1");

        cacheService.get("2");
        cacheService.get("2");
        cacheService.get("2");

        cacheService.get("3");
        cacheService.get("3");
        cacheService.get("3");

        cacheService.put("4", "value2", Duration.ofDays(1));

        ConcurrentHashMap<String, Cache<Object>> cache = cacheService.getCache();
        Assertions.assertNull(cache.get("1"));
        Assertions.assertNotNull(cache.get("2").getData());
        Assertions.assertNotNull(cache.get("3").getData());
    }

    @Test
    @DisplayName("캐시만료전_적재필요여부")
    public void isReloadCacheBeforeExpiration(){
        CacheService cacheService = new CacheService();
        ConcurrentHashMap<String, Cache<Object>> cache = cacheService.getCache();
        cache.put("1", Cache.builder()
                        .hitCount(500)
                        .creationTime(LocalDateTime.of(2024, 2, 29, 1,0,0))
                        .expirationTime(LocalDateTime.of(2024, 2, 29, 2,0,20))
                        .build());

        LocalDateTime localDateTime = LocalDateTime.of(2024, 2, 29, 2, 0, 1);
        Assertions.assertFalse(cacheService.isReloadCacheBeforeExpiration("1", Duration.ofSeconds(20), 501, localDateTime), "hit 카운트 부족");
        Assertions.assertFalse(cacheService.isReloadCacheBeforeExpiration("1", Duration.ofSeconds(18), 499, localDateTime), "시간 부족");
        Assertions.assertTrue(cacheService.isReloadCacheBeforeExpiration("1", Duration.ofSeconds(20), 499, localDateTime), "성공");
    }
}