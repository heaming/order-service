package com.kafka.orderservice.dto;

import java.util.Map;

public class StartOrderResponse {
    public Long orderId;
    public Map<String, Object> paymentMethod;
    public Map<String, Object> address;

}
