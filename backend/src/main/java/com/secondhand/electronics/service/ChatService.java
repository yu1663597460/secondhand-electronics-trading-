package com.secondhand.electronics.service;

import com.secondhand.electronics.dto.ChatSessionDTO;
import com.secondhand.electronics.dto.CreateChatSessionRequest;
import com.secondhand.electronics.dto.MessageDTO;
import com.secondhand.electronics.entity.ChatSession;
import com.secondhand.electronics.entity.Message;
import com.secondhand.electronics.entity.Product;
import com.secondhand.electronics.entity.User;
import com.secondhand.electronics.repository.ChatSessionRepository;
import com.secondhand.electronics.repository.MessageRepository;
import com.secondhand.electronics.repository.ProductRepository;
import com.secondhand.electronics.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {
    
    private final ChatSessionRepository chatSessionRepository;
    private final MessageRepository messageRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    
    /**
     * 获取用户的聊天会话列表
     */
    public List<ChatSessionDTO> getUserChatSessions(Long userId) {
        List<ChatSession> sessions = chatSessionRepository.findByUserId(userId);
        
        return sessions.stream().map(session -> {
            ChatSessionDTO dto = new ChatSessionDTO();
            dto.setId(session.getId());
            dto.setProductId(session.getProductId());
            dto.setBuyerId(session.getBuyerId());
            dto.setSellerId(session.getSellerId());
            dto.setStatus(session.getStatus().name());
            dto.setCreatedAt(session.getCreatedAt());
            dto.setUpdatedAt(session.getUpdatedAt());
            
            // 获取商品信息
            Product product = productRepository.findById(session.getProductId()).orElse(null);
            if (product != null) {
                dto.setProductTitle(product.getTitle());
                dto.setProductImage(product.getImages() != null && !product.getImages().isEmpty() ? 
                    product.getImages().get(0) : null);
            }
            
            // 获取买家和卖家信息
            User buyer = userRepository.findById(session.getBuyerId()).orElse(null);
            User seller = userRepository.findById(session.getSellerId()).orElse(null);
            
            if (buyer != null) {
                dto.setBuyerName(buyer.getUsername());
                dto.setBuyerAvatar(null); // Avatar not implemented yet
            }
            
            if (seller != null) {
                dto.setSellerName(seller.getUsername());
                dto.setSellerAvatar(null); // Avatar not implemented yet
            }
            
            // 设置对方用户信息
            if (userId.equals(session.getBuyerId())) {
                // 当前用户是买家，对方是卖家
                dto.setOtherUserId(session.getSellerId());
                dto.setOtherUserName(dto.getSellerName());
                dto.setOtherUserAvatar(null);
            } else {
                // 当前用户是卖家，对方是买家
                dto.setOtherUserId(session.getBuyerId());
                dto.setOtherUserName(dto.getBuyerName());
                dto.setOtherUserAvatar(null);
            }
            
            // 获取最后一条消息
            Message lastMessage = messageRepository.findFirstBySessionIdOrderByCreatedAtDesc(session.getId());
            if (lastMessage != null) {
                dto.setLastMessage(lastMessage.getContent());
                dto.setLastMessageTime(lastMessage.getCreatedAt());
            }
            
            // 获取未读消息数量
            Long unreadCount = messageRepository.countUnreadMessages(session.getId(), userId);
            dto.setUnreadCount(unreadCount);
            
            return dto;
        }).collect(Collectors.toList());
    }
    
    /**
     * 获取会话消息
     */
    public Page<MessageDTO> getSessionMessages(Long sessionId, Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Message> messages = messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId, pageable);
        
        return messages.map(message -> {
            MessageDTO dto = new MessageDTO();
            dto.setId(message.getId());
            dto.setSessionId(message.getSessionId());
            dto.setSenderId(message.getSenderId());
            dto.setContent(message.getContent());
            dto.setMessageType(message.getMessageType().name());
            dto.setIsRead(message.getIsRead());
            dto.setCreatedAt(message.getCreatedAt());
            dto.setIsMine(message.getSenderId().equals(userId));
            
            // 获取发送者信息
            User sender = userRepository.findById(message.getSenderId()).orElse(null);
            if (sender != null) {
                dto.setSenderName(sender.getUsername());
                dto.setSenderAvatar(null); // Avatar not implemented yet
            }
            
            return dto;
        });
    }
    
    /**
     * 创建或获取聊天会话
     */
    @Transactional
    public ChatSessionDTO createOrGetChatSession(Long buyerId, CreateChatSessionRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("商品不存在"));
        
        Long sellerId = product.getSeller().getId();
        
        // 检查是否已存在会话
        Optional<ChatSession> existingSession = chatSessionRepository
                .findByProductIdAndBuyerIdAndSellerId(request.getProductId(), buyerId, sellerId);
        
        ChatSession session;
        if (existingSession.isPresent()) {
            session = existingSession.get();
        } else {
            // 创建新会话
            session = new ChatSession();
            session.setProductId(request.getProductId());
            session.setBuyerId(buyerId);
            session.setSellerId(sellerId);
            session.setStatus(ChatSession.SessionStatus.ACTIVE);
            session = chatSessionRepository.save(session);
            
            // 发送初始消息
            if (request.getInitialMessage() != null && !request.getInitialMessage().trim().isEmpty()) {
                Message initialMessage = new Message();
                initialMessage.setSessionId(session.getId());
                initialMessage.setSenderId(buyerId);
                initialMessage.setContent(request.getInitialMessage());
                initialMessage.setMessageType(Message.MessageType.TEXT);
                messageRepository.save(initialMessage);
            }
        }
        
        // 转换为DTO
        return convertToSessionDTO(session, buyerId);
    }
    
    /**
     * 发送消息
     */
    @Transactional
    public MessageDTO sendMessage(Long sessionId, Long senderId, String content, Message.MessageType messageType) {
        // 验证会话存在且用户有权限
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("聊天会话不存在"));
        
        if (!session.getBuyerId().equals(senderId) && !session.getSellerId().equals(senderId)) {
            throw new RuntimeException("无权限发送消息");
        }
        
        // 创建消息
        Message message = new Message();
        message.setSessionId(sessionId);
        message.setSenderId(senderId);
        message.setContent(content);
        message.setMessageType(messageType);
        message = messageRepository.save(message);
        
        // 更新会话的最后更新时间
        session.setUpdatedAt(message.getCreatedAt());
        chatSessionRepository.save(session);
        
        // 转换为DTO
        return convertToMessageDTO(message, senderId);
    }
    
    /**
     * 标记消息为已读
     */
    @Transactional
    public void markMessagesAsRead(Long sessionId, Long userId) {
        messageRepository.markMessagesAsRead(sessionId, userId);
    }
    
    /**
     * 获取用户未读消息总数
     */
    public Long getUnreadMessageCount(Long userId) {
        try {
            Long count = chatSessionRepository.countUnreadMessagesByUserId(userId);
            return count != null ? count : 0L;
        } catch (Exception e) {
            // 如果查询出错，返回0
            return 0L;
        }
    }
    
    /**
     * 删除消息（仅对自己不可见）
     */
    @Transactional
    public void deleteMessage(Long messageId, Long userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("消息不存在"));
        
        // 验证用户是否有权限删除该消息（消息发送者或接收者都可以删除）
        ChatSession session = chatSessionRepository.findById(message.getSessionId())
                .orElseThrow(() -> new RuntimeException("会话不存在"));
        
        if (!session.getBuyerId().equals(userId) && !session.getSellerId().equals(userId)) {
            throw new RuntimeException("无权限删除该消息");
        }
        
        // 直接删除消息（也可以改为软删除，添加一个deletedBy字段）
        messageRepository.delete(message);
    }
    
    private ChatSessionDTO convertToSessionDTO(ChatSession session, Long currentUserId) {
        ChatSessionDTO dto = new ChatSessionDTO();
        dto.setId(session.getId());
        dto.setProductId(session.getProductId());
        dto.setBuyerId(session.getBuyerId());
        dto.setSellerId(session.getSellerId());
        dto.setStatus(session.getStatus().name());
        dto.setCreatedAt(session.getCreatedAt());
        dto.setUpdatedAt(session.getUpdatedAt());
        
        // 获取商品信息
        Product product = productRepository.findById(session.getProductId()).orElse(null);
        if (product != null) {
            dto.setProductTitle(product.getTitle());
            dto.setProductImage(product.getImages() != null && !product.getImages().isEmpty() ? 
                product.getImages().get(0) : null);
        }
        
        // 获取用户信息
        User buyer = userRepository.findById(session.getBuyerId()).orElse(null);
        User seller = userRepository.findById(session.getSellerId()).orElse(null);
        
        if (buyer != null) {
            dto.setBuyerName(buyer.getUsername());
            dto.setBuyerAvatar(null); // Avatar not implemented yet
        }
        
        if (seller != null) {
            dto.setSellerName(seller.getUsername());
            dto.setSellerAvatar(null); // Avatar not implemented yet
        }
        
        // 设置对方用户信息
        if (currentUserId.equals(session.getBuyerId())) {
            dto.setOtherUserId(session.getSellerId());
            dto.setOtherUserName(dto.getSellerName());
            dto.setOtherUserAvatar(dto.getSellerAvatar());
        } else {
            dto.setOtherUserId(session.getBuyerId());
            dto.setOtherUserName(dto.getBuyerName());
            dto.setOtherUserAvatar(dto.getBuyerAvatar());
        }
        
        return dto;
    }
    
    private MessageDTO convertToMessageDTO(Message message, Long currentUserId) {
        MessageDTO dto = new MessageDTO();
        dto.setId(message.getId());
        dto.setSessionId(message.getSessionId());
        dto.setSenderId(message.getSenderId());
        dto.setContent(message.getContent());
        dto.setMessageType(message.getMessageType().name());
        dto.setIsRead(message.getIsRead());
        dto.setCreatedAt(message.getCreatedAt());
        dto.setIsMine(message.getSenderId().equals(currentUserId));
        
        // 获取发送者信息
        User sender = userRepository.findById(message.getSenderId()).orElse(null);
        if (sender != null) {
            dto.setSenderName(sender.getUsername());
            dto.setSenderAvatar(null); // Avatar not implemented yet
        }
        
        return dto;
    }
}