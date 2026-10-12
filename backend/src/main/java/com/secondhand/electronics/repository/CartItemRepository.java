package com.secondhand.electronics.repository;

import com.secondhand.electronics.entity.CartItem;
import com.secondhand.electronics.entity.Product;
import com.secondhand.electronics.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    
    /**
     * 查找用户对特定商品的购物车记录
     */
    Optional<CartItem> findByUserAndProduct(User user, Product product);
    
    /**
     * 检查用户购物车中是否已有某商品
     */
    boolean existsByUserIdAndProductId(Long userId, Long productId);
    
    /**
     * 获取用户的购物车列表
     * 使用JOIN FETCH避免N+1问题
     */
    @Query("SELECT c FROM CartItem c " +
           "JOIN FETCH c.product p " +
           "JOIN FETCH p.seller " +
           "LEFT JOIN FETCH p.category " +
           "WHERE c.user.id = :userId " +
           "ORDER BY c.createdAt DESC")
    List<CartItem> findByUserIdWithProduct(@Param("userId") Long userId);
    
    /**
     * 删除用户购物车中的特定商品
     */
    void deleteByUserIdAndProductId(Long userId, Long productId);
    
    /**
     * 清空用户的购物车
     */
    void deleteByUserId(Long userId);
    
    /**
     * 统计用户购物车中的商品数量
     */
    long countByUserId(Long userId);
    
    /**
     * 根据商品ID列表获取购物车项
     */
    @Query("SELECT c FROM CartItem c " +
           "JOIN FETCH c.product p " +
           "WHERE c.user.id = :userId AND p.id IN :productIds")
    List<CartItem> findByUserIdAndProductIds(@Param("userId") Long userId, 
                                             @Param("productIds") List<Long> productIds);
}
