package com.procurea.procurementsystem.controller;

import com.procurea.procurementsystem.model.Quotation;
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

    @GetMapping("/cost-trends")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Double> getCostTrends() {
        return analyticsService.getCostTrends();
    }

    @GetMapping("/recommend/{rfqId}")
    @PreAuthorize("hasRole('PROCUREMENT_OFFICER')")
    public ResponseEntity<Quotation> getRecommendation(@PathVariable Long rfqId) {
        Quotation recommended = analyticsService.recommendBestVendor(rfqId);
        return recommended != null ? ResponseEntity.ok(recommended) : ResponseEntity.noContent().build();
    }
}
