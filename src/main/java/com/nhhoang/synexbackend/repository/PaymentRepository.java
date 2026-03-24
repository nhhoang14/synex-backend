package com.nhhoang.synexbackend.repository;

import com.nhhoang.synexbackend.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}