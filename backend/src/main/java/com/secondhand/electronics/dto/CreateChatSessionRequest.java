package com.secondhand.electronics.dto;

import lombok.Data;

@Data
public class CreateChatSessionRequest {
    private Long productId;
    private String initialMessage;
}