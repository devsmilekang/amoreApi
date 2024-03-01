package com.amor.api.service.cache;

import lombok.Builder;
import lombok.Getter;

import java.time.Duration;
import java.time.LocalDateTime;

@Getter
@Builder
public class Cache<T> {
    private T data;
    private LocalDateTime creationTime;
    private LocalDateTime expirationTime;
    private long hitCount;

    public boolean isNotExpired(LocalDateTime localDateTime){
        return !isExpired(localDateTime);
    }

    public boolean isExpired(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return true;
        }
        return localDateTime.isAfter(expirationTime);
    }

    protected void increaseHitCount(){
        if(Long.MAX_VALUE - 1 > hitCount) {
            this.hitCount += 1;
        }
    }

    protected double calculateCallsPerSecond(LocalDateTime now) {
        Duration duration = Duration.between(creationTime, now);
        return (double) hitCount / ((double) duration.toMillis() / 1000);
    }
}
