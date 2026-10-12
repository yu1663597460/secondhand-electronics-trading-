package com.secondhand.electronics.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MessageDTO {
    private Long id;
    private Long sessionId;
    private Long senderId;
    private String senderName;
    private String senderAvatar;
    private String content;
    private String messageType;
    private Boolean isRead;
    private LocalDateTime createdAt;
    private Boolean isMine; // 是否是当前用户发送的消息
}