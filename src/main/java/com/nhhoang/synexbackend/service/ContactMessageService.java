package com.nhhoang.synexbackend.service;

import com.nhhoang.synexbackend.dto.request.ContactMessageRequest;
import com.nhhoang.synexbackend.entity.ContactMessage;
import com.nhhoang.synexbackend.repository.ContactMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ContactMessageService {

    private final ContactMessageRepository contactMessageRepository;

    @Transactional
    public ContactMessage create(ContactMessageRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Contact message is required");
        }

        if (request.getFullName() == null || request.getFullName().isBlank()) {
            throw new IllegalArgumentException("Full name is required");
        }

        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }

        if (request.getMessage() == null || request.getMessage().isBlank()) {
            throw new IllegalArgumentException("Message is required");
        }

        ContactMessage message = new ContactMessage();
        message.setFullName(request.getFullName().trim());
        message.setEmail(request.getEmail().trim());
        message.setPhone(request.getPhone() == null ? null : request.getPhone().trim());
        message.setSubject(request.getSubject() == null ? null : request.getSubject().trim());
        message.setMessage(request.getMessage().trim());
        message.setStatus("NEW");

        return contactMessageRepository.save(message);
    }

    public List<ContactMessage> getAll() {
        return contactMessageRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public ContactMessage updateStatus(Long id, String status) {
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("Status is required");
        }

        ContactMessage message = contactMessageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contact message not found"));

        message.setStatus(status.trim().toUpperCase());
        return contactMessageRepository.save(message);
    }

    @Transactional
    public void delete(Long id) {
        if (!contactMessageRepository.existsById(id)) {
            throw new RuntimeException("Contact message not found");
        }

        contactMessageRepository.deleteById(id);
    }
}
