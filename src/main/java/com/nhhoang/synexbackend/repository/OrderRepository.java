package com.nhhoang.synexbackend.repository;

import com.nhhoang.synexbackend.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}