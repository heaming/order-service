package com.kafka.orderservice.service;

import blackfriday.protobuf.EdaMessage;
import com.kafka.orderservice.dto.DecreaseStockCountDto;
import com.kafka.orderservice.enums.OrderStatus;
import com.kafka.orderservice.feign.CatalogClient;
import com.kafka.orderservice.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EventListener {

    @Autowired
    private KafkaTemplate<String, byte[]> kafkaTemplate;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    CatalogClient catalogClient;

    @KafkaListener(topics = "payment_result")
    public void consumePaymentResult(byte[] message) throws Exception {
        var object = EdaMessage.PaymentResultV1.parseFrom(message);

        log.info("[payment_result] : consumed {}", object);

        // 결제 정보 업데이트
        var order = orderRepository.findById(object.getOrderId()).orElseThrow();
        order.paymentId = object.getPaymentId();
        order.orderStatus = OrderStatus.DELIVERY_REQUESTED;
        orderRepository.save(order);

        // 배송 요청
        var product = catalogClient.getProduct(order.productId);
        var deliveryRequest = EdaMessage.DeliveryRequestV1.newBuilder()
                .setOrderId(order.id)
                .setProductName(product.get("name").toString())
                .setProductCount(order.count)
                .setAddress(order.deliveryAddress)
                .build();

        kafkaTemplate.send("delivery_request", deliveryRequest.toByteArray());
    }

    @KafkaListener(topics = "delivery_status_update")
    public void consumeDeliveryStatusUpdate(byte[] message) throws Exception {
        var object = EdaMessage.DeliveryStatusUpdateV1.parseFrom(message);

        log.info("[delivery_status_update] consumed :  {}", object);

        if(object.getDeliveryStatus().equals("DELIVERY_REQUESTED")) {
            // 상품 재고 감소
            var order = orderRepository.findById(object.getOrderId()).orElseThrow();
            var decreaseStockCountDto = new DecreaseStockCountDto();
            decreaseStockCountDto.decreaseCount = order.count;
            catalogClient.decreaseStockCount(order.productId, decreaseStockCountDto);
        }
    }
}
