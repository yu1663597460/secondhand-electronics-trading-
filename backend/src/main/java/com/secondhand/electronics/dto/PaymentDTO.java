package com.secondhand.electronics.dto;

import com.secondhand.electronics.entity.Payment;
import com.secondhand.electronics.entity.PaymentMethod;
import com.secondhand.electronics.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentDTO {
    
    private Long id;
    private String paymentNumber;
    private Long orderId;
    private String orderNumber;
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private String transactionId;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
    
    public PaymentDTO() {
    }
    
    public PaymentDTO(Payment payment) {
        this.id = payment.getId();
        this.paymentNumber = payment.getPaymentNumber();
        this.orderId = payment.getOrder().getId();
        this.orderNumber = payment.getOrder().getOrderNumber();
        this.amount = payment.getAmount();
        this.paymentMethod = payment.getPaymentMethod();
        this.status = payment.getStatus();
        this.transactionId = payment.getTransactionId();
        this.paidAt = payment.getPaidAt();
        this.createdAt = payment.getCreatedAt();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getPaymentNumber() {
        return paymentNumber;
    }
    
    public void setPaymentNumber(String paymentNumber) {
        this.paymentNumber = paymentNumber;
    }
    
    public Long getOrderId() {
        return orderId;
    }
    
    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }
    
    public String getOrderNumber() {
        return orderNumber;
    }
    
    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    public PaymentStatus getStatus() {
        return status;
    }
    
    public void setStatus(PaymentStatus status) {
        this.status = status;
    }
    
    public String getTransactionId() {
        return transactionId;
    }
    
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
    
    public LocalDateTime getPaidAt() {
        return paidAt;
    }
    
    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
