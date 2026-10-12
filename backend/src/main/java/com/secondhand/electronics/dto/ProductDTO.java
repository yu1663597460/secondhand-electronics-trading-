package com.secondhand.electronics.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductDTO {
    
    @NotBlank(message = "商品标题不能为空")
    private String title;
    
    @NotBlank(message = "商品描述不能为空")
    private String description;
    
    @NotNull(message = "商品价格不能为空")
    @DecimalMin(value = "0.01", message = "商品价格必须为正数")
    private BigDecimal price;
    
    private Long categoryId;
    
    private String condition; // 商品成色
    
    private List<String> images;
}
