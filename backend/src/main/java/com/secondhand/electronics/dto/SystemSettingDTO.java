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
public class SystemSettingDTO {
    
    private Long id;
    private String settingKey;
    private String settingValue;
    private String settingType;
    private String description;
    private Long updatedBy;
    private String updatedByUsername;
    private LocalDateTime updatedAt;
    private LocalDateTime createdAt;
}