package com.secondhand.electronics.repository;

import com.secondhand.electronics.entity.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {
    
    // 查找用户的所有聊天会话
    @Query("SELECT cs FROM ChatSession cs WHERE cs.buyerId = :userId OR cs.sellerId = :userId ORDER BY cs.updatedAt DESC")
    List<ChatSession> findByUserId(@Param("userId") Long userId);
    
    // 查找特定商品的买家和卖家之间的会话
    Optional<ChatSession> findByProductIdAndBuyerIdAndSellerId(Long productId, Long buyerId, Long sellerId);
    
    // 查找用户作为买家的会话
    List<ChatSession> findByBuyerIdOrderByUpdatedAtDesc(Long buyerId);
    
    // 查找用户作为卖家的会话
    List<ChatSession> findBySellerIdOrderByUpdatedAtDesc(Long sellerId);
    
    // 统计用户的未读消息数量
    @Query("SELECT COUNT(m) FROM Message m JOIN ChatSession cs ON m.sessionId = cs.id " +
           "WHERE (cs.buyerId = :userId OR cs.sellerId = :userId) AND m.senderId != :userId AND m.isRead = false")
    Long countUnreadMessagesByUserId(@Param("userId") Long userId);
}