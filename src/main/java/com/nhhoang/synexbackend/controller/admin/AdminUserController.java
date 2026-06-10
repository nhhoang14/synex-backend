package com.nhhoang.synexbackend.controller.admin;

import com.nhhoang.synexbackend.dto.request.UpdateRoleRequest;
import com.nhhoang.synexbackend.dto.response.UserDTO;
import com.nhhoang.synexbackend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@CrossOrigin("*")
public class AdminUserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<UserDTO> updateUserRole(@PathVariable Long id, @RequestBody UpdateRoleRequest request) {
        return ResponseEntity.ok(userService.updateUserRole(id, request.getRole()));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<UserDTO> updateUserActivationStatus(@PathVariable Long id, @RequestParam boolean activated) {
        return ResponseEntity.ok(userService.updateUserActivationStatus(id, activated));
    }
}