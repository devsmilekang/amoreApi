package com.amor.api.contorller.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProductInsertDTO {
    private String brandName;
    private String productName;
    private long productPrice;
    private Integer categoryNo;
}
