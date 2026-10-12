package com.secondhand.electronics.controller;

import com.secondhand.electronics.entity.Product;
import com.secondhand.electronics.entity.ProductStatus;
import com.secondhand.electronics.service.AdminProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/products")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminProductController {
    
    private final AdminProductService adminProductService;
    
    @GetMapping
    public ResponseEntity<Page<Product>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) ProductStatus status,
            @RequestParam(required = false) Long categoryId) {
        Page<Product> products = adminProductService.getAllProducts(page, size, keyword, status, categoryId);
        return ResponseEntity.ok(products);
    }
    
    @PutMapping("/{id}/approve")
    public ResponseEntity<String> approveProduct(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        String comment = request.getOrDefault("comment", "审核通过");
        adminProductService.approveProduct(id, comment);
        return ResponseEntity.ok("商品审核通过");
    }
    
    @PutMapping("/{id}/reject")
    public ResponseEntity<String> rejectProduct(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        String reason = request.get("reason");
        adminProductService.rejectProduct(id, reason);
        return ResponseEntity.ok("商品已拒绝");
    }
    
    @PutMapping("/{id}/remove")
    public ResponseEntity<String> removeProduct(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        String reason = request.get("reason");
        adminProductService.removeProduct(id, reason);
        return ResponseEntity.ok("商品已下架");
    }
    
    @GetMapping("/pending-count")
    public ResponseEntity<Long> getPendingProductCount() {
        Long count = adminProductService.getPendingProductCount();
        return ResponseEntity.ok(count);
    }

    @PatchMapping("/{id}/images")
    public ResponseEntity<String> updateProductImages(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        java.util.List<String> images = (java.util.List<String>) request.get("images");
        adminProductService.updateProductImages(id, images != null ? images : new java.util.ArrayList<>());
        return ResponseEntity.ok("图片更新成功");
    }
}
