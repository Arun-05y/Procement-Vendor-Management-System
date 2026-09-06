package com.procurea.procurementsystem.service;

import com.procurea.procurementsystem.dto.AnalyticsSummaryDto;

import java.util.Map;

public interface AnalyticsService {
    AnalyticsSummaryDto getAnalyticsSummary();
    Map<String, Double> getCostTrends();
}
