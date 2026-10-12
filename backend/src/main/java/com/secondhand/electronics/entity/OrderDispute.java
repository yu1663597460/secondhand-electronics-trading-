package com.secondhand.electronics.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_disputes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDispute {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "order_id", nullable = false)
    private Long orderId;
    
    @Column(name = "reporter_id", nullable = false)
    private Long reporterId;
    
    @Column(name = "dispute_type", nullable = false, length = 50)
    private String disputeType;
    
    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "evidence_urls", columnDefinition = "JSON")
    private String evidenceUrls;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private DisputeStatus status = DisputeStatus.PENDING;
    
    @Column(name = "resolution", columnDefinition = "TEXT")
    private String resolution;
    
    @Column(name = "resolved_by")
    private Long resolvedBy;
    
    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", insertable = false, updatable = false)
    private User reporter;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolved_by", insertable = false, updatable = false)
    private User resolver;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum DisputeStatus {
        PENDING,
        INVESTIGATING, 
        RESOLVED,
        CLOSED
    }
}