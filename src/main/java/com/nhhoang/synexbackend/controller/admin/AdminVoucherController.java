package com.nhhoang.synexbackend.controller.admin;

import com.nhhoang.synexbackend.dto.request.UpsertVoucherRequest;
import com.nhhoang.synexbackend.dto.response.VoucherResponse;
import com.nhhoang.synexbackend.service.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/vouchers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@CrossOrigin("*")
public class AdminVoucherController {

    private final VoucherService voucherService;

    @GetMapping
    public ResponseEntity<List<VoucherResponse>> getAll() {
        return ResponseEntity.ok(voucherService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<VoucherResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(voucherService.getById(id));
    }

    @PostMapping
    public ResponseEntity<VoucherResponse> create(@RequestBody UpsertVoucherRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(voucherService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VoucherResponse> update(@PathVariable Long id,
                                                  @RequestBody UpsertVoucherRequest request) {
        return ResponseEntity.ok(voucherService.update(id, request));
    }

    @PatchMapping("/{id}/active")
    public ResponseEntity<VoucherResponse> updateActive(@PathVariable Long id,
                                                        @RequestParam boolean active) {
        return ResponseEntity.ok(voucherService.updateActive(id, active));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        voucherService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
