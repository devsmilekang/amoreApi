package com.amor.api.contorller;

import com.amor.api.contorller.dto.ResponseListDTO;
import com.amor.api.service.cache.CacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/caches")

public class CacheController {

    private final CacheService cacheService;

    @GetMapping("/cacheName")
    public ResponseEntity<ResponseListDTO<String>> getCacheKeys(){
        return ResponseEntity.ok(new ResponseListDTO<>(cacheService.getKeys()));
    }

    @DeleteMapping("/{cacheName}")
    public ResponseEntity<Void> deleteCache(@PathVariable String cacheName){
        cacheService.remove(cacheName);
        return ResponseEntity.ok(null);
    }
}
