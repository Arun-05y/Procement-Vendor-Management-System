package com.procurea.procurementsystem.controller;

import com.procurea.procurementsystem.dto.AnalyticsSummaryDto;
import com.procurea.procurementsystem.dto.ApiResponse;
import com.procurea.procurementsystem.dto.DashboardStatsDto;
import com.procurea.procurementsystem.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    @GetMapping("/dashboard")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<DashboardStatsDto>> getDashboardStats() {
        DashboardStatsDto stats = analyticsService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.success("Dashboard statistics fetched successfully", stats));
    }

    @GetMapping("/cost-trends")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, Double>>> getCostTrends() {
        Map<String, Double> trends = analyticsService.getCostTrends();
        return ResponseEntity.ok(ApiResponse.success("Cost trends fetched successfully", trends));
    }

    @GetMapping("/summary")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<AnalyticsSummaryDto>> getAnalyticsSummary() {
        AnalyticsSummaryDto summary = analyticsService.getAnalyticsSummary();
        return ResponseEntity.ok(ApiResponse.success("Analytics summary fetched successfully", summary));
    }
}
