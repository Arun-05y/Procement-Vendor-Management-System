package com.procurea.procurementsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsSummaryDto {
    private Double totalSpend;
    private Double totalSavings;
    private Double avgLeadTime; // in days
    private Map<String, Double> spendByDepartment;
    private Map<String, Double> costTrends;
    private Long vendorCount;
    private Long rfqCount;
    private Long poCount;
}
