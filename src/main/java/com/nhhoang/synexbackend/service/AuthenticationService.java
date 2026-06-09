package com.nhhoang.synexbackend.service;

import com.nhhoang.synexbackend.dto.request.LoginRequest;
import com.nhhoang.synexbackend.dto.request.RefreshTokenRequest;
import com.nhhoang.synexbackend.dto.request.RegisterRequest;
import com.nhhoang.synexbackend.dto.request.ForgotPasswordRequest;
import com.nhhoang.synexbackend.dto.request.ResetPasswordRequest;
import com.nhhoang.synexbackend.dto.response.AuthResponse;
import com.nhhoang.synexbackend.entity.User;
import com.nhhoang.synexbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final JavaMailSender mailSender;

    private static final long OTP_EXPIRY_MINUTES = 5; // OTP valid for 5 minutes

    public AuthResponse register(RegisterRequest request) {
        if (request == null || request.getEmail() == null || request.getPassword() == null) {
            throw new IllegalArgumentException("Email and password are required");
        }

        // Kiểm tra email đã tồn tại
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered");
        }
        // Kiểm tra username đã tồn tại
        if (request.getUsername() != null && userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username already registered");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setRole("USER");
        user.setActivated(true);

        User savedUser = userRepository.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(savedUser.getEmail());
        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        return mapToAuthResponse(savedUser, accessToken, refreshToken);
    }

    public AuthResponse login(LoginRequest request) {
        if (request == null || request.getIdentifier() == null || request.getPassword() == null) {
            throw new IllegalArgumentException("Identifier and password are required");
        }

        String identifier = request.getIdentifier().trim();
        String password = request.getPassword();
        if (identifier.isBlank() || password.isBlank()) {
            throw new IllegalArgumentException("Identifier and password are required");
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(identifier, password)
        );

        com.nhhoang.synexbackend.entity.User user = userRepository.findByEmail(identifier)
                .or(() -> userRepository.findByUsername(identifier))
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserDetails userDetails = userDetailsService.loadUserByUsername(identifier);
        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        return mapToAuthResponse(user, accessToken, refreshToken);
    }

    public AuthResponse refreshToken(RefreshTokenRequest request) {
        if (request == null || request.getRefreshToken() == null || request.getRefreshToken().isBlank()) {
            throw new IllegalArgumentException("Refresh token is required");
        }

        String refreshToken = request.getRefreshToken().trim();
        String email = jwtService.extractUsername(refreshToken);
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        if (!jwtService.isRefreshToken(refreshToken) || !jwtService.isTokenValid(refreshToken, userDetails)) {
            throw new RuntimeException("Invalid refresh token");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String newAccessToken = jwtService.generateAccessToken(userDetails);
        String newRefreshToken = jwtService.generateRefreshToken(userDetails);

        return mapToAuthResponse(user, newAccessToken, newRefreshToken);
    }

    private AuthResponse mapToAuthResponse(User user, String token, String refreshToken) {
        AuthResponse response = new AuthResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setFullName(user.getFullName());
        response.setPhone(user.getPhone());
        response.setRole(user.getRole());
        response.setToken(token);
        response.setRefreshToken(refreshToken);
        return response;
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        if (request == null || request.getIdentifier() == null || request.getIdentifier().isBlank()) {
            throw new IllegalArgumentException("Identifier is required");
        }

        String identifier = request.getIdentifier().trim();
        User user = userRepository.findByEmail(identifier)
                .or(() -> userRepository.findByUsername(identifier))
                .orElseThrow(() -> new RuntimeException("User not found with identifier: " + identifier));

        String otpCode = generateOtp();
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES);

        user.setOtpCode(otpCode);
        user.setOtpExpiredAt(expiryTime);
        userRepository.save(user);

        sendOtpEmail(user.getEmail(), otpCode);
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        if (request == null || request.getIdentifier() == null || request.getIdentifier().isBlank()) {
            throw new IllegalArgumentException("Identifier is required");
        }
        if (request.getOtpCode() == null || request.getOtpCode().isBlank()) {
            throw new IllegalArgumentException("OTP code is required");
        }
        if (request.getNewPassword() == null || request.getNewPassword().isBlank()) {
            throw new IllegalArgumentException("New password is required");
        }

        String identifier = request.getIdentifier().trim();
        String providedOtp = request.getOtpCode().trim();

        User user = userRepository.findByEmail(identifier)
                .or(() -> userRepository.findByUsername(identifier))
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getOtpCode() == null || !user.getOtpCode().equals(providedOtp)) {
            throw new IllegalArgumentException("Invalid OTP code");
        }

        if (LocalDateTime.now().isAfter(user.getOtpExpiredAt())) {
            user.setOtpCode(null);
            user.setOtpExpiredAt(null);
            userRepository.save(user);
            throw new IllegalArgumentException("OTP code has expired");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        user.setOtpCode(null);
        user.setOtpExpiredAt(null);
        
        userRepository.save(user);
    }

    private String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000); 
        return String.valueOf(otp);
    }

    private void sendOtpEmail(String recipientEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(recipientEmail);
        message.setSubject("[Synex Accessories] Your Password Reset OTP");
        message.setText("Your OTP code is: " + otp + ". It will expire in " + OTP_EXPIRY_MINUTES + " minutes.");
        mailSender.send(message);
    }
}
