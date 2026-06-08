package com.nhhoang.synexbackend.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShippingAddressRequest {
    private Long id;
    private String fullName;
    private String phone;
    private String address;
    
    @JsonProperty("default")
    @JsonAlias("isDefault")
    private Boolean defaultAddress;
}