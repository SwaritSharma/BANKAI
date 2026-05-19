package com.digitalwallet.bnkai.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EditVendorProfileRequest {

    @Size(max = 100, message = "Contact person name must be at most 100 characters")
    private String contactPersonName;

    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Contact email must be at most 100 characters")
    private String contactEmail;
    @Pattern(regexp = "^\\d{10}$", message = "Contact phone must be exactly 10 digits")
    private String contactPhone;

    @Size(max = 1000, message = "Description must be at most 1000 characters")
    private String description;

    @Size(max = 255, message = "Website URL must be at most 255 characters")
    private String websiteUrl;
}
