package com.secondhand.electronics.service;

import com.secondhand.electronics.dto.AdminOperationLogDTO;
import com.secondhand.electronics.entity.AdminOperationLog;
import com.secondhand.electronics.repository.AdminOperationLogRepository;
import com.secondhand.electronics.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminOperationLogService {
    
    private final AdminOperationLogRepository operationLogRepository;
    private final UserRepository userRepository;
    
    /**
     * 记录管理员操作日志
     */
    public void logOperation(Long adminId, String operationType, String targetType, 
                           Long targetId, String operationDetails) {
        
        // 获取请求信息
        String ipAddress = null;
        String userAgent = null;
        
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            ipAddress = getClientIpAddress(request);
            userAgent = request.getHeader("User-Agent");
        }
        
        AdminOperationLog log = AdminOperationLog.builder()
                .adminId(adminId)
                .operationType(operationType)
                .targetType(targetType)
                .targetId(targetId)
                .operationDetails(operationDetails)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .createdAt(LocalDateTime.now())
                .build();
        
        operationLogRepository.save(log);
    }
    
    /**
     * 分页查询操作日志
     */
    public Page<AdminOperationLogDTO> getOperationLogs(int page, int size, Long adminId, String operationType) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        
        Page<AdminOperationLog> logPage;
        if (adminId != null) {
            logPage = operationLogRepository.findByAdminIdOrderByCreatedAtDesc(adminId, pageable);
        } else if (operationType != null) {
            logPage = operationLogRepository.findByOperationTypeOrderByCreatedAtDesc(operationType, pageable);
        } else {
            logPage = operationLogRepository.findAll(pageable);
        }
        
        return logPage.map(this::convertToDTO);
    }
    
    /**
     * 获取指定目标的操作历史
     */
    public List<AdminOperationLogDTO> getTargetOperationHistory(String targetType, Long targetId) {
        List<AdminOperationLog> logs = operationLogRepository
                .findByTargetTypeAndTargetIdOrderByCreatedAtDesc(targetType, targetId);
        
        return logs.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取管理员操作统计
     */
    public Long getAdminOperationCount(Long adminId, LocalDateTime since) {
        return operationLogRepository.countByAdminIdAndCreatedAtAfter(adminId, since);
    }
    
    /**
     * 转换为DTO
     */
    private AdminOperationLogDTO convertToDTO(AdminOperationLog log) {
        String adminUsername = null;
        if (log.getAdmin() != null) {
            adminUsername = log.getAdmin().getUsername();
        } else {
            // 如果关联查询没有加载，手动查询
            userRepository.findById(log.getAdminId())
                    .ifPresent(user -> log.setAdmin(user));
            adminUsername = log.getAdmin() != null ? log.getAdmin().getUsername() : "Unknown";
        }
        
        return AdminOperationLogDTO.builder()
                .id(log.getId())
                .adminId(log.getAdminId())
                .adminUsername(adminUsername)
                .operationType(log.getOperationType())
                .targetType(log.getTargetType())
                .targetId(log.getTargetId())
                .operationDetails(log.getOperationDetails())
                .ipAddress(log.getIpAddress())
                .userAgent(log.getUserAgent())
                .createdAt(log.getCreatedAt())
                .build();
    }
    
    /**
     * 获取客户端真实IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String[] headerNames = {
            "X-Forwarded-For",
            "X-Real-IP", 
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_CLIENT_IP",
            "HTTP_X_FORWARDED_FOR"
        };
        
        for (String headerName : headerNames) {
            String ip = request.getHeader(headerName);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // 多级代理的情况，取第一个IP
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
        }
        
        return request.getRemoteAddr();
    }
}