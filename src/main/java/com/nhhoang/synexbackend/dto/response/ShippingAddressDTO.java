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
    private String district;
    private String city;
    private String state;
    private String zipCode;
    private String country;
    private String notes;
    private boolean isDefault;
}
