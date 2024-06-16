package com.kafka.orderservice;

import com.kafka.orderservice.entity.ProductOrder;
import com.kafka.orderservice.enums.OrderStatus;
import com.kafka.orderservice.feign.CatalogClient;
import com.kafka.orderservice.feign.DeliveryClient;
import com.kafka.orderservice.feign.PaymentClient;
import com.kafka.orderservice.repository.OrderRepository;
import com.kafka.orderservice.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@Import(OrderService.class)
class OrderServiceTest {

    @SpyBean
    OrderRepository orderRepository;

    @MockBean
    PaymentClient paymentClient;

    @MockBean
    DeliveryClient deliveryClient;

    @MockBean
    CatalogClient catalogClient;

    @MockBean
    KafkaTemplate<String, byte[]> kafkaTemplate;

    @Autowired
    OrderService orderService;

    @Test
    void startOrder() {
        // given
        var paymentMethodRes = new HashMap<String, Object>();
        var userAddressRes = new HashMap<String, Object>();
        paymentMethodRes.put("paymentMethodType", "CREDIT_CARD");
        userAddressRes.put("address", "서울시 마포구");

        when(paymentClient.getPaymentMethod(1L)).thenReturn(paymentMethodRes);
        when(deliveryClient.getUserAddress(1L)).thenReturn(userAddressRes);

        // when
        var startOrderResponseDto = orderService.startOrder(1L, 1L, 2L);

        // then
        assertNotNull(startOrderResponseDto.orderId);
        assertEquals(paymentMethodRes, startOrderResponseDto.paymentMethod);
        assertEquals(userAddressRes, startOrderResponseDto.address);

        var order = orderRepository.findById(startOrderResponseDto.orderId);
        assertEquals(OrderStatus.INITATTED, order.get().orderStatus);

    }

    @Test
    void finishOrder() {
        // given
        var orderStarted = new ProductOrder(
                1L,
                1L,
                1L,
                OrderStatus.INITATTED,
                null,
                null,
                null
        );
        orderRepository.save(orderStarted);

        final var address = "경기도 성남시";
        var catalogRes = new HashMap<String, Object>();
        var deliveryRes = new HashMap<String, Object>();
        catalogRes.put("price", "100");
        deliveryRes.put("address", address);

        when(catalogClient.getProduct(orderStarted.productId)).thenReturn(catalogRes);
        when(deliveryClient.getDelivery(1L)).thenReturn(deliveryRes);

        // when
        var response = orderService.finishOrder(orderStarted.id, 1L, 1L);

        // then
        assertEquals(address, response.deliveryAddress);
        verify(kafkaTemplate, times(1)).send(
                eq("payment_request"),
                any(byte[].class)
        );
    }

    @Test
    void getUserOrders() {
    }

    @Test
    void getOrderDetail() {
    }
}