package com.secondhand.electronics.repository;

import com.secondhand.electronics.entity.Product;
import com.secondhand.electronics.entity.ProductStatus;
import com.secondhand.electronics.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    
    List<Product> findBySeller(User seller);
    
    Page<Product> findByStatus(ProductStatus status, Pageable pageable);
    
    Page<Product> findByStatusAndCategoryId(ProductStatus status, Long categoryId, Pageable pageable);
    
    // 管理员功能相关查询方法
    Long countBySellerId(Long sellerId);
    
    Long countByStatus(ProductStatus status);

    Long countByCategoryId(Long categoryId);
    
    Page<Product> findBySellerIdOrderByCreatedAtDesc(Long sellerId, Pageable pageable);
    
    // 高级搜索查询方法（模糊匹配标题、描述）
    @Query("SELECT p FROM Product p WHERE " +
           "(:keyword IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
           "(:condition IS NULL OR p.condition = :condition) AND " +
           "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
           "p.status = :status")
    Page<Product> findProductsWithFilters(
        @Param("keyword") String keyword,
        @Param("categoryId") Long categoryId,
        @Param("condition") String condition,
        @Param("minPrice") Double minPrice,
        @Param("maxPrice") Double maxPrice,
        @Param("status") ProductStatus status,
        Pageable pageable
    );

    // 多关键词模糊搜索（将关键词拆分后分别匹配）
    @Query("SELECT p FROM Product p WHERE " +
           "(:kw1 IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :kw1, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :kw1, '%'))) AND " +
           "(:kw2 IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :kw2, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :kw2, '%'))) AND " +
           "(:kw3 IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :kw3, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :kw3, '%'))) AND " +
           "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
           "(:condition IS NULL OR p.condition = :condition) AND " +
           "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
           "p.status = :status")
    Page<Product> findProductsWithMultiKeyword(
        @Param("kw1") String kw1,
        @Param("kw2") String kw2,
        @Param("kw3") String kw3,
        @Param("categoryId") Long categoryId,
        @Param("condition") String condition,
        @Param("minPrice") Double minPrice,
        @Param("maxPrice") Double maxPrice,
        @Param("status") ProductStatus status,
        Pageable pageable
    );
}
