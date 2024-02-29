package com.amor.api.domain.product;

import com.amor.api.contorller.dto.ProductInsertDTO;
import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

@Entity
@Getter
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "product_sequence")
    @SequenceGenerator(name = "product_sequence", sequenceName = "product_sequence", allocationSize = 30)
    @Column(name = "product_no", nullable = false)
    private Long productNo;

    @Column(name = "brand_name")
    private String brandName;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "product_price")
    private BigDecimal productPrice;

    @Column(name = "category_no")
    private Integer categoryNo;

    public static Product createProduct(ProductInsertDTO dto) {
        Product product = new Product();
        product.brandName = dto.getBrandName();
        product.productName = dto.getProductName();
        product.productPrice = new BigDecimal(dto.getProductPrice());
        product.categoryNo = dto.getCategoryNo();
        return product;
    }

    public void changeProductName(String productName) {
        if (StringUtils.hasText(productName)) {
            this.productName = productName;
        }
    }

    public void changeCategoryNo(Integer categoryNo) {
        if (categoryNo != null) {
            this.categoryNo = categoryNo;
        }
    }

    public void changeProductPrice(Long productPrice) {
        if (productPrice != null) {
            this.productPrice = new BigDecimal(categoryNo);
        }
    }
}
