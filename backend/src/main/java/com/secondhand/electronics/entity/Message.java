package com.secondhand.electronics.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "messages")
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(callSuper = false)
public class Message {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "session_id", nullable = false)
    private Long sessionId;
    
    @Column(name = "sender_id", nullable = false)
    private Long senderId;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", length = 20)
    private MessageType messageType = MessageType.TEXT;
    
    @Column(name = "is_read")
    private Boolean isRead = false;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // 关联关系
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", insertable = false, updatable = false)
    private ChatSession chatSession;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", insertable = false, updatable = false)
    private User sender;
    
    public enum MessageType {
        TEXT, IMAGE, SYSTEM
    }
}