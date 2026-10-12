package com.secondhand.electronics.repository;

import com.secondhand.electronics.entity.OrderDispute;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderDisputeRepository extends JpaRepository<OrderDispute, Long> {
    
    /**
     * 根据订单ID查询纠纷
     */
    Optional<OrderDispute> findByOrderId(Long orderId);
    
    /**
     * 根据举报人ID查询纠纷列表
     */
    Page<OrderDispute> findByReporterIdOrderByCreatedAtDesc(Long reporterId, Pageable pageable);
    
    /**
     * 根据纠纷状态查询纠纷列表
     */
    Page<OrderDispute> findByStatusOrderByCreatedAtDesc(OrderDispute.DisputeStatus status, Pageable pageable);
    
    /**
     * 根据处理人ID查询已处理的纠纷
     */
    Page<OrderDispute> findByResolvedByOrderByResolvedAtDesc(Long resolvedBy, Pageable pageable);
    
    /**
     * 查询待处理的纠纷数量
     */
    @Query("SELECT COUNT(d) FROM OrderDispute d WHERE d.status IN ('PENDING', 'INVESTIGATING')")
    Long countPendingDisputes();
    
    /**
     * 查询指定时间范围内的纠纷
     */
    @Query("SELECT d FROM OrderDispute d WHERE d.createdAt BETWEEN :startTime AND :endTime ORDER BY d.createdAt DESC")
    Page<OrderDispute> findByCreatedAtBetween(@Param("startTime") LocalDateTime startTime, 
                                            @Param("endTime") LocalDateTime endTime, 
                                            Pageable pageable);
    
    /**
     * 检查订单是否已有纠纷
     */
    boolean existsByOrderId(Long orderId);
}