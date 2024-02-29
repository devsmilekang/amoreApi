package com.amor.api.repository;

import com.amor.api.domain.product.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("SELECT p FROm Product p WHERE p.categoryNo = :categoryNo")
    List<Product> findByCategoryNo(@Param("categoryNo") Integer categoryNo);

    void deleteByProductNo(Long productNo);
}
