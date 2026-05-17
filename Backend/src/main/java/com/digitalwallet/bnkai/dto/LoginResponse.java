package com.digitalwallet.bnkai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
public class LoginResponse {

    private String token;
    private String name;
    private String email;
    private String role;
    
    @JsonProperty("user_id")
    private Integer userId;

    @JsonProperty("vendor_id")
    private Integer vendorId;

    public LoginResponse(String token, String name, String email, String role, Integer id) {
        this.token = token;
        this.name = name;
        this.email = email;
        this.role = role;
        if ("VENDOR".equals(role)) {
            this.vendorId = id;
            this.userId = id;
        } else {
            this.userId = id;
            this.vendorId = null;
        }
    }
}