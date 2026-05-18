package com.digitalwallet.bnkai.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EditVendorProfileRequest {
    private String contactPersonName;
    private String contactEmail;
    private String contactPhone;
    private String description;
    private String websiteUrl;
}
