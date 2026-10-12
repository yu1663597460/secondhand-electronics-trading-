package com.secondhand.electronics.service;

import com.secondhand.electronics.entity.Order;
import com.secondhand.electronics.entity.OrderStatus;
import com.secondhand.electronics.exception.BusinessException;
import com.secondhand.electronics.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminOrderService {
    
    private final OrderRepository orderRepository;
    
    @Transactional(readOnly = true)
    public Page<Order> getAllOrders(int page, int size) {
        return getAllOrders(page, size, null, null);
    }

    @Transactional(readOnly = true)
    public Page<Order> getAllOrders(int page, int size, String keyword, String status) {
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Specification<Order> spec = (root, query, cb) -> {
            var predicates = cb.conjunction();

            if (keyword != null && !keyword.trim().isEmpty()) {
                var kw = "%" + keyword + "%";
                predicates = cb.and(predicates, cb.or(
                    cb.like(root.get("orderNumber"), kw),
                    cb.like(root.get("buyer").get("username"), kw),
                    cb.like(root.get("seller").get("username"), kw),
                    cb.like(root.get("product").get("title"), kw)
                ));
            }

            if (status != null && !status.trim().isEmpty()) {
                try {
                    predicates = cb.and(predicates,
                        cb.equal(root.get("status"), OrderStatus.valueOf(status)));
                } catch (IllegalArgumentException ignored) {}
            }

            return predicates;
        };

        Page<Order> result = orderRepository.findAll(spec, pageable);
        // 在事务内初始化懒加载集合，避免序列化时报错
        result.getContent().forEach(o -> {
            if (o.getProduct() != null) {
                Hibernate.initialize(o.getProduct().getImages());
            }
        });
        return result;
    }
    
    @Transactional(readOnly = true)
    public Order getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("订单不存在", 404));
        if (order.getProduct() != null) {
            Hibernate.initialize(order.getProduct().getImages());
        }
        return order;
    }
    
    @Transactional
    public void addOrderNote(Long orderId, String note) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("订单不存在", 404));
        
        order.setAdminNote(note);
        orderRepository.save(order);
    }
}
