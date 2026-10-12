package com.secondhand.electronics.service;

import com.secondhand.electronics.entity.Product;
import com.secondhand.electronics.entity.ProductStatus;
import com.secondhand.electronics.exception.BusinessException;
import com.secondhand.electronics.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminProductService {
    
    private final ProductRepository productRepository;
    
    @Transactional(readOnly = true)
    public Page<Product> getAllProducts(int page, int size, String keyword, ProductStatus status) {
        return getAllProducts(page, size, keyword, status, null);
    }

    @Transactional(readOnly = true)
    public Page<Product> getAllProducts(int page, int size, String keyword, ProductStatus status, Long categoryId) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        
        Specification<Product> spec = (root, query, cb) -> {
            var predicates = cb.conjunction();
            
            if (keyword != null && !keyword.trim().isEmpty()) {
                predicates = cb.and(predicates, 
                    cb.like(root.get("title"), "%" + keyword + "%"));
            }
            
            if (status != null) {
                predicates = cb.and(predicates, 
                    cb.equal(root.get("status"), status));
            }

            if (categoryId != null) {
                predicates = cb.and(predicates,
                    cb.equal(root.get("category").get("id"), categoryId));
            }
            
            return predicates;
        };
        
        Page<Product> result = productRepository.findAll(spec, pageable);
        // 在事务内初始化懒加载的图片集合
        result.getContent().forEach(p -> Hibernate.initialize(p.getImages()));
        return result;
    }    
    @Transactional
    public void approveProduct(Long productId, String comment) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException("商品不存在", 404));
        
        product.setStatus(ProductStatus.APPROVED);
        product.setReviewNote(comment);
        product.setUpdatedAt(LocalDateTime.now());
        productRepository.save(product);
    }
    
    @Transactional
    public void rejectProduct(Long productId, String reason) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException("商品不存在", 404));
        
        product.setStatus(ProductStatus.REJECTED);
        product.setReviewNote(reason);
        product.setUpdatedAt(LocalDateTime.now());
        productRepository.save(product);
    }
    
    @Transactional
    public void removeProduct(Long productId, String reason) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException("商品不存在", 404));
        
        product.setStatus(ProductStatus.TAKEN_DOWN);
        product.setReviewNote(reason);
        product.setUpdatedAt(LocalDateTime.now());
        productRepository.save(product);
    }
    
    public Long getPendingProductCount() {
        return productRepository.countByStatus(ProductStatus.PENDING);
    }

    @Transactional
    public void updateProductImages(Long productId, java.util.List<String> images) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException("商品不存在", 404));
        product.setImages(images);
        product.setUpdatedAt(LocalDateTime.now());
        productRepository.save(product);
    }
}
