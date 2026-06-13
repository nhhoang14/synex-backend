package com.nhhoang.synexbackend.service;

import com.nhhoang.synexbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service("authService")
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    public boolean isOwnerOrAdmin(Long userId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || userId == null) {
            return false;
        }

        // Kiểm tra nếu user là ADMIN
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        
        if (isAdmin) return true;

        // Kiểm tra nếu user là chủ sở hữu của resource
        String email = authentication.getName();
        if (email == null) return false;
        
        return userRepository.findByEmail(email)
                .map(user -> user.getId().equals(userId))
                .orElse(false);
    }
}
