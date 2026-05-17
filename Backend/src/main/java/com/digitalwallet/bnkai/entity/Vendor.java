package com.digitalwallet.bnkai.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "vendors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Vendor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vendor_id")
    private Integer vendorId;

    @Column(name = "vendor_name", nullable = false, unique = true)
    private String vendorName;

    @Column(name = "description")
    private String description;

    @Column(name = "contact_person_name")
    private String contactPersonName;

    @Column(name = "contact_email")
    private String contactEmail;

    @Column(name = "contact_phone")
    private String contactPhone;

    @Column(name = "website_url")
    private String websiteUrl;

    @Column(name = "total_gold_quantity", nullable = false)
    private BigDecimal totalGoldQuantity;

    @Column(name = "current_gold_price", nullable = false)
    private BigDecimal currentGoldPrice;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @JsonProperty(
            access =
                    JsonProperty.Access.WRITE_ONLY
    )
    @Column(name = "password")
    private String password;
}