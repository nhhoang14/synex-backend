package com.nhhoang.synexbackend.repository;

import com.nhhoang.synexbackend.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
}