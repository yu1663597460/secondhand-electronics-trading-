package com.secondhand.electronics.controller;

import com.secondhand.electronics.entity.Review;
import com.secondhand.electronics.entity.User;
import com.secondhand.electronics.service.ReviewService;
import com.secondhand.electronics.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final UserService userService;

    /** 买家提交评价 */
    @PostMapping
    public ResponseEntity<Review> submitReview(
            Authentication authentication,
            @RequestBody Map<String, Object> body) {
        User buyer = userService.getUserByUsername(authentication.getName());
        Long orderId = Long.valueOf(body.get("orderId").toString());
        int rating = Integer.parseInt(body.get("rating").toString());
        String content = (String) body.getOrDefault("content", "");
        Review review = reviewService.submitReview(orderId, buyer, rating, content);
        return ResponseEntity.ok(review);
    }

    /** 查询商品评价 */
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<Review>> getProductReviews(@PathVariable Long productId) {
        return ResponseEntity.ok(reviewService.getProductReviews(productId));
    }

    /** 查询卖家评价 */
    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<List<Review>> getSellerReviews(@PathVariable Long sellerId) {
        return ResponseEntity.ok(reviewService.getSellerReviews(sellerId));
    }

    /** 查询订单是否已评价 */
    @GetMapping("/order/{orderId}/exists")
    public ResponseEntity<Map<String, Boolean>> hasReviewed(@PathVariable Long orderId) {
        return ResponseEntity.ok(Map.of("reviewed", reviewService.hasReviewed(orderId)));
    }

    /** 管理员分页查询所有评价 */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<Review>> getAllReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(reviewService.getAllReviews(page, size));
    }

    /** 管理员删除恶评 */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity.ok("评价已删除");
    }
}
