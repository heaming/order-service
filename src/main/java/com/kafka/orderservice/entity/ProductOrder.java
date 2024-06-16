package com.kafka.orderservice.entity;

import com.kafka.orderservice.enums.OrderStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
public class ProductOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public Long userId;
    public Long productId;
    public Long count;
    public OrderStatus orderStatus;
    public Long paymentId;
    public Long deliveryId;
    public String deliveryAddress;

    public ProductOrder(Long userId, Long productId, Long count, OrderStatus orderStatus, Long paymentId, Long deliveryId, String deliveryAddress) {
        this.userId = userId;
        this.productId = productId;
        this.count = count;
        this.orderStatus = orderStatus;
        this.paymentId = paymentId;
        this.deliveryId = deliveryId;
        this.deliveryAddress = deliveryAddress;
    }
}
