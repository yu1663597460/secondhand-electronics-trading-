package com.secondhand.electronics.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatSessionDTO {
    private Long id;
    private Long productId;
    private String productTitle;
    private String productImage;
    private Long buyerId;
    private String buyerName;
    private String buyerAvatar;
    private Long sellerId;
    private String sellerName;
    private String sellerAvatar;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 最后一条消息信息
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private Long unreadCount;
    
    // 对方用户信息（相对于当前用户）
    private Long otherUserId;
    private String otherUserName;
    private String otherUserAvatar;
}