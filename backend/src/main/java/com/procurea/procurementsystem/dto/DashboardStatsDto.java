package com.procurea.procurementsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDto {
    private long totalVendors;
    private long activeVendors;
    private long totalPurchaseOrders;
    private long pendingPurchaseOrders;
    private long completedOrders;
    private double totalProcurementAmount;
    private double pendingPaymentsAmount;
    private long lowStockItemsCount;
    private double averageVendorRating;
    private double averagePerformanceScore;

    // Charts data
    private Map<String, Double> monthlySpending;
    private Map<String, Double> vendorWiseSpending;
    private Map<String, Long> poStatusDistribution;
    private Map<String, Double> categorySpending;
    private List<VendorDto> topVendors;
    private List<AuditLogDto> recentActivities;
}
