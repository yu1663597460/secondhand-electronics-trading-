package com.secondhand.electronics.service;

import com.secondhand.electronics.entity.*;
import com.secondhand.electronics.exception.BusinessException;
import com.secondhand.electronics.repository.ComplaintRepository;
import com.secondhand.electronics.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final OrderRepository orderRepository;

    /** 买家提交投诉 */
    @Transactional
    public Complaint submitComplaint(Long orderId, User complainant, String reason, String description) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("订单不存在", 404));

        if (!order.getBuyer().getId().equals(complainant.getId())) {
            throw new BusinessException("只有买家可以投诉", 403);
        }
        if (order.getStatus() != OrderStatus.COMPLETED) {
            throw new BusinessException("订单未完成，无法投诉");
        }
        if (complaintRepository.findByOrderId(orderId).isPresent()) {
            throw new BusinessException("该订单已提交过投诉");
        }

        Complaint complaint = new Complaint();
        complaint.setOrder(order);
        complaint.setComplainant(complainant);
        complaint.setRespondent(order.getSeller());
        complaint.setReason(reason);
        complaint.setDescription(description);
        complaint.setStatus("PENDING");
        return complaintRepository.save(complaint);
    }

    /** 管理员获取投诉列表 */
    public Page<Complaint> getComplaints(String status, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        if (status != null && !status.isBlank()) {
            return complaintRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
        }
        return complaintRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    /** 管理员处理投诉 */
    @Transactional
    public void handleComplaint(Long complaintId, String status, String resolution) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new BusinessException("投诉不存在", 404));
        complaint.setStatus(status);
        complaint.setResolution(resolution);
        complaintRepository.save(complaint);
    }
}
