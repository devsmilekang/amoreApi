package com.amor.api.contorller;

import com.amor.api.contorller.dto.ProductDTO;
import com.amor.api.contorller.dto.ProductInsertDTO;
import com.amor.api.contorller.dto.ProductUpdateDTO;
import com.amor.api.contorller.dto.ResponseDTO;
import com.amor.api.service.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    @GetMapping("/{productNo}")
    public ResponseEntity<ResponseDTO<ProductDTO>> findProductByNo(@PathVariable Long productNo) {
        ProductDTO productByNo = productService.findProductByNo(productNo);
        return ResponseEntity.ok(new ResponseDTO<>(productByNo));
    }

    @PostMapping("/")
    public ResponseEntity<ResponseDTO<Void>> registerProduct(@RequestBody ProductInsertDTO productInsertDTO) {
        productService.registerProduct(productInsertDTO);
        return ResponseEntity.ok(new ResponseDTO<>(null));
    }

    @PutMapping("/{productNo}")
    public ResponseEntity<ResponseDTO<Void>> updateProductByNo(@PathVariable Long productNo, @RequestBody ProductUpdateDTO productUpdateDTO) {
        productService.updateProductByNo(productNo, productUpdateDTO);
        return ResponseEntity.ok(new ResponseDTO<>(null));
    }

    @DeleteMapping("/{productNo}")
    public ResponseEntity<ResponseDTO<Void>> deleteProductByNo(@PathVariable Long productNo) {
        productService.deleteProductByNo(productNo);
        return ResponseEntity.ok(new ResponseDTO<>(null));
    }
}
