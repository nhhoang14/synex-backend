package com.nhhoang.synexbackend.controller;

import com.nhhoang.synexbackend.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/hooks/sepay-payment")
@CrossOrigin("*")
@RequiredArgsConstructor
public class SepayWebhookController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<String> handleSepayWebhook(@RequestBody Map<String, Object> payload) {
        try {
            if (payload.get("content") == null || payload.get("transferAmount") == null) {
                return ResponseEntity.badRequest().body("Missing required fields");
            }

            String content = (String) payload.get("content");
            Double amount = Double.parseDouble(payload.get("transferAmount").toString());

            orderService.processOrderPaymentViaWebhook(content, amount);

            return ResponseEntity.ok("OK");

        } catch (Exception e) {
            System.err.println("-> [SePay Webhook Error] " + e.getMessage());
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
}