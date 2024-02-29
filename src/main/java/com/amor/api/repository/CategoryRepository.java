package com.amor.api.repository;

import com.amor.api.domain.category.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {

    List<Category> findByDepth(int depth);

    List<Category> findByParentCategory(Category parentCategory);

    List<Category> findByParentCategoryCategoryNo(int parentCategoryNo);

    Category findByCategoryNo(Integer categoryNo);
}
