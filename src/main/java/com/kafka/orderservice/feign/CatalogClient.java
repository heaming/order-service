package com.kafka.orderservice.feign;

import com.kafka.orderservice.dto.DecreaseStockCountDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "catalogClient", url = "http://catalog-service:8080")
public interface CatalogClient {

    @GetMapping("/catalog/product/{productId}")
    Map<String, Object> getProduct(@PathVariable Long productId);

    @PostMapping("/catalog/proudct/{productId}/stock-count")
    void decreaseStockCount(@PathVariable Long productId, @RequestBody DecreaseStockCountDto request);

}
