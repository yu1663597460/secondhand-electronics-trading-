package com.secondhand.electronics.controller;

import com.secondhand.electronics.dto.OrderDTO;
import com.secondhand.electronics.entity.Order;
import com.secondhand.electronics.entity.User;
import com.secondhand.electronics.service.OrderService;
import com.secondhand.electronics.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    
    private final OrderService orderService;
    private final UserService userService;
    
    @PostMapping
    public ResponseEntity<Map<String, Object>> createOrder(
            Authentication authentication,
            @Valid @RequestBody OrderDTO dto) {
        User buyer = userService.getUserByUsername(authentication.getName());
        Order order = orderService.createOrder(buyer, dto);
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("id", order.getId());
        result.put("orderNumber", order.getOrderNumber());
        result.put("amount", order.getAmount());
        result.put("status", order.getStatus());
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/my")
    public ResponseEntity<List<Map<String, Object>>> getMyOrders(Authentication authentication) {
        User user = userService.getUserByUsername(authentication.getName());
        List<Order> orders = orderService.getOrdersByUser(user);
        List<Map<String, Object>> result = orders.stream().map(this::toOrderMap).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getOrderById(
            @PathVariable Long id,
            Authentication authentication) {
        User user = userService.getUserByUsername(authentication.getName());
        Order order = orderService.getOrderById(id, user);
        return ResponseEntity.ok(toOrderMap(order));
    }

    private Map<String, Object> toOrderMap(Order order) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", order.getId());
        map.put("orderNumber", order.getOrderNumber());
        map.put("amount", order.getAmount());
        map.put("status", order.getStatus());
        map.put("createdAt", order.getCreatedAt());
        map.put("updatedAt", order.getUpdatedAt());
        // product info
        if (order.getProduct() != null) {
            Map<String, Object> product = new HashMap<>();
            product.put("id", order.getProduct().getId());
            product.put("title", order.getProduct().getTitle());
            product.put("description", order.getProduct().getDescription());
            product.put("price", order.getProduct().getPrice());
            product.put("images", order.getProduct().getImages());
            map.put("product", product);
        }
        // buyer info
        if (order.getBuyer() != null) {
            Map<String, Object> buyer = new HashMap<>();
            buyer.put("id", order.getBuyer().getId());
            buyer.put("username", order.getBuyer().getUsername());
            map.put("buyer", buyer);
        }
        // seller info
        if (order.getSeller() != null) {
            Map<String, Object> seller = new HashMap<>();
            seller.put("id", order.getSeller().getId());
            seller.put("username", order.getSeller().getUsername());
            map.put("seller", seller);
        }
        return map;
    }
    
    @PutMapping("/{id}/confirm")
    public ResponseEntity<String> confirmReceipt(
            @PathVariable Long id,
            Authentication authentication) {
        User buyer = userService.getUserByUsername(authentication.getName());
        orderService.confirmReceipt(id, buyer);
        return ResponseEntity.ok("确认收货成功");
    }

    @PutMapping("/{id}/pay")
    public ResponseEntity<String> markAsPaid(
            @PathVariable Long id,
            Authentication authentication) {
        User buyer = userService.getUserByUsername(authentication.getName());
        orderService.markAsPaid(id, buyer);
        return ResponseEntity.ok("支付成功");
    }

    @PutMapping("/{id}/ship")
    public ResponseEntity<String> shipOrder(
            @PathVariable Long id,
            Authentication authentication) {
        User seller = userService.getUserByUsername(authentication.getName());
        orderService.shipOrder(id, seller);
        return ResponseEntity.ok("发货成功");
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<String> cancelOrder(
            @PathVariable Long id,
            Authentication authentication) {
        User buyer = userService.getUserByUsername(authentication.getName());
        orderService.cancelOrder(id, buyer);
        return ResponseEntity.ok("订单已取消");
    }
}
