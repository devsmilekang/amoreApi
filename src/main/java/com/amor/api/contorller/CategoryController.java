package com.amor.api.contorller;

import com.amor.api.contorller.dto.*;
import com.amor.api.service.category.CategoryService;
import com.amor.api.service.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;
    private final ProductService productService;

    @GetMapping("")
    public ResponseEntity<ResponseListDTO<CategoryDTO>> getAllCategory() {
        return ResponseEntity.ok(new ResponseListDTO<>(categoryService.getAllCategories()));
    }

    @GetMapping("/names")
    public ResponseEntity<ResponseListDTO<CategoryNameDTO>> categoryNames() {
        return ResponseEntity.ok(new ResponseListDTO<>(categoryService.getAllCategoryNames()));
    }

    @GetMapping("/{categoryNo}/products")
    public ResponseEntity<ResponseListDTO<ProductDTO>> getProductsByCategory(@PathVariable Integer categoryNo) {
        return ResponseEntity.ok(new ResponseListDTO<>(productService.getProductsByCategory(categoryNo)));
    }

    @PutMapping("/{categoryNo}")
    public ResponseEntity<ResponseDTO<Void>> updateCategory(@PathVariable Integer categoryNo, @RequestBody CategoryUpdateDTO categoryUpdateDTO) {
        categoryService.updateCategory(categoryNo, categoryUpdateDTO);
        return ResponseEntity.ok(new ResponseDTO<>(null));
    }

}
