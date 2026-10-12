package com.secondhand.electronics.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "carbon_accounts")
@Data
@NoArgsConstructor
public class CarbonAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password"})
    private User user;

    private int totalPoints = 0;
    private int availablePoints = 0;

    @Column(length = 20)
    private String level = "SPROUT";

    private int carbonSavedGrams = 0;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public CarbonAccount(User user) {
        this.user = user;
    }
}
