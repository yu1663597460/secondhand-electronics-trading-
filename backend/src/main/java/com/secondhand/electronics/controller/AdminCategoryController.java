package com.secondhand.electronics.controller;

import com.secondhand.electronics.dto.CategoryDTO;
import com.secondhand.electronics.dto.CategoryResponseDTO;
import com.secondhand.electronics.entity.Category;
import com.secondhand.electronics.repository.ProductRepository;
import com.secondhand.electronics.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminCategoryController {
    
    private final CategoryService categoryService;
    private final ProductRepository productRepository;
    
    @GetMapping
    public ResponseEntity<List<CategoryResponseDTO>> getAllCategories() {
        List<Category> categories = categoryService.getAllCategories();
        List<CategoryResponseDTO> result = categories.stream().map(c -> {
            long count = productRepository.countByCategoryId(c.getId());
            return new CategoryResponseDTO(c.getId(), c.getName(), c.getDescription(), c.getCreatedAt(), count);
        }).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }
    
    @PostMapping
    public ResponseEntity<CategoryResponseDTO> createCategory(@Valid @RequestBody CategoryDTO dto) {
        Category category = categoryService.createCategory(dto);
        return ResponseEntity.ok(new CategoryResponseDTO(category.getId(), category.getName(), category.getDescription(), category.getCreatedAt(), 0));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponseDTO> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryDTO dto) {
        Category category = categoryService.updateCategory(id, dto);
        long count = productRepository.countByCategoryId(category.getId());
        return ResponseEntity.ok(new CategoryResponseDTO(category.getId(), category.getName(), category.getDescription(), category.getCreatedAt(), count));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok("分类删除成功");
    }
}
