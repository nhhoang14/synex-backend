package com.nhhoang.synexbackend.dto.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShippingAddressDTO {
    private Long id;
    private String fullName;
    private String phone;
    private String street;
    private String ward;
    private String province;
    private String notes;
    private boolean isDefault;
}
