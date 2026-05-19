package com.nhhoang.synexbackend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoucherValidationResponse {
    private boolean valid;
    private String code;
    private String message;
    private double discountAmount;
    private double finalAmount;
}
