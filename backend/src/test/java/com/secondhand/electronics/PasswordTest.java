package com.secondhand.electronics;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordTest {
    public static void main(String[] args) {
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // 数据库中的哈希值
        String adminHash = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";
        String testuserHash = "$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG";
        
        // 测试密码
        String adminPassword = "admin123";
        String testuserPassword = "user123456";
        
        System.out.println("=== 密码验证测试 ===");
        System.out.println();
        
        System.out.println("Admin密码验证:");
        System.out.println("密码: " + adminPassword);
        System.out.println("哈希: " + adminHash);
        boolean adminMatch = encoder.matches(adminPassword, adminHash);
        System.out.println("匹配结果: " + adminMatch);
        System.out.println();
        
        System.out.println("Testuser密码验证:");
        System.out.println("密码: " + testuserPassword);
        System.out.println("哈希: " + testuserHash);
        boolean testuserMatch = encoder.matches(testuserPassword, testuserHash);
        System.out.println("匹配结果: " + testuserMatch);
        System.out.println();
        
        // 生成新的哈希值
        System.out.println("=== 生成新的哈希值 ===");
        String newAdminHash = encoder.encode(adminPassword);
        String newTestuserHash = encoder.encode(testuserPassword);
        System.out.println("Admin新哈希: " + newAdminHash);
        System.out.println("Testuser新哈希: " + newTestuserHash);
        System.out.println();
        
        // 验证新哈希值
        System.out.println("验证新哈希值:");
        System.out.println("Admin: " + encoder.matches(adminPassword, newAdminHash));
        System.out.println("Testuser: " + encoder.matches(testuserPassword, newTestuserHash));
    }
}
