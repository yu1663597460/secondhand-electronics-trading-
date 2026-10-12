package com.secondhand.electronics.controller;

import com.secondhand.electronics.entity.Complaint;
import com.secondhand.electronics.entity.User;
import com.secondhand.electronics.service.ComplaintService;
import com.secondhand.electronics.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/complaints")
@RequiredArgsConstructor
public class ComplaintController {

    private final ComplaintService complaintService;
    private final UserService userService;

    /** 买家提交投诉 */
    @PostMapping
    public ResponseEntity<Complaint> submitComplaint(
            Authentication authentication,
            @RequestBody Map<String, String> body) {
        User complainant = userService.getUserByUsername(authentication.getName());
        Long orderId = Long.valueOf(body.get("orderId"));
        String reason = body.get("reason");
        String description = body.getOrDefault("description", "");
        Complaint complaint = complaintService.submitComplaint(orderId, complainant, reason, description);
        return ResponseEntity.ok(complaint);
    }

    /** 管理员获取投诉列表 */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<Complaint>> getComplaints(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(complaintService.getComplaints(status, page, size));
    }

    /** 管理员处理投诉 */
    @PutMapping("/{id}/handle")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> handleComplaint(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        complaintService.handleComplaint(id, body.get("status"), body.getOrDefault("resolution", ""));
        return ResponseEntity.ok("投诉已处理");
    }
}
