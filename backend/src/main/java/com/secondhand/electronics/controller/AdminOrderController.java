package com.secondhand.electronics.controller;

import com.secondhand.electronics.entity.Order;
import com.secondhand.electronics.repository.ComplaintRepository;
import com.secondhand.electronics.repository.ReviewRepository;
import com.secondhand.electronics.service.AdminOrderService;
import com.secondhand.electronics.service.ComplaintService;
import com.secondhand.electronics.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {
    
    private final AdminOrderService adminOrderService;
    private final ComplaintRepository complaintRepository;
    private final ReviewRepository reviewRepository;
    private final ComplaintService complaintService;
    private final ReviewService reviewService;
    
    @GetMapping
    public ResponseEntity<Page<Order>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        Page<Order> orders = adminOrderService.getAllOrders(page, size, keyword, status);
        return ResponseEntity.ok(orders);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        Order order = adminOrderService.getOrderById(id);
        return ResponseEntity.ok(order);
    }
    
    @PutMapping("/{id}/note")
    public ResponseEntity<String> addOrderNote(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        String note = request.get("note");
        adminOrderService.addOrderNote(id, note);
        return ResponseEntity.ok("订单备注添加成功");
    }

    /** 查询订单关联的投诉 */
    @GetMapping("/{id}/complaint")
    public ResponseEntity<?> getOrderComplaint(@PathVariable Long id) {
        return ResponseEntity.ok(complaintRepository.findByOrderId(id).orElse(null));
    }

    /** 处理订单投诉 */
    @PutMapping("/{orderId}/complaint/{complaintId}/handle")
    public ResponseEntity<String> handleOrderComplaint(
            @PathVariable Long orderId,
            @PathVariable Long complaintId,
            @RequestBody Map<String, String> body) {
        complaintService.handleComplaint(complaintId, body.get("status"), body.getOrDefault("resolution", ""));
        return ResponseEntity.ok("处理成功");
    }

    /** 查询订单关联的评价 */
    @GetMapping("/{id}/review")
    public ResponseEntity<?> getOrderReview(@PathVariable Long id) {
        return ResponseEntity.ok(reviewRepository.findByOrderId(id).orElse(null));
    }

    /** 删除订单评价（恶评） */
    @DeleteMapping("/{orderId}/review/{reviewId}")
    public ResponseEntity<String> deleteOrderReview(
            @PathVariable Long orderId,
            @PathVariable Long reviewId) {
        reviewService.deleteReview(reviewId);
        return ResponseEntity.ok("评价已删除");
    }
}
