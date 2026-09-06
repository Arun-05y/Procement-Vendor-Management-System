package com.procurea.procurementsystem.service;

import com.procurea.procurementsystem.dto.AnalyticsSummaryDto;
import com.procurea.procurementsystem.dto.DashboardStatsDto;

import java.util.Map;

public interface AnalyticsService {
    Map<String, Double> getCostTrends();
    AnalyticsSummaryDto getAnalyticsSummary();
    DashboardStatsDto getDashboardStats();
}
