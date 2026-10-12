package com.secondhand.electronics.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderDTO {
    
    @NotNull(message = "商品ID不能为空")
    private Long productId;
    
    private Long addressId; // 可选

    private Integer carbonPoints; // 可选，使用碳积分抵扣（100积分=1元，最多500积分）
}
