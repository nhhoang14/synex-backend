package com.nhhoang.synexbackend.controller.admin;

import com.nhhoang.synexbackend.dto.request.ContactMessageStatusUpdateRequest;
import com.nhhoang.synexbackend.model.ContactMessage;
import com.nhhoang.synexbackend.service.ContactMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/contact-messages")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@CrossOrigin("*")
public class AdminContactMessageController {

    private final ContactMessageService contactMessageService;

    @GetMapping
    public ResponseEntity<List<ContactMessage>> getAll() {
        return ResponseEntity.ok(contactMessageService.getAll());
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ContactMessage> updateStatus(@PathVariable Long id,
                                                       @RequestBody ContactMessageStatusUpdateRequest request) {
        return ResponseEntity.ok(contactMessageService.updateStatus(id, request.getStatus()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        contactMessageService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
