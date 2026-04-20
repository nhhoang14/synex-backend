package com.nhhoang.synexbackend.repository;

import com.nhhoang.synexbackend.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Cart findByUserId(Long userId);

    void deleteByUserId(Long userId);
}