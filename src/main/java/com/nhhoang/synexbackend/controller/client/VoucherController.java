package com.nhhoang.synexbackend.controller.client;

import com.nhhoang.synexbackend.dto.response.VoucherValidationResponse;
import com.nhhoang.synexbackend.service.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/vouchers")
@RequiredArgsConstructor
@CrossOrigin("*")
public class VoucherController {

    private final VoucherService voucherService;

    @GetMapping("/validate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<VoucherValidationResponse> validate(@RequestParam String code,
                                                              @RequestParam double orderAmount) {
        return ResponseEntity.ok(voucherService.validateVoucher(code, orderAmount));
    }
}
