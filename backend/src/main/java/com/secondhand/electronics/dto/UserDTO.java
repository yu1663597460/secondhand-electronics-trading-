package com.secondhand.electronics.dto;

import com.secondhand.electronics.entity.AccountStatus;
import com.secondhand.electronics.entity.VerificationStatus;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String username;
    private String nickname;
    private String email;
    private String phone;
    private String realName;
    private AccountStatus status;
    private VerificationStatus verificationStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
