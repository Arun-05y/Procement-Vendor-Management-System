package com.procurea.procurementsystem.service;

import com.procurea.procurementsystem.model.Quotation;
import com.procurea.procurementsystem.repository.QuotationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class QuotationService {
    @Autowired
    private QuotationRepository quotationRepository;

    public Quotation submitQuotation(Quotation quotation) {
        return quotationRepository.save(quotation);
    }

    public List<Quotation> getQuotationsByRFQ(Long rfqId) {
        return quotationRepository.findByRfqId(rfqId);
    }

    public Quotation getBestQuotation(Long rfqId) {
        List<Quotation> quotations = quotationRepository.findByRfqId(rfqId);
        return quotations.stream()
                .min(Comparator.comparing(Quotation::getTotalAmount))
                .orElse(null);
    }

    public Quotation updateQuotationStatus(Long id, Quotation.QuotationStatus status) {
        Quotation quotation = quotationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quotation not found"));
        quotation.setStatus(status);
        return quotationRepository.save(quotation);
    }
}
