package com.kafka.orderservice.feign;

import com.kafka.orderservice.dto.DecreaseStockCountDto;
import com.kafka.orderservice.dto.ProcessDeliveryDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "deliveryClient", url = "http://delivery-service:8080")
public interface DeliveryClient {

    @GetMapping("/delivery/users/{userId}/first-address")
    Map<String, Object> getUserAddress(@PathVariable Long userId);

    @GetMapping("/delivery/address/{addressId}")
    Map<String, Object> getAddress(@PathVariable Long addressId);

    @PostMapping("/delivery/process-delivery")
    Map<String, Object> processDelivery(@RequestBody ProcessDeliveryDto request);

    @GetMapping("/delivery/{deliveryId}")
    Map<String, Object> getDelivery(@PathVariable Long deliveryId);



}
