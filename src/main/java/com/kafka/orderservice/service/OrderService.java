package com.kafka.orderservice.service;

import com.kafka.orderservice.dto.*;
import com.kafka.orderservice.entity.ProductOrder;
import com.kafka.orderservice.enums.OrderStatus;
import com.kafka.orderservice.feign.CatalogClient;
import com.kafka.orderservice.feign.DeliveryClient;
import com.kafka.orderservice.feign.PaymentClient;
import com.kafka.orderservice.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    PaymentClient paymentClient;

    @Autowired
    DeliveryClient deliveryClient;

    @Autowired
    CatalogClient catalogClient;

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

        // 2. 결제
        // 2. 결제 수단 조회
//        var paymentMethod = paymentClient.getPaymentMethod(order.userId);
        var processPaymentDto = new ProcessPaymentDto();
        processPaymentDto.orderId = order.id;
        processPaymentDto.userId = order.userId;
        processPaymentDto.amountKRW = Long.parseLong(product.get("price").toString()) * order.count;
        processPaymentDto.paymentMethodId = paymentMethodId;
//        processPaymentDto.paymentMethodType = paymentMethod.get("")

        var payment = paymentClient.processPayment(processPaymentDto);

        // 3. 배송 요청
        var address = deliveryClient.getAddress(addressId);
        var processDeliveryDto = new ProcessDeliveryDto();
        processDeliveryDto.orderId = order.id;
        processDeliveryDto.productName = product.get("name").toString();
        processDeliveryDto.productCount = order.count;
        processDeliveryDto.address = address.get("address").toString();

        var delivery = deliveryClient.processDelivery(processDeliveryDto);

        // 4. 상품 재고 감소
        var decreaseStockCountDto = new DecreaseStockCountDto();
        decreaseStockCountDto.decreaseCount = order.count;
        catalogClient.decreaseStockCount(order.productId, decreaseStockCountDto);

        // 5. 주문 정보 업데이트
        order.paymentId = Long.parseLong(payment.get("id").toString());
        order.deliveryId = Long.parseLong(delivery.get("id").toString());
        order.orderStatus = OrderStatus.DELIVERY_REQUESTED;

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
