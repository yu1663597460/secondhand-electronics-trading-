package com.secondhand.electronics.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin/settings")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminSettingsController {
    
    @GetMapping
    public ResponseEntity<Map<String, Object>> getSystemSettings() {
        Map<String, Object> settings = new HashMap<>();
        settings.put("siteName", "二手电子产品交易平台");
        settings.put("siteDescription", "安全可靠的二手电子产品交易平台");
        settings.put("productReviewEnabled", true);
        settings.put("userVerificationRequired", false);
        settings.put("maxProductImages", 5);
        settings.put("maxProductPrice", 999999.99);
        settings.put("orderAutoConfirmDays", 7);
        settings.put("maintenanceMode", false);
        
        return ResponseEntity.ok(settings);
    }
    
    @PutMapping
    public ResponseEntity<String> updateSystemSettings(@RequestBody Map<String, Object> settings) {
        // 在实际应用中，这里应该保存到数据库
        // 目前只是返回成功消息
        return ResponseEntity.ok("系统设置更新成功");
    }
}
