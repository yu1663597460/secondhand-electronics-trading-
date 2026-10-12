package com.secondhand.electronics.service;

import com.secondhand.electronics.dto.UserDTO;
import com.secondhand.electronics.dto.UserDetailDTO;
import com.secondhand.electronics.dto.UserStatisticsDTO;
import com.secondhand.electronics.entity.AccountStatus;
import com.secondhand.electronics.entity.User;
import com.secondhand.electronics.entity.VerificationStatus;
import com.secondhand.electronics.exception.BusinessException;
import com.secondhand.electronics.repository.UserRepository;
import com.secondhand.electronics.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminUserService {
    
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final AdminOperationLogService operationLogService;
    
    /**
     * 分页查询用户列表
     */
    public Page<UserDTO> getUsers(int page, int size, String keyword) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        
        Page<User> userPage;
        if (keyword != null && !keyword.trim().isEmpty()) {
            userPage = userRepository.findByUsernameContainingOrEmailContainingOrPhoneContaining(
                keyword, keyword, keyword, pageable);
        } else {
            userPage = userRepository.findAll(pageable);
        }
        
        return userPage.map(this::convertToUserDTO);
    }
    
    /**
     * 获取用户详细信息
     */
    public UserDetailDTO getUserDetail(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在", 404));
        
        UserDetailDTO userDetail = convertToUserDetailDTO(user);
        
        // 获取用户商品数量
        Long productCount = productRepository.countBySellerId(userId);
        userDetail.setProductCount(productCount.intValue());
        
        // 可以在这里添加更多统计信息
        // userDetail.setOrderCount(orderRepository.countByBuyerIdOrSellerId(userId, userId));
        
        return userDetail;
    }
    
    /**
     * 更新用户状态
     */
    @Transactional
    public void updateUserStatus(Long userId, AccountStatus status, String reason, Long adminId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在", 404));
        
        AccountStatus oldStatus = user.getStatus();
        user.setStatus(status);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        
        // 记录操作日志
        try {
            String operationDetails = String.format(
                "{\"oldStatus\":\"%s\",\"newStatus\":\"%s\",\"reason\":\"%s\"}", 
                oldStatus, status, reason != null ? reason : "");
            operationLogService.logOperation(
                adminId, 
                status == AccountStatus.DISABLED ? "DISABLE_USER" : "ENABLE_USER",
                "USER", 
                userId, 
                operationDetails
            );
        } catch (Exception e) {
            // 日志记录失败不影响主流程
        }
    }
    
    /**
     * 搜索用户
     */
    public List<UserDTO> searchUsers(String keyword) {
        List<User> users = userRepository.findByUsernameContainingOrEmailContainingOrPhoneContaining(
            keyword, keyword, keyword);
        
        return users.stream()
                .map(this::convertToUserDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 审核实名认证
     */
    @Transactional
    public void reviewVerification(Long userId, String action, String reason, Long adminId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在", 404));

        if ("APPROVE".equals(action)) {
            user.setVerificationStatus(VerificationStatus.VERIFIED);
        } else if ("REJECT".equals(action)) {
            user.setVerificationStatus(VerificationStatus.UNVERIFIED);
        } else {
            throw new BusinessException("无效的审核操作");
        }
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        try {
            operationLogService.logOperation(
                adminId, "REVIEW_VERIFICATION", "USER", userId,
                String.format("{\"action\":\"%s\",\"reason\":\"%s\"}", action, reason != null ? reason : "")
            );
        } catch (Exception e) {
            // 日志记录失败不影响主流程
        }
    }

    /**
     * 获取待审核实名认证用户列表
     */
    public Page<UserDTO> getPendingVerificationUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("updatedAt").descending());
        Page<User> userPage = userRepository.findByVerificationStatus(VerificationStatus.PENDING, pageable);
        return userPage.map(this::convertToUserDTO);
    }

    /**
     * 获取用户统计信息
     */
    public UserStatisticsDTO getUserStatistics() {
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByStatus(AccountStatus.ACTIVE);
        long disabledUsers = userRepository.countByStatus(AccountStatus.DISABLED);
        
        // 获取最近30天新注册用户数
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        long newUsersLast30Days = userRepository.countByCreatedAtAfter(thirtyDaysAgo);
        
        return UserStatisticsDTO.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .disabledUsers(disabledUsers)
                .newUsersLast30Days(newUsersLast30Days)
                .build();
    }
    
    /**
     * 转换为UserDTO
     */
    private UserDTO convertToUserDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .realName(user.getRealName())
                .status(user.getStatus())
                .verificationStatus(user.getVerificationStatus())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
    
    /**
     * 转换为UserDetailDTO
     */
    private UserDetailDTO convertToUserDetailDTO(User user) {
        UserDetailDTO userDetail = new UserDetailDTO();
        userDetail.setId(user.getId());
        userDetail.setUsername(user.getUsername());
        userDetail.setEmail(user.getEmail());
        userDetail.setPhone(user.getPhone());
        userDetail.setRealName(user.getRealName());
        userDetail.setIdCard(user.getIdCard());
        userDetail.setStatus(user.getStatus());
        userDetail.setVerificationStatus(user.getVerificationStatus());
        userDetail.setCreatedAt(user.getCreatedAt());
        userDetail.setUpdatedAt(user.getUpdatedAt());
        
        return userDetail;
    }
}