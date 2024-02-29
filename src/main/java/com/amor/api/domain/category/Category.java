package com.amor.api.domain.category;

import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.util.StringUtils;

@Entity
@Getter
public class Category {

    @Id
    @Column(name = "category_no", nullable = false)
    private Integer categoryNo;

    @Column(name = "category_name")
    private String categoryName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_no")
    private Category parentCategory;

    @Column(name = "depth", nullable = false)
    private Integer depth;

    public void changeCategoryName(String categoryName) {
        if (StringUtils.hasText(categoryName)) {
            this.categoryName = categoryName;
        }
    }
}
