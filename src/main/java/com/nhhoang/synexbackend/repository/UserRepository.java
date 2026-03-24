package com.nhhoang.synexbackend.repository;

import com.nhhoang.synexbackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}