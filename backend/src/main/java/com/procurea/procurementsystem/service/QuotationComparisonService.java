package com.procurea.procurementsystem.service;

import com.procurea.procurementsystem.dto.QuotationComparisonDto;

public interface QuotationComparisonService {
    QuotationComparisonDto compareQuotations(Long rfqId);
}
