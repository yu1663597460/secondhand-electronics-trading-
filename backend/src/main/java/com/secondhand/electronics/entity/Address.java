package com.secondhand.electronics.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "addresses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Address {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false, length = 50)
    private String receiverName;
    
    @Column(nullable = false, length = 20)
    private String phone;
    
    @Column(nullable = false, length = 50)
    private String province;
    
    @Column(nullable = false, length = 50)
    private String city;
    
    @Column(nullable = false, length = 50)
    private String district;
    
    @Column(nullable = false, length = 200)
    private String detailAddress;
    
    @Column(nullable = false)
    private Boolean isDefault = false;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
