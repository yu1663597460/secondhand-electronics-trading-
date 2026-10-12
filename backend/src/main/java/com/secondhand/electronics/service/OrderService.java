package com.secondhand.electronics.service;

import com.secondhand.electronics.dto.OrderDTO;
import com.secondhand.electronics.entity.*;
import com.secondhand.electronics.exception.BusinessException;
import com.secondhand.electronics.repository.AddressRepository;
import com.secondhand.electronics.repository.OrderRepository;
import com.secondhand.electronics.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final AddressRepository addressRepository;
    private final CarbonService carbonService;
    
    @Transactional
    public Order createOrder(User buyer, OrderDTO dto) {
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new BusinessException("商品不存在", 404));
        
        if (product.getStatus() != ProductStatus.APPROVED) {
            throw new BusinessException("商品不可购买");
        }
        
        if (product.getSeller().getId().equals(buyer.getId())) {
            throw new BusinessException("不能购买自己的商品");
        }
        
        Order order = new Order();
        String orderNumber = "ORD" + LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + buyer.getId();
        order.setOrderNumber(orderNumber);
        order.setBuyer(buyer);
        order.setSeller(product.getSeller());
        order.setProduct(product);
        order.setStatus(OrderStatus.CREATED);

        // 碳积分抵扣（100积分=1元，最多500积分/5元）
        java.math.BigDecimal finalAmount = product.getPrice();
        if (dto.getCarbonPoints() != null && dto.getCarbonPoints() > 0) {
            int points = Math.min(dto.getCarbonPoints(), 500); // 上限500积分
            points = (points / 100) * 100; // 必须是100的整数倍
            if (points > 0) {
                java.math.BigDecimal discount = java.math.BigDecimal.valueOf(points / 100.0);
                finalAmount = product.getPrice().subtract(discount).max(java.math.BigDecimal.ZERO);
                carbonService.redeemPoints(buyer, points,
                    "购买商品抵扣：" + product.getTitle(), null);
            }
        }
        order.setAmount(finalAmount);

        // 地址可选
        if (dto.getAddressId() != null) {
            Address address = addressRepository.findById(dto.getAddressId())
                    .orElseThrow(() -> new BusinessException("地址不存在", 404));
            if (!address.getUser().getId().equals(buyer.getId())) {
                throw new BusinessException("地址不属于当前用户", 403);
            }
            order.setShippingAddress(address);
        }

        // 创建订单时不改变商品状态，支付后才标记为已售
        return orderRepository.save(order);
    }
    
    @Transactional(readOnly = true)
    public List<Order> getOrdersByUser(User user) {
        List<Order> orders = orderRepository.findByBuyerOrSeller(user, user);
        // 初始化懒加载字段
        orders.forEach(o -> {
            if (o.getProduct() != null) {
                o.getProduct().getId();
                o.getProduct().getImages();
            }
            if (o.getBuyer() != null) o.getBuyer().getId();
            if (o.getSeller() != null) o.getSeller().getId();
        });
        return orders;
    }
    
    @Transactional(readOnly = true)
    public Order getOrderById(Long orderId, User user) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("订单不存在", 404));
        
        if (!order.getBuyer().getId().equals(user.getId()) && 
            !order.getSeller().getId().equals(user.getId())) {
            throw new BusinessException("无权限查看此订单", 403);
        }
        
        // 初始化懒加载字段
        if (order.getProduct() != null) {
            order.getProduct().getId();
            order.getProduct().getImages();
        }
        if (order.getBuyer() != null) order.getBuyer().getId();
        if (order.getSeller() != null) order.getSeller().getId();
        
        return order;
    }
    
    @Transactional
    public void confirmReceipt(Long orderId, User buyer) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("订单不存在", 404));

        if (!order.getBuyer().getId().equals(buyer.getId())) {
            throw new BusinessException("只有买家可以确认收货", 403);
        }

        if (order.getStatus() != OrderStatus.SHIPPED) {
            throw new BusinessException("卖家尚未发货，无法确认收货");
        }

        order.setStatus(OrderStatus.COMPLETED);
        orderRepository.save(order);
        // 交易完成，买卖双方各获碳积分
        String desc = "交易完成：" + order.getProduct().getTitle();
        carbonService.award(buyer, "COMPLETE_ORDER", desc, order.getId());
        carbonService.award(order.getSeller(), "COMPLETE_ORDER", desc, order.getId());
    }

    /**
     * 支付成功后调用：订单状态 CREATED -> PAID，商品标记为已售
     */
    @Transactional
    public void markAsPaid(Long orderId, User buyer) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("订单不存在", 404));

        if (!order.getBuyer().getId().equals(buyer.getId())) {
            throw new BusinessException("无权操作此订单", 403);
        }

        if (order.getStatus() != OrderStatus.CREATED) {
            throw new BusinessException("订单状态异常，无法支付");
        }

        order.setStatus(OrderStatus.PAID);
        // 支付成功后才将商品标记为已售
        Product product = order.getProduct();
        if (product != null) {
            product.setStatus(ProductStatus.SOLD);
            productRepository.save(product);
        }
        orderRepository.save(order);
    }

    /**
     * 卖家发货：订单状态 PAID -> SHIPPED
     */
    @Transactional
    public void shipOrder(Long orderId, User seller) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("订单不存在", 404));

        if (!order.getSeller().getId().equals(seller.getId())) {
            throw new BusinessException("只有卖家可以操作发货", 403);
        }

        if (order.getStatus() != OrderStatus.PAID) {
            throw new BusinessException("订单未支付，无法发货");
        }

        order.setStatus(OrderStatus.SHIPPED);
        orderRepository.save(order);
    }
    
    @Transactional
    public void cancelOrder(Long orderId, User buyer) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("订单不存在", 404));
        
        if (!order.getBuyer().getId().equals(buyer.getId())) {
            throw new BusinessException("只有买家可以取消订单", 403);
        }
        
        if (order.getStatus() == OrderStatus.COMPLETED) {
            throw new BusinessException("已完成的订单不能取消");
        }
        
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new BusinessException("订单已经取消");
        }
        
        order.setStatus(OrderStatus.CANCELLED);
        // 恢复商品状态
        Product product = order.getProduct();
        if (product != null && product.getStatus() == ProductStatus.SOLD) {
            product.setStatus(ProductStatus.APPROVED);
            productRepository.save(product);
        }
        orderRepository.save(order);
    }
}
