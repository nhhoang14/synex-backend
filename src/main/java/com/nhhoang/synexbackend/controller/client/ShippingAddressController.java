package com.nhhoang.synexbackend.controller.client;

import com.nhhoang.synexbackend.dto.request.ShippingAddressRequest;
import com.nhhoang.synexbackend.entity.ShippingAddress;
import com.nhhoang.synexbackend.service.ShippingAddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/me/addresses")
@RequiredArgsConstructor
@CrossOrigin("*")
public class ShippingAddressController {

    private final ShippingAddressService shippingAddressService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ShippingAddress> addAddress(@RequestBody ShippingAddressRequest request) {
        ShippingAddress address = toEntity(request);
        ShippingAddress savedAddress = shippingAddressService.addAddressForCurrentUser(address);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedAddress);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ShippingAddress>> getUserAddresses() {
        List<ShippingAddress> addresses = shippingAddressService.getCurrentUserAddresses();
        return ResponseEntity.ok(addresses);
    }

    @GetMapping("/{addressId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ShippingAddress> getAddress(@PathVariable Long addressId) {
        ShippingAddress address = shippingAddressService.getCurrentUserAddress(addressId);
        return ResponseEntity.ok(address);
    }

    @PutMapping("/{addressId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ShippingAddress> updateAddress(
            @PathVariable Long addressId,
            @RequestBody ShippingAddressRequest request) {
        ShippingAddress address = toEntity(request);
        ShippingAddress updatedAddress = shippingAddressService.updateAddress(addressId, address);
        return ResponseEntity.ok(updatedAddress);
    }

    @DeleteMapping("/{addressId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteAddress(@PathVariable Long addressId) {
        shippingAddressService.deleteAddress(addressId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{addressId}/default")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ShippingAddress> setDefaultAddress(@PathVariable Long addressId) {
        ShippingAddress address = shippingAddressService.setDefaultAddress(addressId);
        return ResponseEntity.ok(address);
    }

    private ShippingAddress toEntity(ShippingAddressRequest request) {
        ShippingAddress address = new ShippingAddress();
        address.setFullName(request.getFullName());
        address.setPhone(request.getPhone());
        address.setStreet(request.getStreet());
        address.setWard(request.getWard());
        address.setDistrict(request.getDistrict());
        address.setCity(request.getCity());
        address.setDefault(Boolean.TRUE.equals(request.getDefaultAddress()));
        return address;
    }
}
