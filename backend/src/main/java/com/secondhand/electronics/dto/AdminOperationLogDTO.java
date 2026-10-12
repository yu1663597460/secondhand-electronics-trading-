package com.secondhand.electronics.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminOperationLogDTO {
    
    private Long id;
    private Long adminId;
    private String adminUsername;
    private String operationType;
    private String targetType;
    private Long targetId;
    private String operationDetails;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime createdAt;
}