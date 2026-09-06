package com.procurea.procurementsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VendorEvaluationDto {
    private Long id;
    private Long vendorId;
    private String vendorCompanyName;
    private Long purchaseOrderId;
    private String poNumber;
    private Double deliveryRating;
    private Double qualityRating;
    private Double priceCompetitiveness;
    private Double overallRating;
    private String comments;
    private LocalDateTime evaluationDate;
}
