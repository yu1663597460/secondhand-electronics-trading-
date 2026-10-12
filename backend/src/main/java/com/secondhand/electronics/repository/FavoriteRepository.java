package com.secondhand.electronics.repository;

import com.secondhand.electronics.entity.Favorite;
import com.secondhand.electronics.entity.Product;
import com.secondhand.electronics.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    
    /**
     * 查找用户对特定商品的收藏记录
     */
    Optional<Favorite> findByUserAndProduct(User user, Product product);
    
    /**
     * 检查用户是否已收藏某商品
     */
    boolean existsByUserIdAndProductId(Long userId, Long productId);
    
    /**
     * 获取用户的收藏列表（分页）
     * 使用JOIN FETCH避免N+1问题
     */
    @Query("SELECT f FROM Favorite f " +
           "JOIN FETCH f.product p " +
           "JOIN FETCH p.seller " +
           "WHERE f.user.id = :userId " +
           "ORDER BY f.createdAt DESC")
    Page<Favorite> findByUserIdWithProduct(@Param("userId") Long userId, Pageable pageable);
    
    /**
     * 删除用户对特定商品的收藏
     */
    void deleteByUserIdAndProductId(Long userId, Long productId);
    
    /**
     * 统计用户的收藏数量
     */
    long countByUserId(Long userId);
    
    /**
     * 统计商品被收藏的次数
     */
    long countByProductId(Long productId);
}
