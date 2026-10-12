package com.secondhand.electronics.repository;

import com.secondhand.electronics.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByProductIdAndDeletedFalseOrderByCreatedAtDesc(Long productId);
    List<Review> findBySellerIdAndDeletedFalseOrderByCreatedAtDesc(Long sellerId);
    Optional<Review> findByOrderId(Long orderId);
    Page<Review> findByDeletedFalse(Pageable pageable);
    Page<Review> findAll(Pageable pageable);
}
