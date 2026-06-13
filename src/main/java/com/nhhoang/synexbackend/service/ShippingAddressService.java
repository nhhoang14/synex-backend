package com.nhhoang.synexbackend.service;

import com.nhhoang.synexbackend.entity.ShippingAddress;
import com.nhhoang.synexbackend.entity.User;
import com.nhhoang.synexbackend.repository.ShippingAddressRepository;
import com.nhhoang.synexbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShippingAddressService {

    private static final int MAX_ADDRESSES_PER_USER = 3;

    private final ShippingAddressRepository shippingAddressRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional
    public ShippingAddress addAddress(ShippingAddress address) {
        User user = getCurrentUser();
        long currentAddressCount = shippingAddressRepository.countByUserId(user.getId());
        if (currentAddressCount >= MAX_ADDRESSES_PER_USER) {
            throw new IllegalArgumentException("Each account can only have up to 3 shipping addresses");
        }

        address.setUser(user);
        List<ShippingAddress> existingAddresses = shippingAddressRepository.findByUserId(user.getId());

        if (existingAddresses.isEmpty()) {
            address.setDefault(true);
        } else if (address.isDefault()) {
            resetDefaultAddress(user.getId());
        } else {
            address.setDefault(false);
        }

        return shippingAddressRepository.save(address);
    }

    @Transactional
    public ShippingAddress updateAddress(Long addressId, ShippingAddress updatedAddress) {
        User currentUser = getCurrentUser();
        ShippingAddress address = shippingAddressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        if (!address.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        address.setFullName(updatedAddress.getFullName());
        address.setPhone(updatedAddress.getPhone());
        address.setAddress(updatedAddress.getAddress());

        if (updatedAddress.isDefault() && !address.isDefault()) {
            resetDefaultAddress(currentUser.getId());
        }

        if (!updatedAddress.isDefault() && address.isDefault()) {
            throw new IllegalArgumentException("Default address cannot be unset directly. Set another address as default first");
        }

        address.setDefault(updatedAddress.isDefault());

        return shippingAddressRepository.save(address);
    }

    @Transactional
    public void deleteAddress(Long addressId) {
        User currentUser = getCurrentUser();
        ShippingAddress address = shippingAddressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        if (!address.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        boolean wasDefault = address.isDefault();
        shippingAddressRepository.delete(address);

        if (wasDefault) {
            List<ShippingAddress> remainingAddresses = shippingAddressRepository.findByUserId(currentUser.getId());
            if (!remainingAddresses.isEmpty()) {
                ShippingAddress nextDefault = remainingAddresses.get(0);
                nextDefault.setDefault(true);
                shippingAddressRepository.save(nextDefault);
            }
        }
    }

    @Transactional(readOnly = true)
    public List<ShippingAddress> getCurrentUserAddresses() {
        User currentUser = getCurrentUser();
        return shippingAddressRepository.findByUserId(currentUser.getId());
    }

    @Transactional(readOnly = true)
    public ShippingAddress getCurrentUserAddress(Long addressId) {
        User currentUser = getCurrentUser();
        ShippingAddress address = shippingAddressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        if (!address.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        return address;
    }

    @Transactional
    public ShippingAddress setDefaultAddress(Long addressId) {
        User currentUser = getCurrentUser();
        ShippingAddress address = shippingAddressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        if (!address.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        resetDefaultAddress(currentUser.getId());
        address.setDefault(true);
        
        return shippingAddressRepository.save(address);
    }

    @Transactional
    public void resetDefaultAddress(Long userId) {
        shippingAddressRepository.findByUserId(userId).forEach(addr -> {
            addr.setDefault(false);
            shippingAddressRepository.save(addr);
        });
    }
}
