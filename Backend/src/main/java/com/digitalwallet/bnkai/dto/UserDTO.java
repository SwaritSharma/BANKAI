package com.digitalwallet.bnkai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UserDTO {

    @JsonProperty("user_id")
    private Integer userId;

    private String name;

    private String email;

    private BigDecimal balance;

    private HoldingDTO.AddressDTO address;
}
