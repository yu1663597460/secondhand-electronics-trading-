package com.secondhand.electronics.service;

import com.secondhand.electronics.dto.CategoryDTO;
import com.secondhand.electronics.entity.Category;
import com.secondhand.electronics.exception.BusinessException;
import com.secondhand.electronics.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {
    
    private final CategoryRepository categoryRepository;
    
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }
    
    @Transactional
    public Category createCategory(CategoryDTO dto) {
        if (categoryRepository.existsByName(dto.getName())) {
            throw new BusinessException("分类名称已存在");
        }
        
        Category category = new Category();
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        
        return categoryRepository.save(category);
    }
    
    @Transactional
    public Category updateCategory(Long id, CategoryDTO dto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException("分类不存在", 404));
        
        if (!category.getName().equals(dto.getName()) && 
            categoryRepository.existsByName(dto.getName())) {
            throw new BusinessException("分类名称已存在");
        }
        
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        
        return categoryRepository.save(category);
    }
    
    @Transactional
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new BusinessException("分类不存在", 404);
        }
        categoryRepository.deleteById(id);
    }
}
