package com.secondhand.electronics.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerificationRequest {
    
    @NotBlank(message = "真实姓名不能为空")
    private String realName;
    
    @NotBlank(message = "身份证号不能为空")
    private String idCard;
}
