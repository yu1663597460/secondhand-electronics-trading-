package com.secondhand.electronics.controller;

import com.secondhand.electronics.dto.ProductDTO;
import com.secondhand.electronics.entity.Product;
import com.secondhand.electronics.entity.ProductStatus;
import com.secondhand.electronics.entity.User;
import com.secondhand.electronics.service.ProductService;
import com.secondhand.electronics.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {
    
    private final ProductService productService;
    private final UserService userService;
    
    @PostMapping
    public ResponseEntity<Product> createProduct(
            Authentication authentication,
            @Valid @RequestBody ProductDTO dto) {
        User seller = userService.getUserByUsername(authentication.getName());
        Product product = productService.createProduct(seller, dto);
        return ResponseEntity.ok(product);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(
            @PathVariable Long id,
            Authentication authentication,
            @Valid @RequestBody ProductDTO dto) {
        User seller = userService.getUserByUsername(authentication.getName());
        Product product = productService.updateProduct(id, dto, seller);
        return ResponseEntity.ok(product);
    }
    
    @GetMapping
    public ResponseEntity<Page<Product>> getProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String condition,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<Product> products = productService.searchProducts(
            keyword, categoryId, condition, minPrice, maxPrice, sort, page, size);
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }
    
    @GetMapping("/my")
    public ResponseEntity<List<Product>> getMyProducts(Authentication authentication) {
        User seller = userService.getUserByUsername(authentication.getName());
        List<Product> products = productService.getProductsByUser(seller);
        return ResponseEntity.ok(products);
    }
    
    @PutMapping("/{id}/status")
    public ResponseEntity<String> updateProductStatus(
            @PathVariable Long id,
            Authentication authentication,
            @RequestBody Map<String, String> request) {
        User seller = userService.getUserByUsername(authentication.getName());
        ProductStatus status = ProductStatus.valueOf(request.get("status"));
        productService.updateProductStatus(id, status, seller);
        return ResponseEntity.ok("商品状态更新成功");
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(
            @PathVariable Long id,
            Authentication authentication) {
        User seller = userService.getUserByUsername(authentication.getName());
        productService.deleteProduct(id, seller);
        return ResponseEntity.ok("商品删除成功");
    }
}
