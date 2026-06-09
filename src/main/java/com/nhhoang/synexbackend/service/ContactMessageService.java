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
        // Thay vì validate thủ công, hãy dùng @Valid ở Controller kết hợp với 
        // các annotation như @NotBlank, @Email trong ContactMessageRequest.
        // Ở đây ta chỉ giữ lại logic mapping để code gọn gàng hơn.
        
        ContactMessage message = new ContactMessage();
        message.setFullName(request.getFullName().trim());
        message.setEmail(request.getEmail().trim());
        message.setPhone(request.getPhone() == null ? null : request.getPhone().trim());
        message.setSubject(request.getSubject() == null ? null : request.getSubject().trim());
        message.setMessage(request.getMessage().trim());

        // Xử lý file nếu có upload
        if (request.getImageFile() != null && !request.getImageFile().isEmpty()) {
            // String uploadedUrl = fileService.upload(request.getImageFile());
            // message.setImageUrl(uploadedUrl);
        }
        
        // Khởi tạo trạng thái mặc định (có thể dùng @PrePersist trong Entity tương tự Order.java)
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
