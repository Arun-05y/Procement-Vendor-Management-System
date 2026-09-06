package com.procurea.procurementsystem.service;

import com.procurea.procurementsystem.dto.QuotationDto;
import com.procurea.procurementsystem.entity.Quotation;

import java.util.List;

public interface QuotationService {
    QuotationDto submitQuotation(QuotationDto dto, Long vendorId);
    QuotationDto getQuotationById(Long id);
    List<QuotationDto> getQuotationsForRFQ(Long rfqId);
    List<QuotationDto> getQuotationsForVendor(Long vendorId);
    QuotationDto updateQuotationStatus(Long id, Quotation.QuotationStatus status);
}
