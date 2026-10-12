package com.secondhand.electronics.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/upload")
@Slf4j
public class FileUploadController {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @PostMapping("/image")
    public ResponseEntity<Map<String, Object>> uploadImage(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 验证文件
            if (file.isEmpty()) {
                response.put("success", false);
                response.put("message", "文件不能为空");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 验证文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                response.put("success", false);
                response.put("message", "只能上传图片文件");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 验证文件大小 (2MB)
            if (file.getSize() > 2 * 1024 * 1024) {
                response.put("success", false);
                response.put("message", "文件大小不能超过2MB");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 创建上传目录
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = UUID.randomUUID().toString() + extension;
            
            // 保存文件
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath);
            
            // 返回文件URL
            String fileUrl = "/uploads/" + filename;
            
            Map<String, Object> data = new HashMap<>();
            data.put("url", fileUrl);
            data.put("filename", filename);
            data.put("originalName", originalFilename);
            data.put("size", file.getSize());
            
            response.put("success", true);
            response.put("message", "上传成功");
            response.put("data", data);
            
            log.info("文件上传成功: {}", filename);
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            log.error("文件上传失败", e);
            response.put("success", false);
            response.put("message", "文件上传失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}