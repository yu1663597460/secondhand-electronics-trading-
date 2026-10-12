package com.secondhand.electronics.entity;

public enum OrderStatus {
    CREATED,    // 已创建（待支付）
    CONFIRMED,  // 兼容旧数据（等同于 PAID）
    PAID,       // 已支付（待发货）
    SHIPPED,    // 已发货（待收货）
    COMPLETED,  // 已完成（确认收货）
    CANCELLED   // 已取消
}
