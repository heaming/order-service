package com.kafka.orderservice.dto;

import com.kafka.orderservice.enums.OrderStatus;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ProductOrderDetailResponse {
    public Long id;
    public Long userId;
    public Long productId;
    public Long count;
    public OrderStatus orderStatus;
    public Long paymentId;
    public Long deliveryId;
    public String paymentStatus;
    public String deliveryStatus;
}
