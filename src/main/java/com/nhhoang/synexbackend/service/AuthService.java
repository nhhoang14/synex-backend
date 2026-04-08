package com.nhhoang.synexbackend.service;

import com.nhhoang.synexbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

@Service("authService")
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    /**
     * Kiểm tra user có phải owner hay admin
     * @param userId ID của resource owner
     * @param authentication authentication object từ SecurityContext
     * @return true nếu user là ADMIN hoặc là chủ sở hữu resource
     */
    public boolean isOwnerOrAdmin(Long userId, Authentication authentication) {
        if (authentication == null || userId == null) {
            return false;
        }

        // Kiểm tra nếu user là ADMIN
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if (authority.getAuthority().equals("ROLE_ADMIN")) {
                return true;
            }
        }

        // Kiểm tra nếu user là chủ sở hữu của resource
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .map(user -> user.getId().equals(userId))
                .orElse(false);
    }
}
