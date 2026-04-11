package com.nhhoang.synexbackend.service;

import com.nhhoang.synexbackend.model.ShippingAddress;
import com.nhhoang.synexbackend.model.User;
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
    public ShippingAddress addAddressForCurrentUser(ShippingAddress address) {
        User user = getCurrentUser();
        long currentAddressCount = shippingAddressRepository.countByUserId(user.getId());
        if (currentAddressCount >= MAX_ADDRESSES_PER_USER) {
            throw new IllegalArgumentException("Each account can only have up to 3 shipping addresses");
        }

        address.setUser(user);
        List<ShippingAddress> existingAddresses = shippingAddressRepository.findByUserId(user.getId());

        // First address is always default. Otherwise only keep one default.
        if (existingAddresses.isEmpty()) {
            address.setDefault(true);
        } else if (address.isDefault()) {
            existingAddresses.forEach(addr -> {
                addr.setDefault(false);
                shippingAddressRepository.save(addr);
            });
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
        address.setStreet(updatedAddress.getStreet());
        address.setWard(updatedAddress.getWard());
        address.setDistrict(updatedAddress.getDistrict());
        address.setCity(updatedAddress.getCity());
        address.setState(updatedAddress.getState());
        address.setZipCode(updatedAddress.getZipCode());
        address.setCountry(updatedAddress.getCountry());
        address.setNotes(updatedAddress.getNotes());

        // If this address is set to default, unset the others.
        if (updatedAddress.isDefault() && !address.isDefault()) {
            shippingAddressRepository.findByUserId(currentUser.getId()).forEach(addr -> {
                addr.setDefault(false);
                shippingAddressRepository.save(addr);
            });
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

    public List<ShippingAddress> getCurrentUserAddresses() {
        User currentUser = getCurrentUser();
        return shippingAddressRepository.findByUserId(currentUser.getId());
    }

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

        // Unset other default addresses
        shippingAddressRepository.findByUserId(currentUser.getId()).forEach(addr -> {
            addr.setDefault(false);
            shippingAddressRepository.save(addr);
        });

        address.setDefault(true);
        return shippingAddressRepository.save(address);
    }

    // Deprecated methods (kept for backward compatibility if needed)
    @Transactional
    public ShippingAddress addAddress(Long userId, ShippingAddress address) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        long currentAddressCount = shippingAddressRepository.countByUserId(userId);
        if (currentAddressCount >= MAX_ADDRESSES_PER_USER) {
            throw new IllegalArgumentException("Each account can only have up to 3 shipping addresses");
        }

        address.setUser(user);
        List<ShippingAddress> existingAddresses = shippingAddressRepository.findByUserId(userId);
        if (existingAddresses.isEmpty()) {
            address.setDefault(true);
        } else if (address.isDefault()) {
            existingAddresses.forEach(addr -> {
                addr.setDefault(false);
                shippingAddressRepository.save(addr);
            });
        } else {
            address.setDefault(false);
        }
        return shippingAddressRepository.save(address);
    }

    public List<ShippingAddress> getUserAddresses(Long userId) {
        return shippingAddressRepository.findByUserId(userId);
    }

    public ShippingAddress getAddress(Long userId, Long addressId) {
        ShippingAddress address = shippingAddressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found"));
        if (!address.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }
        return address;
    }
}
