package com.amor.api.service.product;

import com.amor.api.contorller.dto.ProductDTO;
import com.amor.api.contorller.dto.ProductInsertDTO;
import com.amor.api.contorller.dto.ProductUpdateDTO;
import com.amor.api.domain.category.Category;
import com.amor.api.domain.product.Product;
import com.amor.api.exception.BizException;
import com.amor.api.repository.CategoryRepository;
import com.amor.api.repository.ProductRepository;
import com.amor.api.service.cache.CacheService;
import com.amor.api.service.category.CategoryPathName;
import com.amor.api.service.constant.CacheName;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final CacheService cacheService;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<ProductDTO> getProductsByCategory(Integer categoryNo) {

        Category category = categoryRepository.findByCategoryNo(categoryNo);
        if(category == null){
            return null;
        }

        Optional<List<ProductDTO>> prodctCacheOptional = cacheService.get("product_by_category_" + categoryNo);
        return prodctCacheOptional.orElseGet(() -> {
            List<ProductDTO> products = productRepository.findByCategoryNo(categoryNo).stream()
                    .map(product -> mapToProductDTO(product, categoryNo))
                    .collect(Collectors.toList());

            cacheService.put("product_by_category_" + categoryNo, products, Duration.ofSeconds(60));
            return products;
        });
    }

    /*
     * 상품 정보를 DTO로 변환
     */
    private ProductDTO mapToProductDTO(Product product, Integer categoryNo) {
        return ProductDTO.builder()
                .productName(product.getProductName())
                .productPrice(product.getProductPrice().longValue())
                .categoryName(getProductCategoryName(categoryNo))
                .build();
    }

    /*
     * 카테고리 번호로 상품 카테고리 이름을 조회
     */
    private String getProductCategoryName(Integer categoryNo) {
        Optional<Map<Integer, CategoryPathName>> cachedCategoriesOptional = cacheService.get(CacheName.PRODUCT_CATEGORY_NAMES);

        /*
         * 카테고리 이름 캐시가 존재하면 캐시에서 조회
         * 카테고리 이름 캐시가 존재하지 않으면 캐시에 저장
         */
        Map<Integer, CategoryPathName> categoryPathNameMap = cachedCategoriesOptional.orElseGet(() -> {
            Map<Integer, CategoryPathName> categoryNames = new HashMap<>(1);
            categoryNames.put(categoryNo, CategoryPathName.builder()
                    .categoryNo(categoryNo)
                    .categoryPathName(getProductCategoryPath(categoryNo))
                    .build());

            cacheService.put(CacheName.PRODUCT_CATEGORY_NAMES, categoryNames);
            return categoryNames;
        });

        CategoryPathName categoryPathName = categoryPathNameMap.get(categoryNo);

        // 캐시에 저장된 카테고리 이름이 없으면 조회해서 캐시에 저장
        if(categoryPathName == null){
            String categoryPath = getProductCategoryPath(categoryNo);

            Map<Integer, CategoryPathName> categoryPathNameHashMap = cachedCategoriesOptional.orElseGet(HashMap::new);

            categoryPathNameHashMap.put(categoryNo, CategoryPathName.builder()
                    .categoryNo(categoryNo)
                    .categoryPathName(categoryPath)
                    .build());
            cacheService.put(CacheName.PRODUCT_CATEGORY_NAMES, categoryPathNameHashMap);

            return categoryPath;
        }

        return categoryPathName.getCategoryPathName();
    }

    /*
     * 카테고리 번호로 카테고리 경로를 조회
     */
    private String getProductCategoryPath(Integer categoryNo) {
        StringBuilder pathBuilder = new StringBuilder();

        Category category = categoryRepository.findByCategoryNo(categoryNo);
        while (category != null) {
            pathBuilder.insert(0, category.getCategoryName());

            if (category.getParentCategory() != null) {
                pathBuilder.insert(0, "-");
            }

            category = category.getParentCategory();
        }

        return pathBuilder.toString();
    }

    /*
     * 상품 번호로 상품 정보 조회
     */
    @Transactional(readOnly = true)
    public ProductDTO findProductByNo(Long productNo) {
        Product product = productRepository.findById(productNo)
                .orElseThrow(() -> new BizException("요청하신 상품정보가 존재하지 않습니다."));

        Optional<ProductDTO> productDTOOptional = cacheService.get("product_by_no_" + productNo);

        // 상품에 몰릴 경우를 대비해서 만료시간 20초 이내로 남았을 때 총 호출횟수 20회가 넘으면 캐시 적재 (테스트를 위해 숫자 작게 조정)
        productDTOOptional.ifPresent(productDTO -> {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                boolean reloadCacheBeforeExpiration = cacheService.isReloadCacheBeforeExpiration("product_by_no_" + productNo, Duration.ofSeconds(20), 20, LocalDateTime.now());
                if (reloadCacheBeforeExpiration) {
                    cacheService.put("product_by_no_" + productNo, mapToProductDTO(product, product.getCategoryNo()), Duration.ofMinutes(10));
                }
            });

            future.thenAccept(result -> {
                log.debug("비동기 캐시 적재 완료!");
            });
        });

        return productDTOOptional.orElseGet(() -> {
            ProductDTO productDTO = mapToProductDTO(product, product.getCategoryNo());
            cacheService.put("product_by_no_" + productNo, productDTO, Duration.ofMinutes(10));
            return productDTO;
        });
    }

    /*
     * 상품 정보 수정
     */
    @Transactional
    public void updateProductByNo(Long productNo, ProductUpdateDTO productUpdateDTO) {
        Product product = productRepository.findById(productNo)
                .orElseThrow(() -> new BizException("존재하지 않는 상품 번호입니다"));

        if(productUpdateDTO.getCategoryNo() != null){
            categoryRepository.findById(productUpdateDTO.getCategoryNo())
                    .orElseThrow(() -> new BizException("존재하지 않는 카테고리 번호입니다"));
        }

        product.changeProductName(productUpdateDTO.getProductName());
        product.changeProductPrice(productUpdateDTO.getProductPrice());
        product.changeCategoryNo(productUpdateDTO.getCategoryNo());

        mapToProductDTO(product, product.getCategoryNo());
    }

    /*
     * 상품 삭제
     */
    @Transactional
    public void deleteProductByNo(Long productNo) {
        productRepository.deleteByProductNo(productNo);
    }


    @Transactional
    public void registerProduct(ProductInsertDTO productDTO) {
        Category category = categoryRepository.findByCategoryNo(productDTO.getCategoryNo());
        if(category == null){
            throw new BizException("존재하지 않는 카테고리 번호입니다");
        }

        Product product = Product.createProduct(productDTO);
        productRepository.save(product);

        mapToProductDTO(product, product.getCategoryNo());
    }
}
