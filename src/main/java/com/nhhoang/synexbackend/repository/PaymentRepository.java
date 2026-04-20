package com.nhhoang.synexbackend.repository;

import com.nhhoang.synexbackend.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
	void deleteByOrderId(Long orderId);
}