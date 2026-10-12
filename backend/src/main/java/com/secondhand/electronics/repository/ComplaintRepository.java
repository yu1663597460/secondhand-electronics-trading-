package com.secondhand.electronics.repository;

import com.secondhand.electronics.entity.Complaint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {
    Page<Complaint> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);
    Page<Complaint> findAllByOrderByCreatedAtDesc(Pageable pageable);
    Optional<Complaint> findByOrderId(Long orderId);
}
