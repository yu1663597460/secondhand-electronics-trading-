
package com.secondhand.electronics.service;

import com.secondhand.electronics.dto.ProductDTO;
import com.secondhand.electronics.entity.*;
import com.secondhand.electronics.exception.BusinessException;
import com.secondhand.electronics.repository.CategoryRepository;
import com.secondhand.electronics.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
    
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final CarbonService carbonService;
    
    @Transactional
    public Product createProduct(User seller, ProductDTO dto) {
        if (dto.getCategoryId() == null) {
            throw new BusinessException("商品分类不能为空");
        }
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new BusinessException("分类不存在", 404));
        
        Product product = new Product();
        product.setTitle(dto.getTitle());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setCategory(category);
        product.setSeller(seller);
        product.setImages(dto.getImages());
        product.setCondition(dto.getCondition());
        product.setStatus(ProductStatus.PENDING);
        
        Product saved = productRepository.save(product);
        // 发布闲置获得碳积分
        carbonService.award(seller, "PUBLISH", "发布闲置商品：" + dto.getTitle(), saved.getId());
        return saved;
    }
    
    @Transactional
    public Product updateProduct(Long productId, ProductDTO dto, User seller) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException("商品不存在", 404));
        
        if (!product.getSeller().getId().equals(seller.getId())) {
            throw new BusinessException("无权限编辑此商品", 403);
        }
        
        if (product.getStatus() == ProductStatus.SOLD) {
            throw new BusinessException("已售商品不能编辑");
        }
        
        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new BusinessException("分类不存在", 404));
            product.setCategory(category);
        }
        
        product.setTitle(dto.getTitle());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setImages(dto.getImages());
        product.setCondition(dto.getCondition());
        product.setStatus(ProductStatus.PENDING);
        
        return productRepository.save(product);
    }
    
    public Product getProductById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException("商品不存在", 404));
    }
    
    public List<Product> getProductsByUser(User user) {
        return productRepository.findBySeller(user);
    }
    
    public Page<Product> searchProducts(String keyword, Long categoryId, String condition,
                                      Double minPrice, Double maxPrice, String sort, int page, int size) {
        Sort sortOrder = buildSortOrder(sort);
        PageRequest pageRequest = PageRequest.of(page, size, sortOrder);

        // 将关键词按空格/标点拆分，最多取前3个词分别模糊匹配，实现多词联合搜索
        if (keyword != null && !keyword.trim().isEmpty()) {
            String[] tokens = keyword.trim().split("[\\s，,、]+");
            String kw1 = tokens.length > 0 ? tokens[0] : null;
            String kw2 = tokens.length > 1 ? tokens[1] : null;
            String kw3 = tokens.length > 2 ? tokens[2] : null;
            return productRepository.findProductsWithMultiKeyword(
                kw1, kw2, kw3, categoryId, condition, minPrice, maxPrice, ProductStatus.APPROVED, pageRequest);
        }

        return productRepository.findProductsWithFilters(
            null, categoryId, condition, minPrice, maxPrice, ProductStatus.APPROVED, pageRequest);
    }
    
    private Sort buildSortOrder(String sort) {
        if (sort == null) {
            return Sort.by("createdAt").descending(); // 默认按创建时间倒序
        }
        
        return switch (sort) {
            case "price_asc" -> Sort.by("price").ascending();
            case "price_desc" -> Sort.by("price").descending();
            case "newest" -> Sort.by("createdAt").descending();
            case "popular" -> Sort.by("id").descending(); // 暂时用ID代替浏览量
            default -> Sort.by("createdAt").descending();
        };
    }
    
    @Transactional
    public void updateProductStatus(Long productId, ProductStatus status, User seller) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException("商品不存在", 404));
        
        if (!product.getSeller().getId().equals(seller.getId())) {
            throw new BusinessException("无权限操作此商品", 403);
        }
        
        product.setStatus(status);
        productRepository.save(product);
    }
    
    @Transactional
    public void deleteProduct(Long productId, User seller) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException("商品不存在", 404));
        
        if (!product.getSeller().getId().equals(seller.getId())) {
            throw new BusinessException("无权限删除此商品", 403);
        }
        
        if (product.getStatus() == ProductStatus.SOLD) {
            throw new BusinessException("已售出的商品不能删除");
        }
        
        productRepository.delete(product);
    }
}
