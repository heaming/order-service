package com.kafka.orderservice.controller;

import com.kafka.orderservice.dto.FinishOrderDto;
import com.kafka.orderservice.dto.ProductOrderDetailResponse;
import com.kafka.orderservice.dto.StartOrderDto;
import com.kafka.orderservice.dto.StartOrderResponse;
import com.kafka.orderservice.entity.ProductOrder;
import com.kafka.orderservice.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    OrderService orderService;

    @PostMapping("/start-order")
    public StartOrderResponse startOrder(@RequestBody StartOrderDto request) throws Exception {
        return orderService.startOrder(request.userId, request.productId, request.count);
    }

    @PostMapping("/finish-order")
    public ProductOrder finshOrder(@RequestBody FinishOrderDto request) throws Exception {
        return orderService.finishOrder(request.orderId, request.paymentMethodId, request.addressId);
    }

    @GetMapping("/users/{userId}/orders")
    public List<ProductOrder> getUserOrders(@PathVariable Long userId) {
        return orderService.getUserOrders(userId);
    }

    @GetMapping("/order-detail/{orderId}")
    public ProductOrderDetailResponse getOrderDetail(@PathVariable Long orderId) {
        return orderService.getOrderDetail(orderId);
    }
}
