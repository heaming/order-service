package com.kafka.orderservice.dto;

public class ProcessPaymentDto {
    public Long orderId;
    public Long userId;
    public Long amountKRW;
    public String paymentMethodType;
    public Long paymentMethodId;
}
