package com.secondhand.electronics.repository;

import com.secondhand.electronics.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
    // 获取会话的消息（分页）
    Page<Message> findBySessionIdOrderByCreatedAtAsc(Long sessionId, Pageable pageable);
    
    // 获取会话的最新消息
    List<Message> findBySessionIdOrderByCreatedAtDesc(Long sessionId);
    
    // 获取会话的最后一条消息
    Message findFirstBySessionIdOrderByCreatedAtDesc(Long sessionId);
    
    // 标记会话中用户的所有未读消息为已读
    @Modifying
    @Query("UPDATE Message m SET m.isRead = true WHERE m.sessionId = :sessionId AND m.senderId != :userId AND m.isRead = false")
    int markMessagesAsRead(@Param("sessionId") Long sessionId, @Param("userId") Long userId);
    
    // 统计会话中用户的未读消息数量
    @Query("SELECT COUNT(m) FROM Message m WHERE m.sessionId = :sessionId AND m.senderId != :userId AND m.isRead = false")
    Long countUnreadMessages(@Param("sessionId") Long sessionId, @Param("userId") Long userId);
}