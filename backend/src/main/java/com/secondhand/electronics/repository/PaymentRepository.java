package com.secondhand.electronics.repository;

import com.secondhand.electronics.entity.Payment;
import com.secondhand.electronics.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    Optional<Payment> findByPaymentNumber(String paymentNumber);
    
    Optional<Payment> findByOrderId(Long orderId);
    
    boolean existsByOrderId(Long orderId);
    
    long countByStatus(PaymentStatus status);
}
