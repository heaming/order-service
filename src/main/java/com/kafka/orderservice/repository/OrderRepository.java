package com.kafka.orderservice.repository;

import com.kafka.orderservice.entity.ProductOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<ProductOrder, Long> {
    List<ProductOrder> findByUserId(Long userId);
}
