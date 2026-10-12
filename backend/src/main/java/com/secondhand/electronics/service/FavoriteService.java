package com.secondhand.electronics.service;

import com.secondhand.electronics.dto.FavoriteDTO;
import com.secondhand.electronics.entity.Favorite;
import com.secondhand.electronics.entity.Product;
import com.secondhand.electronics.entity.User;
import com.secondhand.electronics.exception.ResourceNotFoundException;
import com.secondhand.electronics.repository.FavoriteRepository;
import com.secondhand.electronics.repository.ProductRepository;
import com.secondhand.electronics.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FavoriteService {
    
    @Autowired
    private FavoriteRepository favoriteRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    /**
     * 添加收藏
     */
    @Transactional
    public FavoriteDTO addFavorite(Long userId, Long productId) {
        // 验证用户
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
        
        // 验证商品
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("商品不存在"));
        
        // 检查是否已收藏
        if (favoriteRepository.existsByUserIdAndProductId(userId, productId)) {
            throw new IllegalStateException("已经收藏过该商品");
        }
        
        // 创建收藏记录
        Favorite favorite = new Favorite(user, product);
        favorite = favoriteRepository.save(favorite);
        
        return new FavoriteDTO(favorite);
    }
    
    /**
     * 取消收藏
     */
    @Transactional
    public void removeFavorite(Long userId, Long productId) {
        // 验证用户
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("用户不存在");
        }
        
        // 验证商品
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("商品不存在");
        }
        
        // 检查是否已收藏
        if (!favoriteRepository.existsByUserIdAndProductId(userId, productId)) {
            throw new IllegalStateException("未收藏该商品");
        }
        
        // 删除收藏记录
        favoriteRepository.deleteByUserIdAndProductId(userId, productId);
    }
    
    /**
     * 获取用户的收藏列表（分页）
     */
    @Transactional(readOnly = true)
    public Page<FavoriteDTO> getUserFavorites(Long userId, int page, int size) {
        // 验证用户
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("用户不存在");
        }
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Favorite> favorites = favoriteRepository.findByUserIdWithProduct(userId, pageable);
        
        return favorites.map(FavoriteDTO::new);
    }
    
    /**
     * 检查用户是否已收藏某商品
     */
    @Transactional(readOnly = true)
    public boolean isFavorited(Long userId, Long productId) {
        return favoriteRepository.existsByUserIdAndProductId(userId, productId);
    }
    
    /**
     * 获取用户的收藏数量
     */
    @Transactional(readOnly = true)
    public long getUserFavoriteCount(Long userId) {
        return favoriteRepository.countByUserId(userId);
    }
    
    /**
     * 获取商品被收藏的次数
     */
    @Transactional(readOnly = true)
    public long getProductFavoriteCount(Long productId) {
        return favoriteRepository.countByProductId(productId);
    }
}
