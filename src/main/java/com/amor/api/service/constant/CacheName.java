package com.amor.api.service.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Duration;

@Getter
@RequiredArgsConstructor
public enum CacheName {

    CATEGORY_NAMES_API("CATEGORY_NAMES_API", Duration.ofSeconds(600)),
    PRODUCT_CATEGORY_NAMES("PRODUCT_CATEGORY_NAMES", Duration.ofSeconds(600)),
    CATEGORY_ALL("CATEGORY_ALL", Duration.ofMinutes(10) );

    private final String cacheName;
    private final Duration duration;
}
