package com.procurea.procurementsystem.service;

import com.procurea.procurementsystem.model.Quotation;
import com.procurea.procurementsystem.repository.QuotationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {
    @Autowired
    private QuotationRepository quotationRepository;

    public Map<String, Double> getCostTrends() {
        List<Quotation> acceptedQuotations = quotationRepository.findAll().stream()
                .filter(q -> q.getStatus() == Quotation.QuotationStatus.ACCEPTED)
                .collect(Collectors.toList());

        return acceptedQuotations.stream()
                .collect(Collectors.groupingBy(
                        q -> q.getSubmittedAt().getMonth().toString(),
                        Collectors.summingDouble(Quotation::getTotalAmount)
                ));
    }

    public Quotation recommendBestVendor(Long rfqId) {
        List<Quotation> quotations = quotationRepository.findByRfqId(rfqId);
        
        // Smart Logic: Score = (Price Score * 0.7) + (Vendor Rating * 0.3)
        // Price Score: Lower is better (normalized)
        if (quotations.isEmpty()) return null;

        double minPrice = quotations.stream().mapToDouble(Quotation::getTotalAmount).min().orElse(1.0);

        return quotations.stream().min((q1, q2) -> {
            double score1 = (minPrice / q1.getTotalAmount() * 70) + (q1.getVendor().getRating() * 6);
            double score2 = (minPrice / q2.getTotalAmount() * 70) + (q2.getVendor().getRating() * 6);
            return Double.compare(score2, score1); // Higher score is better
        }).orElse(null);
    }
}
