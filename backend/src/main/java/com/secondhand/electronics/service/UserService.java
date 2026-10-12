package com.secondhand.electronics.service;

import com.secondhand.electronics.dto.*;
import com.secondhand.electronics.entity.AccountStatus;
import com.secondhand.electronics.entity.User;
import com.secondhand.electronics.entity.VerificationStatus;
import com.secondhand.electronics.exception.BusinessException;
import com.secondhand.electronics.repository.UserRepository;
import com.secondhand.electronics.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    
    @jakarta.annotation.PostConstruct
    public void initializeAdminPassword() {
        // 修复admin密码
        userRepository.findByUsername("admin").ifPresent(admin -> {
            String correctHash = passwordEncoder.encode("admin123");
            admin.setPassword(correctHash);
            userRepository.save(admin);
            System.out.println("Admin密码已重置为: admin123");
        });
        
        // 修复testuser密码
        userRepository.findByUsername("testuser").ifPresent(testuser -> {
            String correctHash = passwordEncoder.encode("user123456");
            testuser.setPassword(correctHash);
            userRepository.save(testuser);
            System.out.println("Testuser密码已重置为: user123456");
        });
    }
    
    @Transactional
    public AuthResponse registerUser(RegisterRequest request) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("用户名已存在");
        }
        
        // 检查邮箱是否已存在
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("邮箱已被注册");
        }
        
        // 创建新用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        
        userRepository.save(user);
        
        // 生成JWT令牌
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());
        
        return new AuthResponse(token, user.getUsername(), user.getRole().name());
    }
    
    public AuthResponse authenticateUser(LoginRequest request) {
        // 查找用户
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException("用户名或密码错误", 401));
        
        // 检查账号状态
        if (user.getStatus() == AccountStatus.DISABLED) {
            throw new BusinessException("账号已被禁用", 403);
        }
        
        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误", 401);
        }
        
        // 生成JWT令牌
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());
        
        return new AuthResponse(token, user.getUsername(), user.getRole().name());
    }
    
    @Transactional
    public void updateUserProfile(Long userId, UserDTO userDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在", 404));

        if (userDTO.getNickname() != null) {
            String nick = userDTO.getNickname().trim();
            if (nick.length() < 2 || nick.length() > 20) {
                throw new BusinessException("昵称长度须在2-20个字符之间");
            }
            user.setNickname(nick);
        }
        if (userDTO.getRealName() != null) {
            user.setRealName(userDTO.getRealName());
        }
        if (userDTO.getPhone() != null) {
            user.setPhone(userDTO.getPhone());
        }

        userRepository.save(user);
    }
    
    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在", 404));
        
        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        
        // 验证新密码长度
        if (newPassword.length() < 8 || newPassword.length() > 20) {
            throw new BusinessException("密码长度必须在8-20个字符之间");
        }
        
        // 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
    
    @Transactional
    public void submitVerification(Long userId, VerificationRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在", 404));
        
        user.setRealName(request.getRealName());
        user.setIdCard(request.getIdCard());
        user.setVerificationStatus(VerificationStatus.PENDING);
        
        userRepository.save(user);
    }
    
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在", 404));
    }
    
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户不存在", 404));
    }

    @Transactional
    public void updateAvatar(Long userId, String avatarUrl) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在", 404));
        user.setAvatar(avatarUrl);
        userRepository.save(user);
    }
}
