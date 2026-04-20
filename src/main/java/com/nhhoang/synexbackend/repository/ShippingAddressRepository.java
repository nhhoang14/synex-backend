package com.nhhoang.synexbackend.repository;

import com.nhhoang.synexbackend.entity.ShippingAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShippingAddressRepository extends JpaRepository<ShippingAddress, Long> {
    List<ShippingAddress> findByUserId(Long userId);

    Optional<ShippingAddress> findFirstByUserIdAndIsDefaultTrue(Long userId);

    long countByUserId(Long userId);

    void deleteByUserId(Long userId);
}
