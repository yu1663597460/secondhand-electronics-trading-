package com.secondhand.electronics.service;

import com.secondhand.electronics.entity.*;
import com.secondhand.electronics.exception.ResourceNotFoundException;
import com.secondhand.electronics.repository.OrderRepository;
import com.secondhand.electronics.repository.PaymentRepository;
import com.secondhand.electronics.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PaymentService {
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    /**
     * 创建支付
     */
    @Transactional
    public Payment createPayment(Long orderId, PaymentMethod paymentMethod) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("订单不存在"));
        
        if (paymentRepository.existsByOrderId(orderId)) {
            throw new IllegalStateException("该订单已创建支付");
        }
        
        if (order.getStatus() != OrderStatus.CREATED) {
            throw new IllegalStateException("订单状态不正确");
        }
        
        Payment payment = new Payment(order, order.getAmount(), paymentMethod);
        return paymentRepository.save(payment);
    }
    
    /**
     * 模拟支付处理
     */
    @Transactional
    public Payment processPayment(String paymentNumber) {
        Payment payment = paymentRepository.findByPaymentNumber(paymentNumber)
            .orElseThrow(() -> new ResourceNotFoundException("支付记录不存在"));
        
        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new IllegalStateException("支付状态不正确");
        }
        
        // 模拟支付处理
        payment.setStatus(PaymentStatus.PROCESSING);
        paymentRepository.save(payment);
        
        // 模拟支付成功
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setTransactionId(UUID.randomUUID().toString());
        payment.setPaidAt(LocalDateTime.now());
        
        // 更新订单状态为已支付
        Order order = payment.getOrder();
        order.setStatus(OrderStatus.PAID);
        orderRepository.save(order);
        
        // 更新商品状态为已售
        Product product = order.getProduct();
        product.setStatus(ProductStatus.SOLD);
        productRepository.save(product);
        
        return paymentRepository.save(payment);
    }
    
    /**
     * 取消支付
     */
    @Transactional
    public Payment cancelPayment(String paymentNumber) {
        Payment payment = paymentRepository.findByPaymentNumber(paymentNumber)
            .orElseThrow(() -> new ResourceNotFoundException("支付记录不存在"));
        
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            throw new IllegalStateException("已支付成功，无法取消");
        }
        
        payment.setStatus(PaymentStatus.CANCELLED);
        
        // 取消订单
        Order order = payment.getOrder();
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        
        return paymentRepository.save(payment);
    }
    
    /**
     * 获取支付信息
     */
    @Transactional(readOnly = true)
    public Payment getPaymentByNumber(String paymentNumber) {
        return paymentRepository.findByPaymentNumber(paymentNumber)
            .orElseThrow(() -> new ResourceNotFoundException("支付记录不存在"));
    }
    
    /**
     * 根据订单ID获取支付信息
     */
    @Transactional(readOnly = true)
    public Payment getPaymentByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("支付记录不存在"));
    }
}
