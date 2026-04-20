package com.nhhoang.synexbackend.service;

import com.nhhoang.synexbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
        public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        com.nhhoang.synexbackend.entity.User appUser = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        String normalizedRole = normalizeRole(appUser.getRole());

        return User.withUsername(appUser.getEmail())
                .password(appUser.getPassword())
                .roles(normalizedRole)
                .build();
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
}
