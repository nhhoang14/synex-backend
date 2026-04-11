package com.nhhoang.synexbackend.controller;

import com.nhhoang.synexbackend.dto.ChangePasswordRequest;
import com.nhhoang.synexbackend.dto.UpdateUserRequest;
import com.nhhoang.synexbackend.dto.UserDTO;
import com.nhhoang.synexbackend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin("*")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDTO> getCurrentUser() {
        UserDTO user = userService.getCurrentUserProfile();
        return ResponseEntity.ok(user);
    }

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDTO> updateProfile(@RequestBody UpdateUserRequest request) {
        UserDTO user = userService.updateUserProfile(request);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/me/password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> changePassword(@RequestBody ChangePasswordRequest request) {
        userService.changePassword(request);
        return ResponseEntity.ok("Password changed successfully");
    }
}