package com.amor.api.config;

import com.amor.api.service.category.CategoryService;
import com.amor.api.service.product.ProductService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class LoadingCacheConfig {

    private final CategoryService categoryService;
    private final ProductService productService;

    @PostConstruct
    public void loadingCache(){
        categoryService.getAllCategories();
        categoryService.getAllCategoryNames();
        // 캐시로딩을 위해 카테고리 1과 2를 사전 로딩
        productService.getProductsByCategory(1);
        productService.getProductsByCategory(2);
        productService.getProductsByCategory(3);
        productService.getProductsByCategory(4);
        productService.getProductsByCategory(5);
        productService.getProductsByCategory(6);
        productService.getProductsByCategory(7);
        productService.getProductsByCategory(8);

    }

}
