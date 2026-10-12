package com.secondhand.electronics.service;

import com.secondhand.electronics.dto.CartItemDTO;
import com.secondhand.electronics.entity.CartItem;
import com.secondhand.electronics.entity.Product;
import com.secondhand.electronics.entity.ProductStatus;
import com.secondhand.electronics.entity.User;
import com.secondhand.electronics.exception.ResourceNotFoundException;
import com.secondhand.electronics.repository.CartItemRepository;
import com.secondhand.electronics.repository.ProductRepository;
import com.secondhand.electronics.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {
    
    @Autowired
    private CartItemRepository cartItemRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    /**
     * 添加商品到购物车
     */
    @Transactional
    public CartItemDTO addToCart(Long userId, Long productId) {
        // 验证用户
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
        
        // 验证商品
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("商品不存在"));
        
        // 检查商品状态
        if (product.getStatus() != ProductStatus.APPROVED) {
            throw new IllegalStateException("该商品不可购买");
        }
        
        // 检查是否已在购物车中
        if (cartItemRepository.existsByUserIdAndProductId(userId, productId)) {
            throw new IllegalStateException("该商品已在购物车中");
        }
        
        // 检查是否是自己的商品
        if (product.getSeller().getId().equals(userId)) {
            throw new IllegalStateException("不能购买自己的商品");
        }
        
        // 创建购物车项
        CartItem cartItem = new CartItem(user, product);
        cartItem = cartItemRepository.save(cartItem);
        
        return new CartItemDTO(cartItem);
    }
    
    /**
     * 从购物车移除商品
     */
    @Transactional
    public void removeFromCart(Long userId, Long productId) {
        // 不在购物车中则静默跳过，不抛异常
        cartItemRepository.deleteByUserIdAndProductId(userId, productId);
    }
    
    /**
     * 获取用户的购物车列表
     */
    @Transactional(readOnly = true)
    public List<CartItemDTO> getCartItems(Long userId) {
        // 验证用户
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("用户不存在");
        }
        
        List<CartItem> cartItems = cartItemRepository.findByUserIdWithProduct(userId);
        
        // 过滤掉已售出或下架的商品
        return cartItems.stream()
            .filter(item -> item.getProduct().getStatus() == ProductStatus.APPROVED)
            .map(CartItemDTO::new)
            .collect(Collectors.toList());
    }
    
    /**
     * 清空购物车
     */
    @Transactional
    public void clearCart(Long userId) {
        // 验证用户
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("用户不存在");
        }
        
        cartItemRepository.deleteByUserId(userId);
    }
    
    /**
     * 获取购物车商品数量
     */
    @Transactional(readOnly = true)
    public long getCartItemCount(Long userId) {
        return cartItemRepository.countByUserId(userId);
    }
    
    /**
     * 批量结算购物车（创建订单）
     * 注意：这个方法会在后续实现订单创建逻辑时完善
     */
    @Transactional
    public void checkoutCart(Long userId, List<Long> productIds, Long addressId) {
        // 验证用户
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
        
        // 获取购物车中的指定商品
        List<CartItem> cartItems = cartItemRepository.findByUserIdAndProductIds(userId, productIds);
        
        if (cartItems.isEmpty()) {
            throw new IllegalStateException("购物车中没有选中的商品");
        }
        
        // 验证所有商品都可购买
        for (CartItem item : cartItems) {
            Product product = item.getProduct();
            if (product.getStatus() != ProductStatus.APPROVED) {
                throw new IllegalStateException("商品 \"" + product.getTitle() + "\" 不可购买");
            }
        }
        
        // TODO: 创建订单逻辑将在订单模块实现
        // 这里先抛出提示
        throw new UnsupportedOperationException("批量结算功能将在订单支付模块实现");
    }
}
