package com.secondhand.electronics.controller;

import com.secondhand.electronics.dto.*;
import com.secondhand.electronics.entity.User;
import com.secondhand.electronics.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Slf4j
public class UserController {
    
    private final UserService userService;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;
    
    @PostMapping("/auth/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = userService.registerUser(request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/auth/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = userService.authenticateUser(request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/users/profile")
    public ResponseEntity<User> getProfile(Authentication authentication) {
        String username = authentication.getName();
        User user = userService.getUserByUsername(username);
        return ResponseEntity.ok(user);
    }
    
    @PutMapping("/users/profile")
    public ResponseEntity<String> updateProfile(
            Authentication authentication,
            @RequestBody UserDTO userDTO) {
        String username = authentication.getName();
        User user = userService.getUserByUsername(username);
        userService.updateUserProfile(user.getId(), userDTO);
        return ResponseEntity.ok("个人信息更新成功");
    }
    
    @PutMapping("/users/password")
    public ResponseEntity<String> changePassword(
            Authentication authentication,
            @RequestBody Map<String, String> request) {
        String username = authentication.getName();
        User user = userService.getUserByUsername(username);
        
        String oldPassword = request.get("oldPassword");
        String newPassword = request.get("newPassword");
        
        userService.changePassword(user.getId(), oldPassword, newPassword);
        return ResponseEntity.ok("密码修改成功");
    }
    
    @PostMapping("/users/verification")
    public ResponseEntity<String> submitVerification(
            Authentication authentication,
            @Valid @RequestBody VerificationRequest request) {
        String username = authentication.getName();
        User user = userService.getUserByUsername(username);
        userService.submitVerification(user.getId(), request);
        return ResponseEntity.ok("实名认证信息已提交");
    }

    @PostMapping("/users/avatar")
    public ResponseEntity<Map<String, String>> uploadAvatar(
            Authentication authentication,
            @RequestParam("file") MultipartFile file) {
        if (file.isEmpty() || !file.getContentType().startsWith("image/")) {
            return ResponseEntity.badRequest().body(Map.of("message", "请上传有效的图片文件"));
        }
        if (file.getSize() > 2 * 1024 * 1024) {
            return ResponseEntity.badRequest().body(Map.of("message", "图片不能超过2MB"));
        }
        try {
            Path uploadPath = Paths.get(uploadDir, "avatars");
            if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);
            String ext = "";
            String orig = file.getOriginalFilename();
            if (orig != null && orig.contains(".")) ext = orig.substring(orig.lastIndexOf("."));
            String filename = UUID.randomUUID() + ext;
            Files.copy(file.getInputStream(), uploadPath.resolve(filename));
            String avatarUrl = "/uploads/avatars/" + filename;
            String username = authentication.getName();
            User user = userService.getUserByUsername(username);
            userService.updateAvatar(user.getId(), avatarUrl);
            return ResponseEntity.ok(Map.of("avatarUrl", avatarUrl));
        } catch (IOException e) {
            log.error("头像上传失败", e);
            return ResponseEntity.internalServerError().body(Map.of("message", "上传失败"));
        }
    }
}
