package com.secondhand.electronics.service;

import com.secondhand.electronics.entity.*;
import com.secondhand.electronics.exception.BusinessException;
import com.secondhand.electronics.repository.OrderRepository;
import com.secondhand.electronics.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final CarbonService carbonService;

    /** 买家提交评价 */
    @Transactional
    public Review submitReview(Long orderId, User buyer, int rating, String content) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("订单不存在", 404));

        if (!order.getBuyer().getId().equals(buyer.getId())) {
            throw new BusinessException("只有买家可以评价", 403);
        }
        if (order.getStatus() != OrderStatus.COMPLETED) {
            throw new BusinessException("订单未完成，无法评价");
        }
        if (reviewRepository.findByOrderId(orderId).isPresent()) {
            throw new BusinessException("该订单已评价");
        }
        if (rating < 1 || rating > 5) {
            throw new BusinessException("评分须在1-5之间");
        }

        Review review = new Review();
        review.setOrder(order);
        review.setProduct(order.getProduct());
        review.setBuyer(buyer);
        review.setSeller(order.getSeller());
        review.setRating(rating);
        review.setContent(content);
        review.setDeleted(false);
        Review saved = reviewRepository.save(review);
        // 写评价获得碳积分
        carbonService.award(buyer, "REVIEW", "评价商品：" + order.getProduct().getTitle(), order.getId());
        return saved;
    }

    /** 查询商品评价列表 */
    public List<Review> getProductReviews(Long productId) {
        return reviewRepository.findByProductIdAndDeletedFalseOrderByCreatedAtDesc(productId);
    }

    /** 查询卖家收到的评价 */
    public List<Review> getSellerReviews(Long sellerId) {
        return reviewRepository.findBySellerIdAndDeletedFalseOrderByCreatedAtDesc(sellerId);
    }

    /** 管理员分页查询所有评价 */
    @Transactional(readOnly = true)
    public Page<Review> getAllReviews(int page, int size) {
        Page<Review> reviews = reviewRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        // 初始化懒加载字段，避免序列化时报错
        reviews.forEach(r -> {
            if (r.getBuyer() != null) r.getBuyer().getUsername();
            if (r.getSeller() != null) r.getSeller().getUsername();
            if (r.getProduct() != null) r.getProduct().getTitle();
            if (r.getOrder() != null) r.getOrder().getOrderNumber();
        });
        return reviews;
    }

    /** 管理员删除恶评 */
    @Transactional
    public void deleteReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BusinessException("评价不存在", 404));
        review.setDeleted(true);
        reviewRepository.save(review);
    }

    /** 查询某订单是否已评价 */
    public boolean hasReviewed(Long orderId) {
        return reviewRepository.findByOrderId(orderId).isPresent();
    }
}
