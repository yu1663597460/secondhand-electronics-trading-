package com.secondhand.electronics.repository;

import com.secondhand.electronics.entity.AccountStatus;
import com.secondhand.electronics.entity.User;
import com.secondhand.electronics.entity.VerificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    // 管理员功能相关查询方法
    
    /**
     * 根据关键词搜索用户（用户名、邮箱、手机号）
     */
    @Query("SELECT u FROM User u WHERE u.username LIKE %:keyword% OR u.email LIKE %:keyword% OR u.phone LIKE %:keyword%")
    Page<User> findByUsernameContainingOrEmailContainingOrPhoneContaining(
            @Param("keyword") String keyword1, 
            @Param("keyword") String keyword2, 
            @Param("keyword") String keyword3, 
            Pageable pageable);
    
    /**
     * 根据关键词搜索用户（不分页）
     */
    @Query("SELECT u FROM User u WHERE u.username LIKE %:keyword% OR u.email LIKE %:keyword% OR u.phone LIKE %:keyword%")
    List<User> findByUsernameContainingOrEmailContainingOrPhoneContaining(
            @Param("keyword") String keyword1, 
            @Param("keyword") String keyword2, 
            @Param("keyword") String keyword3);
    
    /**
     * 根据账户状态统计用户数量
     */
    Long countByStatus(AccountStatus status);
    
    /**
     * 统计指定时间之后创建的用户数量
     */
    Long countByCreatedAtAfter(LocalDateTime createdAt);
    
    /**
     * 根据状态查询用户列表
     */
    Page<User> findByStatusOrderByCreatedAtDesc(AccountStatus status, Pageable pageable);

    /**
     * 根据实名认证状态查询用户列表
     */
    Page<User> findByVerificationStatus(VerificationStatus verificationStatus, Pageable pageable);
}
