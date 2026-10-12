package com.secondhand.electronics.repository;

import com.secondhand.electronics.entity.AdminOperationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AdminOperationLogRepository extends JpaRepository<AdminOperationLog, Long> {
    
    /**
     * 根据管理员ID查询操作日志
     */
    Page<AdminOperationLog> findByAdminIdOrderByCreatedAtDesc(Long adminId, Pageable pageable);
    
    /**
     * 根据操作类型查询日志
     */
    Page<AdminOperationLog> findByOperationTypeOrderByCreatedAtDesc(String operationType, Pageable pageable);
    
    /**
     * 根据目标类型和目标ID查询日志
     */
    List<AdminOperationLog> findByTargetTypeAndTargetIdOrderByCreatedAtDesc(String targetType, Long targetId);
    
    /**
     * 查询指定时间范围内的操作日志
     */
    @Query("SELECT a FROM AdminOperationLog a WHERE a.createdAt BETWEEN :startTime AND :endTime ORDER BY a.createdAt DESC")
    Page<AdminOperationLog> findByCreatedAtBetween(@Param("startTime") LocalDateTime startTime, 
                                                  @Param("endTime") LocalDateTime endTime, 
                                                  Pageable pageable);
    
    /**
     * 统计指定管理员的操作次数
     */
    @Query("SELECT COUNT(a) FROM AdminOperationLog a WHERE a.adminId = :adminId AND a.createdAt >= :since")
    Long countByAdminIdAndCreatedAtAfter(@Param("adminId") Long adminId, @Param("since") LocalDateTime since);
}