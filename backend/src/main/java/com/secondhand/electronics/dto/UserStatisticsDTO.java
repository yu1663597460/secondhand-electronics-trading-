package com.secondhand.electronics.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatisticsDTO {
    
    private Long totalUsers;
    private Long activeUsers;
    private Long disabledUsers;
    private Long newUsersLast30Days;
    private Long verifiedUsers;
    private Long unverifiedUsers;
}