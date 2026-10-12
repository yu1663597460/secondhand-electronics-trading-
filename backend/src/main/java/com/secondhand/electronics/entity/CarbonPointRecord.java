package com.secondhand.electronics.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "carbon_point_records")
@Data
@NoArgsConstructor
public class CarbonPointRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password"})
    private User user;

    private int points;

    @Column(length = 30, nullable = false)
    private String actionType;

    @Column(length = 200)
    private String description;

    private Long refId;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public CarbonPointRecord(User user, int points, String actionType, String description, Long refId) {
        this.user = user;
        this.points = points;
        this.actionType = actionType;
        this.description = description;
        this.refId = refId;
    }
}
