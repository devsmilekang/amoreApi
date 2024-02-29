package com.amor.api.service.cache;

import com.amor.api.service.constant.CacheName;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Service
@Slf4j
public class CacheService {

    private final ConcurrentHashMap<String, Cache<Object>> cache = new ConcurrentHashMap<>();
    private int CACHE_MAX_SIZE = 5;
    private final CopyOnWriteArraySet<String> cacheNames = new CopyOnWriteArraySet<>();

    public <T> Optional<T> get(CacheName cacheName){
        return this.get(cacheName.getCacheName());
    }

    public <T> Optional<T> get(String cacheName){
        try{
            Cache<T> cacheValue = (Cache<T>) cache.get(cacheName);

            if (cacheValue != null && cacheValue.isNotExpired(LocalDateTime.now())) {
                log.debug("캐시 HIT! cacheName='{}'", cacheName);

                cacheValue.increaseHitCount();   // hit 카운트 증가

                return Optional.ofNullable(cacheValue.getData());
            } else{
                log.debug("캐시 MISS! cacheName='{}'", cacheName);
                return Optional.empty();
            }
        }
        catch (Exception e){
            log.error("캐시 조회 에러", e);
            return Optional.empty();
        }
    }

    public LocalDateTime getExpiredTime(String cacheName){
        try{
            Cache<Object> cacheValue = cache.get(cacheName);
            if (cacheValue == null) {
                return null;
            }
            return cacheValue.getExpirationTime();
        }
        catch (Exception e){
            log.error("캐시 만료시간 조회 에러", e);
            return null;
        }
    }

    @Async
    public <T> void put(String cacheName, T value, Duration duration) {
        try {
            if (!StringUtils.hasText(cacheName)) {
                throw new IllegalArgumentException("캐시에 넣을 캐시명이 없습니다.");
            }

            if (value == null) {
                throw new IllegalArgumentException("캐시에 넣을 값이 없습니다.");
            }

            if (duration == null) {
                throw new IllegalArgumentException("캐시 유지시간이 존재하지 않습니다.");
            }

            // 캐시 개수 초과시 evict
            evictCache();

            LocalDateTime expirationTime = LocalDateTime.now().plus(duration);

            cache.put(cacheName, Cache.<Object>builder()
                    .data(value)
                    .creationTime(LocalDateTime.now())
                    .expirationTime(expirationTime)
                    .hitCount(1)
                    .build());

            //등록된 캐시 이름 추가
            cacheNames.add(cacheName);

            log.debug("캐시 PUT! cacheName='{}'", cacheName);

        } catch (Exception e) {
            log.error("캐시 적재 중 오류가 발생하였습니다.", e);
        }
    }

    @Async
    public <T> void put(CacheName cacheName, T value){
        if(cacheName == null){
            throw new IllegalArgumentException("캐시에 넣을 캐시명이 없습니다.");
        }

        this.put(cacheName.getCacheName(), value, cacheName.getDuration());
    }

    /*
        1. 만료된 캐시를 캐시 삽입시 삭제한다.
        2. 캐시가 가득차서 더 넣을 곳이 없다면 호출횟수/(현재시간-생성시점[초])로 제일 작은 값을 제거한다.

        첫 번째는 캐시를 사용하는 사용자가 판단하여 캐시를 얼만큼 유지할 것인지 판단하여 등록한다.
         - 짧은 주기를 입력한 경우는 최신정보가 중요할 때 사용한다. (상품의 가격 등 변동과 영향이 심한 것)
         - 긴 주기를 입력한 경우는 변동성이 별로 없고 최신정보가 바로 교체되지 않아도 괜찮을 때 사용할 수 있다.
        이와 같이 캐시에 대하여 사용자가 판단하여 사용할 수 있도록 만료시간을 지정한다.

        두 번째는 초당 호출횟수를 기준으로 가장 적게 사용된 것을 제거한다.
         - 사용이 빈번하지 않은 캐시를 제거하기 위함이다.
         - 한 번만 사용하였는데 캐시에 올라가있거나 오랜 시간이 지났지만 사용을 별로 하지 않았을 때 우선순위가 되기 위함이다.
     */
    protected void evictCache() {

        List<String> expiredList = cacheNames.stream()
                .filter(cache::containsKey)
                .filter(cacheNames -> cache.get(cacheNames).isExpired(LocalDateTime.now()))
                .toList();

        for (String expiredCacheName : expiredList) {
            log.debug("캐시 expired! cacheName='{}'", expiredCacheName);
            this.remove(expiredCacheName);
        }

        if(cacheNames.size() >= CACHE_MAX_SIZE){
            Optional<String> leastUsedKey = cache.entrySet().stream()
                    .min(Map.Entry.comparingByValue(Comparator.comparingDouble(cache -> cache.calculateCallsPerSecond(LocalDateTime.now()))))
                    .map(Map.Entry::getKey);

            leastUsedKey.ifPresent(cache::remove);
        }
    }

    public boolean isReloadCacheBeforeExpiration(String cacheName, Duration beforeTerm, long overHitCount, LocalDateTime now){
        Cache<Object> targetCache = cache.get(cacheName);

        if(targetCache == null){
            return false;
        }

        LocalDateTime renewedTime = now.plus(beforeTerm);
        boolean isRenewedTimeAfterExpirationTime = renewedTime.isAfter(targetCache.getExpirationTime());

        return overHitCount <= targetCache.getHitCount() && isRenewedTimeAfterExpirationTime;
    }

    public void remove(String cacheName){
        log.debug("캐시 제거! cacheName='{}'", cacheName);
        cache.remove(cacheName);
        cacheNames.remove(cacheName);
    }
    public List<String> getKeys(){
        return cacheNames.stream().toList();
    }

    // TEST CASE 확인을 위해 추가
    protected ConcurrentHashMap<String, Cache<Object>> getCache(){
        return cache;
    }

    // TEST CASE 확인을 위해 추가
    protected void setCacheMaxSize(int size){
        this.CACHE_MAX_SIZE = size;
    }

}
