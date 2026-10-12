package com.secondhand.electronics.controller;

import com.secondhand.electronics.dto.PaymentDTO;
import com.secondhand.electronics.entity.Payment;
import com.secondhand.electronics.entity.PaymentMethod;
import com.secondhand.electronics.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/payments")
@CrossOrigin(origins = "*")
public class PaymentController {
    
    @Autowired
    private PaymentService paymentService;
    
    /**
     * 创建支付
     */
    @PostMapping
    public ResponseEntity<?> createPayment(
            @RequestParam Long orderId,
            @RequestParam String paymentMethod) {
        try {
            PaymentMethod method = PaymentMethod.valueOf(paymentMethod.toUpperCase());
            Payment payment = paymentService.createPayment(orderId, method);
            return ResponseEntity.ok(new PaymentDTO(payment));
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "不支持的支付方式");
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * 处理支付（模拟支付）
     */
    @PostMapping("/{paymentNumber}/process")
    public ResponseEntity<?> processPayment(@PathVariable String paymentNumber) {
        try {
            Payment payment = paymentService.processPayment(paymentNumber);
            return ResponseEntity.ok(new PaymentDTO(payment));
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * 取消支付
     */
    @PostMapping("/{paymentNumber}/cancel")
    public ResponseEntity<?> cancelPayment(@PathVariable String paymentNumber) {
        try {
            Payment payment = paymentService.cancelPayment(paymentNumber);
            return ResponseEntity.ok(new PaymentDTO(payment));
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * 获取支付信息
     */
    @GetMapping("/{paymentNumber}")
    public ResponseEntity<?> getPayment(@PathVariable String paymentNumber) {
        try {
            Payment payment = paymentService.getPaymentByNumber(paymentNumber);
            return ResponseEntity.ok(new PaymentDTO(payment));
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "支付记录不存在");
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 根据订单ID获取支付信息
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<?> getPaymentByOrderId(@PathVariable Long orderId) {
        try {
            Payment payment = paymentService.getPaymentByOrderId(orderId);
            return ResponseEntity.ok(new PaymentDTO(payment));
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "支付记录不存在");
            return ResponseEntity.notFound().build();
        }
    }
}
