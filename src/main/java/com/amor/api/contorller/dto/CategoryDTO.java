package com.amor.api.contorller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO {

    private Integer categoryNo;
    private String categoryName;
    private Integer depth;
    private List<CategoryDTO> subCategories;
}
