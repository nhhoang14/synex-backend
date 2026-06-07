package com.nhhoang.synexbackend.repository;

import com.nhhoang.synexbackend.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
	void deleteByOrderId(Long orderId);

	Optional<Payment> findByOrderId(Long orderId);
}