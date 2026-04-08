package com.nhhoang.synexbackend.service;

import com.nhhoang.synexbackend.model.ShippingAddress;
import com.nhhoang.synexbackend.model.User;
import com.nhhoang.synexbackend.repository.ShippingAddressRepository;
import com.nhhoang.synexbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShippingAddressService {

    private final ShippingAddressRepository shippingAddressRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public ShippingAddress addAddressForCurrentUser(ShippingAddress address) {
        User user = getCurrentUser();
        address.setUser(user);

        // Nếu đặt là default, thì các địa chỉ khác không phải default
        if (address.isDefault()) {
            shippingAddressRepository.findByUserId(user.getId()).forEach(addr -> {
                addr.setDefault(false);
                shippingAddressRepository.save(addr);
            });
        }

        return shippingAddressRepository.save(address);
    }

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

        // Nếu đặt là default, thì các địa chỉ khác không phải default
        if (updatedAddress.isDefault() && !address.isDefault()) {
            shippingAddressRepository.findByUserId(currentUser.getId()).forEach(addr -> {
                addr.setDefault(false);
                shippingAddressRepository.save(addr);
            });
        }

        address.setDefault(updatedAddress.isDefault());

        return shippingAddressRepository.save(address);
    }

    public void deleteAddress(Long addressId) {
        User currentUser = getCurrentUser();
        ShippingAddress address = shippingAddressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        if (!address.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        shippingAddressRepository.delete(address);
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
    public ShippingAddress addAddress(Long userId, ShippingAddress address) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        address.setUser(user);
        if (address.isDefault()) {
            shippingAddressRepository.findByUserId(userId).forEach(addr -> {
                addr.setDefault(false);
                shippingAddressRepository.save(addr);
            });
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
