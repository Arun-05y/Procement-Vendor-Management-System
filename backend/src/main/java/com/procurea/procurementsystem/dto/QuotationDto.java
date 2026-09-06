package com.procurea.procurementsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuotationDto {
    private Long id;
    private Long rfqId;
    private String rfqTitle;
    private Long vendorId;
    private String vendorCompanyName;
    private Double totalAmount;
    private Integer deliveryDays;
    private Double qualityRating;
    private Integer warrantyMonths;
    private Double previousPerformanceScore;
    private String termsAndConditions;
    private String status;
    private LocalDateTime submittedAt;
}
