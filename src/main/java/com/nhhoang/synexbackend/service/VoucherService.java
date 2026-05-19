package com.nhhoang.synexbackend.service;

import com.nhhoang.synexbackend.dto.request.UpsertVoucherRequest;
import com.nhhoang.synexbackend.dto.response.VoucherResponse;
import com.nhhoang.synexbackend.dto.response.VoucherValidationResponse;
import com.nhhoang.synexbackend.entity.Voucher;
import com.nhhoang.synexbackend.repository.VoucherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class VoucherService {

    private final VoucherRepository voucherRepository;

    public List<VoucherResponse> getAll() {
        return voucherRepository.findAll().stream().map(this::toResponse).toList();
    }

    public VoucherResponse getById(Long id) {
        return toResponse(findById(id));
    }

    public VoucherResponse create(UpsertVoucherRequest request) {
        validateRequest(request);

        String code = normalizeCode(request.getCode());
        if (voucherRepository.existsByCodeIgnoreCase(code)) {
            throw new RuntimeException("Voucher code already exists");
        }

        Voucher voucher = new Voucher();
        applyRequest(voucher, request);
        voucher.setCode(code);

        return toResponse(voucherRepository.save(voucher));
    }

    public VoucherResponse update(Long id, UpsertVoucherRequest request) {
        validateRequest(request);

        Voucher voucher = findById(id);
        String code = normalizeCode(request.getCode());

        if (voucherRepository.existsByCodeIgnoreCaseAndIdNot(code, id)) {
            throw new RuntimeException("Voucher code already exists");
        }

        applyRequest(voucher, request);
        voucher.setCode(code);

        return toResponse(voucherRepository.save(voucher));
    }

    public VoucherResponse updateActive(Long id, boolean active) {
        Voucher voucher = findById(id);
        voucher.setActive(active);
        return toResponse(voucherRepository.save(voucher));
    }

    public void delete(Long id) {
        Voucher voucher = findById(id);
        voucherRepository.delete(voucher);
    }

    @Transactional(readOnly = true)
    public VoucherValidationResponse validateVoucher(String code, double orderAmount) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Voucher code is required");
        }

        if (orderAmount <= 0) {
            throw new IllegalArgumentException("Order amount must be greater than 0");
        }

        Voucher voucher = voucherRepository.findByCodeIgnoreCase(code.trim())
                .orElseThrow(() -> new RuntimeException("Voucher not found"));

        validateVoucherAvailability(voucher, orderAmount);

        double discountAmount = calculateDiscount(voucher, orderAmount);
        double finalAmount = Math.max(0, orderAmount - discountAmount);

        return new VoucherValidationResponse(
                true,
                voucher.getCode(),
                "Voucher is valid",
                discountAmount,
                finalAmount
        );
    }

    private Voucher findById(Long id) {
        return voucherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Voucher not found"));
    }

    private void applyRequest(Voucher voucher, UpsertVoucherRequest request) {
        voucher.setDescription(request.getDescription());
        voucher.setDiscountType(request.getDiscountType().trim().toUpperCase());
        voucher.setDiscountValue(request.getDiscountValue());
        voucher.setMinOrderAmount(Math.max(0, request.getMinOrderAmount()));
        voucher.setMaxDiscountAmount(request.getMaxDiscountAmount());
        voucher.setStartAt(request.getStartAt());
        voucher.setEndAt(request.getEndAt());
        voucher.setUsageLimit(request.getUsageLimit());
        voucher.setActive(request.isActive());
    }

    private void validateRequest(UpsertVoucherRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Voucher request is required");
        }

        if (request.getCode() == null || request.getCode().isBlank()) {
            throw new IllegalArgumentException("Voucher code is required");
        }

        if (request.getDiscountType() == null || request.getDiscountType().isBlank()) {
            throw new IllegalArgumentException("Discount type is required");
        }

        String discountType = request.getDiscountType().trim().toUpperCase();
        if (!"PERCENT".equals(discountType) && !"FIXED".equals(discountType)) {
            throw new IllegalArgumentException("Discount type must be PERCENT or FIXED");
        }

        if (request.getDiscountValue() <= 0) {
            throw new IllegalArgumentException("Discount value must be greater than 0");
        }

        if ("PERCENT".equals(discountType) && request.getDiscountValue() > 100) {
            throw new IllegalArgumentException("Percent discount cannot exceed 100");
        }

        if (request.getMinOrderAmount() < 0) {
            throw new IllegalArgumentException("Min order amount cannot be negative");
        }

        if (request.getUsageLimit() != null && request.getUsageLimit() < 1) {
            throw new IllegalArgumentException("Usage limit must be at least 1");
        }

        if (request.getMaxDiscountAmount() != null && request.getMaxDiscountAmount() <= 0) {
            throw new IllegalArgumentException("Max discount amount must be greater than 0");
        }

        if (request.getStartAt() != null && request.getEndAt() != null
                && request.getEndAt().isBefore(request.getStartAt())) {
            throw new IllegalArgumentException("Voucher end date must be after start date");
        }
    }

    private void validateVoucherAvailability(Voucher voucher, double orderAmount) {
        if (!voucher.isActive()) {
            throw new RuntimeException("Voucher is inactive");
        }

        LocalDateTime now = LocalDateTime.now();
        if (voucher.getStartAt() != null && now.isBefore(voucher.getStartAt())) {
            throw new RuntimeException("Voucher is not active yet");
        }

        if (voucher.getEndAt() != null && now.isAfter(voucher.getEndAt())) {
            throw new RuntimeException("Voucher has expired");
        }

        if (orderAmount < voucher.getMinOrderAmount()) {
            throw new RuntimeException("Order amount does not meet voucher minimum");
        }

        if (voucher.getUsageLimit() != null && voucher.getUsedCount() != null
                && voucher.getUsedCount() >= voucher.getUsageLimit()) {
            throw new RuntimeException("Voucher usage limit exceeded");
        }
    }

    private double calculateDiscount(Voucher voucher, double orderAmount) {
        double discount;

        if ("PERCENT".equalsIgnoreCase(voucher.getDiscountType())) {
            discount = orderAmount * (voucher.getDiscountValue() / 100.0);
            if (voucher.getMaxDiscountAmount() != null) {
                discount = Math.min(discount, voucher.getMaxDiscountAmount());
            }
            return discount;
        }

        discount = voucher.getDiscountValue();
        return Math.min(discount, orderAmount);
    }

    private VoucherResponse toResponse(Voucher voucher) {
        return new VoucherResponse(
                voucher.getId(),
                voucher.getCode(),
                voucher.getDescription(),
                voucher.getDiscountType(),
                voucher.getDiscountValue(),
                voucher.getMinOrderAmount(),
                voucher.getMaxDiscountAmount(),
                voucher.getStartAt(),
                voucher.getEndAt(),
                voucher.getUsageLimit(),
                voucher.getUsedCount(),
                voucher.isActive()
        );
    }

    private String normalizeCode(String code) {
        return code.trim().toUpperCase();
    }
}
