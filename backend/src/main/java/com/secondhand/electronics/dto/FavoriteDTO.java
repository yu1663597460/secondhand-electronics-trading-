package com.secondhand.electronics.dto;

import com.secondhand.electronics.entity.Favorite;
import com.secondhand.electronics.entity.Product;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class FavoriteDTO {
    
    private Long id;
    private Long productId;
    private ProductInfo product;
    private LocalDateTime createdAt;
    
    // Constructors
    public FavoriteDTO() {
    }
    
    public FavoriteDTO(Favorite favorite) {
        this.id = favorite.getId();
        this.productId = favorite.getProduct().getId();
        this.product = new ProductInfo(favorite.getProduct());
        this.createdAt = favorite.getCreatedAt();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getProductId() {
        return productId;
    }
    
    public void setProductId(Long productId) {
        this.productId = productId;
    }
    
    public ProductInfo getProduct() {
        return product;
    }
    
    public void setProduct(ProductInfo product) {
        this.product = product;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    // Inner class for product information
    public static class ProductInfo {
        private Long id;
        private String title;
        private String description;
        private BigDecimal price;
        private String status;
        private String categoryName;
        private String condition;
        private List<String> images;
        private SellerInfo seller;
        
        public ProductInfo(Product product) {
            this.id = product.getId();
            this.title = product.getTitle();
            this.description = product.getDescription();
            this.price = product.getPrice();
            this.status = product.getStatus() != null ? product.getStatus().name() : null;
            this.categoryName = product.getCategory() != null ? product.getCategory().getName() : null;
            this.condition = product.getCondition();
            this.images = product.getImages();
            if (product.getSeller() != null) {
                this.seller = new SellerInfo(product.getSeller().getId(), product.getSeller().getUsername());
            }
        }
        
        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public String getCategoryName() { return categoryName; }
        public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
        
        public String getCondition() { return condition; }
        public void setCondition(String condition) { this.condition = condition; }
        
        public List<String> getImages() { return images; }
        public void setImages(List<String> images) { this.images = images; }
        
        public SellerInfo getSeller() { return seller; }
        public void setSeller(SellerInfo seller) { this.seller = seller; }
    }
    
    // Inner class for seller information
    public static class SellerInfo {
        private Long id;
        private String username;
        
        public SellerInfo(Long id, String username) {
            this.id = id;
            this.username = username;
        }
        
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
    }
}
