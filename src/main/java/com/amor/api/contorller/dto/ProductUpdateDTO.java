package com.amor.api.contorller.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProductUpdateDTO {

    private String productName;
    private Long productPrice;
    private Integer categoryNo;
}
