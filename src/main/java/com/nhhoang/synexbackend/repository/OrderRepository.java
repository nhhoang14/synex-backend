package com.nhhoang.synexbackend.repository;

import com.nhhoang.synexbackend.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;

import java.util.Optional;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
	List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

	List<Order> findAllByOrderByCreatedAtDesc();

	Optional<Order> findByOrderCode(String orderCode);

    List<Order> findByPaymentMethodAndStatusAndCreatedAtBefore(String paymentMethod, String status, LocalDateTime threshold);
}