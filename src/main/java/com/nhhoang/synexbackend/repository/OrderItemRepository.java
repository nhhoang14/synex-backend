package com.nhhoang.synexbackend.repository;

import com.nhhoang.synexbackend.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
	void deleteByOrderId(Long orderId);
}