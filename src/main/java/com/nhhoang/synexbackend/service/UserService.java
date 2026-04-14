package com.nhhoang.synexbackend.service;

import com.nhhoang.synexbackend.dto.request.ChangePasswordRequest;
import com.nhhoang.synexbackend.dto.request.UpdateUserRequest;
import com.nhhoang.synexbackend.dto.response.UserDTO;
import com.nhhoang.synexbackend.model.Cart;
import com.nhhoang.synexbackend.model.User;
import com.nhhoang.synexbackend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ShippingAddressRepository shippingAddressRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final ReviewRepository reviewRepository;
    private final WishlistRepository wishlistRepository;

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream().map(this::mapToUserDTO).toList();
    }

    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return mapToUserDTO(user);
    }

    /**
     * Lấy profile hiện tại của user đang login
     */
    public UserDTO getCurrentUserProfile() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return mapToUserDTO(user);
    }

    /**
     * Update profile của user hiện tại
     */
    public UserDTO updateUserProfile(UpdateUserRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("UpdateUserRequest cannot be null");
        }

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Update username nếu có
        if (request.getUsername() != null && !request.getUsername().trim().isEmpty()) {
            user.setUsername(request.getUsername().trim());
        }

        // Update email nếu có
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            String newEmail = request.getEmail().trim();
            // Kiểm tra email mới không trùng với email của user khác
            if (!newEmail.equals(user.getEmail()) && 
                userRepository.findByEmail(newEmail).isPresent()) {
                throw new RuntimeException("Email already in use");
            }
            user.setEmail(newEmail);
        }

        // Update phone nếu có
        if (request.getPhone() != null && !request.getPhone().trim().isEmpty()) {
            user.setPhone(request.getPhone().trim());
        }

        User updatedUser = userRepository.save(user);
        return mapToUserDTO(updatedUser);
    }

    public void changePassword(ChangePasswordRequest request) {
        if (request == null || request.getCurrentPassword() == null || request.getNewPassword() == null) {
            throw new IllegalArgumentException("Current password and new password are required");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("New password and confirm password do not match");
        }

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    public UserDTO updateUserRole(Long id, String role) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid user ID");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setRole(normalizeRole(role));
        return mapToUserDTO(userRepository.save(user));
    }

    /**
     * Delete user (only ADMIN can call this)
     */
    @Transactional
    public void deleteUser(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid user ID");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).forEach(order -> {
            paymentRepository.deleteByOrderId(order.getId());
            orderItemRepository.deleteByOrderId(order.getId());
        });

        reviewRepository.deleteByUserId(user.getId());
        wishlistRepository.deleteByUserId(user.getId());
        shippingAddressRepository.deleteByUserId(user.getId());

        Cart cart = cartRepository.findByUserId(user.getId());
        if (cart != null) {
            cartItemRepository.deleteByCartId(cart.getId());
            cartRepository.deleteByUserId(user.getId());
        }
        
        userRepository.delete(user);
    }

    private String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            return "USER";
        }

        String trimmedRole = role.trim().toUpperCase();
        if (trimmedRole.startsWith("ROLE_")) {
            return trimmedRole.substring(5);
        }
        return trimmedRole;
    }

    /**
     * Map User entity to UserDTO
     */
    private UserDTO mapToUserDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setRole(user.getRole());
        return dto;
    }
}