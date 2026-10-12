package com.secondhand.electronics.controller;

import com.secondhand.electronics.dto.ChatSessionDTO;
import com.secondhand.electronics.dto.CreateChatSessionRequest;
import com.secondhand.electronics.dto.MessageDTO;
import com.secondhand.electronics.entity.Message;
import com.secondhand.electronics.service.ChatService;
import com.secondhand.electronics.service.UserService;
import org.springframework.security.core.Authentication;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {
    
    private final ChatService chatService;
    private final UserService userService;
    
    /**
     * 获取用户的聊天会话列表
     */
    @GetMapping("/sessions")
    public ResponseEntity<List<ChatSessionDTO>> getUserChatSessions(Authentication authentication) {
        String username = authentication.getName();
        Long userId = userService.getUserByUsername(username).getId();
        List<ChatSessionDTO> sessions = chatService.getUserChatSessions(userId);
        return ResponseEntity.ok(sessions);
    }
    
    /**
     * 创建或获取聊天会话
     */
    @PostMapping("/sessions")
    public ResponseEntity<ChatSessionDTO> createChatSession(
            Authentication authentication,
            @RequestBody CreateChatSessionRequest request) {
        String username = authentication.getName();
        Long userId = userService.getUserByUsername(username).getId();
        ChatSessionDTO session = chatService.createOrGetChatSession(userId, request);
        return ResponseEntity.ok(session);
    }
    
    /**
     * 获取会话消息
     */
    @GetMapping("/sessions/{sessionId}/messages")
    public ResponseEntity<Page<MessageDTO>> getSessionMessages(
            Authentication authentication,
            @PathVariable Long sessionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        String username = authentication.getName();
        Long userId = userService.getUserByUsername(username).getId();
        Page<MessageDTO> messages = chatService.getSessionMessages(sessionId, userId, page, size);
        return ResponseEntity.ok(messages);
    }
    
    /**
     * 发送消息
     */
    @PostMapping("/sessions/{sessionId}/messages")
    public ResponseEntity<MessageDTO> sendMessage(
            Authentication authentication,
            @PathVariable Long sessionId,
            @RequestBody Map<String, String> request) {
        String username = authentication.getName();
        Long userId = userService.getUserByUsername(username).getId();
        String content = request.get("content");
        String messageType = request.getOrDefault("messageType", "TEXT");
        
        MessageDTO message = chatService.sendMessage(
                sessionId, 
                userId, 
                content, 
                Message.MessageType.valueOf(messageType)
        );
        return ResponseEntity.ok(message);
    }
    
    /**
     * 标记消息为已读
     */
    @PutMapping("/sessions/{sessionId}/read")
    public ResponseEntity<Void> markMessagesAsRead(
            Authentication authentication,
            @PathVariable Long sessionId) {
        String username = authentication.getName();
        Long userId = userService.getUserByUsername(username).getId();
        chatService.markMessagesAsRead(sessionId, userId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 获取未读消息数量
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadMessageCount(Authentication authentication) {
        String username = authentication.getName();
        Long userId = userService.getUserByUsername(username).getId();
        Long count = chatService.getUnreadMessageCount(userId);
        return ResponseEntity.ok(Map.of("count", count));
    }
    
    /**
     * 删除单条消息
     */
    @DeleteMapping("/messages/{messageId}")
    public ResponseEntity<Void> deleteMessage(
            Authentication authentication,
            @PathVariable Long messageId) {
        String username = authentication.getName();
        Long userId = userService.getUserByUsername(username).getId();
        chatService.deleteMessage(messageId, userId);
        return ResponseEntity.ok().build();
    }
}