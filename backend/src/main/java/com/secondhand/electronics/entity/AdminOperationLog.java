package com.secondhand.electronics.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Entity
@Table(name = "admin_operation_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminOperationLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "admin_id", nullable = false)
    private Long adminId;
    
    @Column(name = "operation_type", nullable = false, length = 50)
    private String operationType;
    
    @Column(name = "target_type", nullable = false, length = 50)
    private String targetType;
    
    @Column(name = "target_id")
    private Long targetId;
    
    @Column(name = "operation_details", columnDefinition = "TEXT")
    private String operationDetails;
    
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    
    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", insertable = false, updatable = false)
    private User admin;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}