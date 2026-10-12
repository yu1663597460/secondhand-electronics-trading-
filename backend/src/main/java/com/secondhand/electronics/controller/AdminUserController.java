package com.secondhand.electronics.controller;

import com.secondhand.electronics.dto.UserDTO;
import com.secondhand.electronics.dto.UserDetailDTO;
import com.secondhand.electronics.dto.UserStatisticsDTO;
import com.secondhand.electronics.entity.AccountStatus;
import com.secondhand.electronics.entity.User;
import com.secondhand.electronics.service.AdminUserService;
import com.secondhand.electronics.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {
    
    private final AdminUserService adminUserService;
    private final UserService userService;
    
    /**
     * 分页查询用户列表
     */
    @GetMapping
    public ResponseEntity<Page<UserDTO>> getUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword) {
        
        Page<UserDTO> users = adminUserService.getUsers(page, size, keyword);
        return ResponseEntity.ok(users);
    }
    
    /**
     * 获取用户详细信息
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserDetailDTO> getUserDetail(@PathVariable Long id) {
        UserDetailDTO userDetail = adminUserService.getUserDetail(id);
        return ResponseEntity.ok(userDetail);
    }
    
    /**
     * 更新用户状态
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<String> updateUserStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        
        String statusStr = (String) request.get("status");
        String reason = (String) request.get("reason");
        
        AccountStatus status = AccountStatus.valueOf(statusStr);
        
        // 获取当前管理员信息
        User admin = userService.getUserByUsername(authentication.getName());
        
        adminUserService.updateUserStatus(id, status, reason, admin.getId());
        
        String message = status == AccountStatus.DISABLED ? "用户已禁用" : "用户已启用";
        return ResponseEntity.ok(message);
    }
    
    /**
     * 搜索用户
     */
    @GetMapping("/search")
    public ResponseEntity<List<UserDTO>> searchUsers(@RequestParam String keyword) {
        List<UserDTO> users = adminUserService.searchUsers(keyword);
        return ResponseEntity.ok(users);
    }
    
    /**
     * 获取用户统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<UserStatisticsDTO> getUserStatistics() {
        UserStatisticsDTO statistics = adminUserService.getUserStatistics();
        return ResponseEntity.ok(statistics);
    }

    /**
     * 审核实名认证（通过/拒绝）
     */
    @PutMapping("/{id}/verification")
    public ResponseEntity<String> reviewVerification(
            @PathVariable Long id,
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        String action = request.get("action"); // APPROVE or REJECT
        String reason = request.get("reason");
        User admin = userService.getUserByUsername(authentication.getName());
        adminUserService.reviewVerification(id, action, reason, admin.getId());
        return ResponseEntity.ok("APPROVE".equals(action) ? "实名认证已通过" : "实名认证已拒绝");
    }

    /**
     * 获取待审核实名认证列表
     */
    @GetMapping("/pending-verification")
    public ResponseEntity<Page<UserDTO>> getPendingVerificationUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<UserDTO> users = adminUserService.getPendingVerificationUsers(page, size);
        return ResponseEntity.ok(users);
    }
}