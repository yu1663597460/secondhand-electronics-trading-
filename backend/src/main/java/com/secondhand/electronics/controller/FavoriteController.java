package com.secondhand.electronics.controller;

import com.secondhand.electronics.dto.FavoriteDTO;
import com.secondhand.electronics.entity.User;
import com.secondhand.electronics.repository.UserRepository;
import com.secondhand.electronics.security.JwtUtil;
import com.secondhand.electronics.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/favorites")
@CrossOrigin(origins = "*")
public class FavoriteController {
    
    @Autowired
    private FavoriteService favoriteService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private UserRepository userRepository;
    
    private Long getUserIdFromToken(String token) {
        String username = jwtUtil.extractUsername(token);
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        return user.getId();
    }
    
    /**
     * 添加收藏
     */
    @PostMapping
    public ResponseEntity<?> addFavorite(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, Long> request) {
        try {
            Long userId = getUserIdFromToken(token.substring(7));
            Long productId = request.get("productId");
            
            if (productId == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("message", "商品ID不能为空"));
            }
            
            FavoriteDTO favorite = favoriteService.addFavorite(userId, productId);
            
            return ResponseEntity.ok(Map.of(
                "message", "收藏成功",
                "data", favorite
            ));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "收藏失败: " + e.getMessage()));
        }
    }
    
    /**
     * 取消收藏
     */
    @DeleteMapping("/{productId}")
    public ResponseEntity<?> removeFavorite(
            @RequestHeader("Authorization") String token,
            @PathVariable Long productId) {
        try {
            Long userId = getUserIdFromToken(token.substring(7));
            
            favoriteService.removeFavorite(userId, productId);
            
            return ResponseEntity.ok(Map.of("message", "取消收藏成功"));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "取消收藏失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取收藏列表
     */
    @GetMapping
    public ResponseEntity<?> getFavorites(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Long userId = getUserIdFromToken(token.substring(7));
            
            Page<FavoriteDTO> favorites = favoriteService.getUserFavorites(userId, page, size);
            
            Map<String, Object> response = new HashMap<>();
            response.put("content", favorites.getContent());
            response.put("totalElements", favorites.getTotalElements());
            response.put("totalPages", favorites.getTotalPages());
            response.put("currentPage", favorites.getNumber());
            response.put("size", favorites.getSize());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "获取收藏列表失败: " + e.getMessage()));
        }
    }
    
    /**
     * 检查是否已收藏
     */
    @GetMapping("/check/{productId}")
    public ResponseEntity<?> isFavorited(
            @RequestHeader("Authorization") String token,
            @PathVariable Long productId) {
        try {
            Long userId = getUserIdFromToken(token.substring(7));
            
            boolean isFavorited = favoriteService.isFavorited(userId, productId);
            
            return ResponseEntity.ok(Map.of("isFavorited", isFavorited));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "检查收藏状态失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取收藏数量
     */
    @GetMapping("/count")
    public ResponseEntity<?> getFavoriteCount(
            @RequestHeader("Authorization") String token) {
        try {
            Long userId = getUserIdFromToken(token.substring(7));
            
            long count = favoriteService.getUserFavoriteCount(userId);
            
            return ResponseEntity.ok(Map.of("count", count));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "获取收藏数量失败: " + e.getMessage()));
        }
    }
}
