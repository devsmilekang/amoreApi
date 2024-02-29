package com.amor.api.service.category;

import com.amor.api.contorller.dto.CategoryDTO;
import com.amor.api.contorller.dto.CategoryNameDTO;
import com.amor.api.contorller.dto.CategoryUpdateDTO;
import com.amor.api.domain.category.Category;
import com.amor.api.exception.BizException;
import com.amor.api.repository.CategoryRepository;
import com.amor.api.service.cache.CacheService;
import com.amor.api.service.constant.CacheName;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CacheService cacheService;

    @Transactional(readOnly = true)
    public List<CategoryDTO> getAllCategories() {
        Optional<List<CategoryDTO>> categoryDTOOptional = cacheService.get(CacheName.CATEGORY_ALL);
        return categoryDTOOptional.orElseGet(() -> {
            List<Category> depth1Categories = categoryRepository.findByDepth(1);

            List<CategoryDTO> list = depth1Categories.stream()
                    .map(this::mapToCategoryDTO)
                    .toList();

            cacheService.put(CacheName.CATEGORY_ALL, list);
            return list;
        });
    }

    private CategoryDTO mapToCategoryDTO(Category category) {
        List<Category> subCategories = categoryRepository.findByParentCategory(category);
        List<CategoryDTO> subCategoryDTOs = subCategories.stream()
                .map(this::mapToCategoryDTO)
                .collect(Collectors.toList());

        return new CategoryDTO(category.getCategoryNo(), category.getCategoryName(),
                category.getDepth(), subCategoryDTOs);
    }

    @Transactional(readOnly = true)
    public List<CategoryNameDTO> getAllCategoryNames() {
        Optional<List<CategoryNameDTO>> cachedCategoriesOptional = cacheService.get(CacheName.CATEGORY_NAMES_API);
        /*
         * 카테고리 이름이 캐시되어 있는 경우 캐시된 값을 반환
         * 캐시된 값이 없는 경우 새로운 카테고리 이름을 조회하여 캐시에 저장
         */

        return cachedCategoriesOptional.orElseGet(() -> {
            List<Category> depth1Categories = categoryRepository.findByDepth(1);

            List<CategoryNameDTO> categoryNameDTOS = new ArrayList<>(); // 카테고리 이름 반환 목록
            Map<Integer, CategoryPathName> categoryPathNameMap = new HashMap<>(); // 카테고리 키를 통한 캐쉬 저장용 목록

            for (Category category : depth1Categories) {
                StringBuilder categoryPathNameBuilder = new StringBuilder(category.getCategoryName());

                categoryPathNameMap.put(category.getCategoryNo(), CategoryPathName.builder()
                        .categoryNo(category.getCategoryNo())
                        .categoryPathName(category.getCategoryName())
                        .build());

                this.appendSubCategoryPathNames(category, categoryPathNameBuilder, categoryPathNameMap);
                categoryNameDTOS.add(new CategoryNameDTO(categoryPathNameBuilder.toString()));
            }

            cacheService.put(CacheName.CATEGORY_NAMES_API, categoryNameDTOS);
            cacheService.put(CacheName.PRODUCT_CATEGORY_NAMES, categoryPathNameMap);

            return categoryNameDTOS;
        });
    }

    private void appendSubCategoryPathNames(Category parentCategory, StringBuilder categoryPathNameBuilder, Map<Integer, CategoryPathName> categoryPathNameMap) {
        List<Category> subCategories = categoryRepository.findByParentCategoryCategoryNo(parentCategory.getCategoryNo());
        for (Category subCategory : subCategories) {
            categoryPathNameBuilder.append("-").append(subCategory.getCategoryName());
            categoryPathNameMap.put(subCategory.getCategoryNo(), CategoryPathName.builder()
                    .categoryNo(subCategory.getCategoryNo())
                    .categoryPathName(categoryPathNameBuilder.toString())
                    .build());
            this.appendSubCategoryPathNames(subCategory, categoryPathNameBuilder, categoryPathNameMap);
        }
    }

    @Transactional
    public void updateCategory(Integer categoryNo, CategoryUpdateDTO categoryUpdateDTO) {
        Category category = categoryRepository.findById(categoryNo)
                .orElseThrow(() -> new BizException("존재하지 않는 카테고리입니다."));

        category.changeCategoryName(categoryUpdateDTO.getCategoryName());
    }
}
