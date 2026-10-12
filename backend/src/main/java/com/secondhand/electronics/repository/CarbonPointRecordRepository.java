package com.secondhand.electronics.repository;

import com.secondhand.electronics.entity.CarbonPointRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface CarbonPointRecordRepository extends JpaRepository<CarbonPointRecord, Long> {
    Page<CarbonPointRecord> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    @Query("SELECT COUNT(r) > 0 FROM CarbonPointRecord r WHERE r.user.id = :userId AND r.actionType = 'DAILY_SIGN' AND r.createdAt >= :startOfDay")
    boolean existsTodaySign(Long userId, LocalDateTime startOfDay);
}
