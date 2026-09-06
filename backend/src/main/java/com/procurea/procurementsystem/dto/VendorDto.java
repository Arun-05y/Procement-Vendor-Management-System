package com.procurea.procurementsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VendorDto {
    private Long id;
    private Long userId;
    private String username;
    private String vendorCode;
    private String companyName;
    private String contactPerson;
    private String email;
    private String phoneNumber;
    private String address;
    private String city;
    private String state;
    private String country;
    private String gstNumber;
    private String panNumber;
    private String category;

    // Bank details
    private String bankAccountNumber;
    private String bankName;
    private String bankIfscCode;

    private LocalDate contractStartDate;
    private LocalDate contractEndDate;
    private String status;
    private Double rating;

    // Performance metrics
    private Double onTimeDeliveryRate;
    private Double fulfillmentRate;
    private Double qualityRating;
    private Double responseTimeHours;
    private Double performanceScore;
    private String performanceCategory;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
