package com.secondhand.electronics.dto;

import com.secondhand.electronics.entity.AccountStatus;
import com.secondhand.electronics.entity.VerificationStatus;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailDTO {
    
    private Long id;
    private String username;
    private String email;
    private String phone;
    private String realName;
    private String idCard;
    private AccountStatus status;
    private VerificationStatus verificationStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 统计信息
    private Integer productCount;
    private Integer orderCount;
    private Integer completedOrderCount;
    
    // 最近活动
    private List<AdminOperationLogDTO> recentOperations;
    private LocalDateTime lastLoginAt;
}