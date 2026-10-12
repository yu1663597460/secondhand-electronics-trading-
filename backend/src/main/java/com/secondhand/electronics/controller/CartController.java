package com.secondhand.electronics.controller;

import com.secondhand.electronics.dto.CartItemDTO;
import com.secondhand.electronics.service.CartService;
import com.secondhand.electronics.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cart")
@CrossOrigin(origins = "*")
public class CartController {
    
    @Autowired
    private CartService cartService;
    
    @Autowired
    private UserService userService;
    
    /**
     * 添加商品到购物车
     */
    @PostMapping("/items")
    public ResponseEntity<?> addToCart(
            Authentication authentication,
            @RequestParam Long productId) {
        try {
            String username = authentication.getName();
            Long userId = userService.getUserByUsername(username).getId();
            CartItemDTO cartItem = cartService.addToCart(userId, productId);
            return ResponseEntity.ok(cartItem);
        } catch (IllegalStateException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "添加到购物车失败");
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * 获取购物车列表
     */
    @GetMapping("/items")
    public ResponseEntity<?> getCartItems(Authentication authentication) {
        try {
            String username = authentication.getName();
            Long userId = userService.getUserByUsername(username).getId();
            List<CartItemDTO> cartItems = cartService.getCartItems(userId);
            return ResponseEntity.ok(cartItems);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "获取购物车失败");
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * 从购物车移除商品
     */
    @DeleteMapping("/items/{productId}")
    public ResponseEntity<?> removeFromCart(
            Authentication authentication,
            @PathVariable Long productId) {
        try {
            String username = authentication.getName();
            Long userId = userService.getUserByUsername(username).getId();
            cartService.removeFromCart(userId, productId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "已从购物车移除");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "移除失败");
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * 清空购物车
     */
    @DeleteMapping("/items")
    public ResponseEntity<?> clearCart(Authentication authentication) {
        try {
            String username = authentication.getName();
            Long userId = userService.getUserByUsername(username).getId();
            cartService.clearCart(userId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "购物车已清空");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "清空购物车失败");
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * 获取购物车商品数量
     */
    @GetMapping("/count")
    public ResponseEntity<?> getCartItemCount(Authentication authentication) {
        try {
            String username = authentication.getName();
            Long userId = userService.getUserByUsername(username).getId();
            long count = cartService.getCartItemCount(userId);
            Map<String, Long> response = new HashMap<>();
            response.put("count", count);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "获取购物车数量失败");
            return ResponseEntity.internalServerError().body(error);
        }
    }
}
