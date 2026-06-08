package com.nhhoang.synexbackend.dto.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShippingAddressDTO {
    private Long id;
    private String fullName;
    private String phone;
    private String address;
    private boolean isDefault;
}
