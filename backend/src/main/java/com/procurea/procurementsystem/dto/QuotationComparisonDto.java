package com.procurea.procurementsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuotationComparisonDto {
    private Long rfqId;
    private String rfqTitle;
    private Double estimatedBudget;
    private List<QuotationComparisonItem> quotations;
    private Long recommendedVendorId;
    private String recommendedVendorName;
    private String recommendationReason;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuotationComparisonItem {
        private Long quotationId;
        private Long vendorId;
        private String vendorCompanyName;
        private Double totalAmount;
        private Integer deliveryDays;
        private Double qualityRating;
        private Integer warrantyMonths;
        private Double previousPerformanceScore;
        
        // Calculated scores
        private Double priceScore;          // 40%
        private Double deliveryDaysScore;   // 25%
        private Double qualityScore;        // 20%
        private Double vendorRatingScore;   // 15%
        private Double overallScore;        // Sum of weighted scores
    }
}
