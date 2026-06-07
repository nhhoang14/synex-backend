package com.nhhoang.synexbackend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/hooks/sepay-payment")
@CrossOrigin("*")
public class SepayWebhookController {

    @PostMapping
    public ResponseEntity<String> handleSepayWebhook(@RequestBody Map<String, Object> payload) {
        System.out.println(">>> Received SePay Webhook: " + payload);

        try {
            String content = (String) payload.get("content");
            Double amount = Double.parseDouble(payload.get("transferAmount").toString());

            System.out.println("=== KẾT QUẢ PHÂN TÍCH DIỄN BIẾN TRANSACTIONS ===");
            System.out.println("-> Khách chuyển khoản nội dung: " + content);
            System.out.println("-> Số tiền nhận được: " + amount + " VND");
            System.out.println("===============================================");

            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            System.err.println("Lỗi xử lý dữ liệu: " + e.getMessage());
            return ResponseEntity.status(500).body("Error");
        }
    }
}