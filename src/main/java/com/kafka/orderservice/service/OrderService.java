package com.kafka.orderservice.service;

import blackfriday.protobuf.EdaMessage;
import com.kafka.orderservice.dto.*;
import com.kafka.orderservice.entity.ProductOrder;
import com.kafka.orderservice.enums.OrderStatus;
import com.kafka.orderservice.feign.CatalogClient;
import com.kafka.orderservice.feign.DeliveryClient;
import com.kafka.orderservice.feign.PaymentClient;
import com.kafka.orderservice.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class OrderService {

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    PaymentClient paymentClient;

    @Autowired
    DeliveryClient deliveryClient;

    @Autowired
    CatalogClient catalogClient;

    @Autowired
    KafkaTemplate<String, byte[]> kafkaTemplate;

    public StartOrderResponse startOrder(Long userId, Long productId, Long count) {
        // 1. 상품 정보 조회
        var product = catalogClient.getProduct(productId);
        // 2. 결제 수단 조회
        var paymentMethod = paymentClient.getPaymentMethod(userId);
        // 3. 배송지 정보 조회
        var address = deliveryClient.getUserAddress(userId);

        var order = new ProductOrder(
                userId,
                productId,
                count,
                OrderStatus.INITATTED,
                null, // Long.parseLong(paymentMethod.get("id").toString()),
                null,
                null
        );
        orderRepository.save(order);

        var response = new StartOrderResponse();
        response.orderId = order.id;
        response.paymentMethod = paymentMethod;
        response.address = address;

        return response;
    }

    public ProductOrder finishOrder(Long orderId, Long paymentMethodId, Long addressId) {
        var order = orderRepository.findById(orderId).orElseThrow();

        //  1. 상품 정보 조회
        var product = catalogClient.getProduct(order.productId);

        // 2. 결제 요청
        var message = EdaMessage.PaymentRequestV1.newBuilder()
                .setOrderId(order.id)
                .setUserId(order.userId)
                .setAmountKRW(Long.parseLong(product.get("price").toString()) * order.count)
                .setPaymentMethodId(paymentMethodId)
                .build();

        kafkaTemplate.send("payment_request", message.toByteArray());

        // 3. 주문 정보 업데이트
        var address = deliveryClient.getAddress(addressId);
        order.orderStatus = OrderStatus.PAYMENT_REQUESTED;
        order.deliveryAddress = address.get("address").toString();

        return orderRepository.save(order);
    }

    public List<ProductOrder> getUserOrders(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    public ProductOrderDetailResponse getOrderDetail(Long orderId) {
        var order = orderRepository.findById(orderId).orElseThrow();

        var paymentRes = paymentClient.getPayment(order.paymentId);
        var deliveryRes = deliveryClient.getDelivery(order.deliveryId);

        return new ProductOrderDetailResponse(
                order.id,
                order.userId,
                order.productId,
                order.count,
                order.orderStatus,
                order.paymentId,
                order.deliveryId,
                paymentRes.get("paymentStatus").toString(),
                deliveryRes.get("deliveryStatus").toString()
        );
    }

}
