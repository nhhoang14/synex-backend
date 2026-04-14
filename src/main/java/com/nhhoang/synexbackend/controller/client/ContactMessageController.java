package com.nhhoang.synexbackend.controller.client;

import com.nhhoang.synexbackend.dto.request.ContactMessageRequest;
import com.nhhoang.synexbackend.model.ContactMessage;
import com.nhhoang.synexbackend.service.ContactMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contact-messages")
@RequiredArgsConstructor
@CrossOrigin("*")
public class ContactMessageController {

    private final ContactMessageService contactMessageService;

    @PostMapping
    public ResponseEntity<ContactMessage> create(@RequestBody ContactMessageRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(contactMessageService.create(request));
    }
}
