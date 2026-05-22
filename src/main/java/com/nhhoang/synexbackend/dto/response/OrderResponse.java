package com.nhhoang.synexbackend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private Long id;
    private Long userId;
    private double totalAmount;
    private String status;
    private String paymentMethod;
    private LocalDateTime createdAt;
    private String shippingFullName;
    private String shippingPhone;
    private String shippingStreet;
    private String shippingWard;
    private String shippingDistrict;
    private String shippingCity;
    private String shippingNotes;
    private List<OrderItemResponse> items;
}