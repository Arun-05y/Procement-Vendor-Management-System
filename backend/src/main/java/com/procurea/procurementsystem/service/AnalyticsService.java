package com.procurea.procurementsystem.service;

import com.procurea.procurementsystem.dto.AnalyticsSummaryDto;
import com.procurea.procurementsystem.model.Delivery;
import com.procurea.procurementsystem.model.Quotation;
import com.procurea.procurementsystem.model.RFQ;
import com.procurea.procurementsystem.repository.DeliveryRepository;
import com.procurea.procurementsystem.repository.QuotationRepository;
import com.procurea.procurementsystem.repository.RFQRepository;
import com.procurea.procurementsystem.repository.VendorRepository;
import com.procurea.procurementsystem.repository.PurchaseOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {
    @Autowired
    private QuotationRepository quotationRepository;

    @Autowired
    private RFQRepository rfqRepository;

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private PurchaseOrderRepository poRepository;

    public Map<String, Double> getCostTrends() {
        List<Quotation> acceptedQuotations = quotationRepository.findAll().stream()
                .filter(q -> q.getStatus() == Quotation.QuotationStatus.ACCEPTED)
                .collect(Collectors.toList());

        return acceptedQuotations.stream()
                .filter(q -> q.getSubmittedAt() != null)
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

    public AnalyticsSummaryDto getAnalyticsSummary() {
        List<Quotation> allQuotations = quotationRepository.findAll();
        List<Quotation> acceptedQuotations = allQuotations.stream()
                .filter(q -> q.getStatus() == Quotation.QuotationStatus.ACCEPTED)
                .collect(Collectors.toList());

        // 1. Total Spend
        Double totalSpend = acceptedQuotations.stream()
                .mapToDouble(Quotation::getTotalAmount)
                .sum();

        // 2. Total Savings
        // Savings = sum of (max quotation price - accepted quotation price) for each RFQ
        double totalSavings = 0.0;
        List<RFQ> rfqs = rfqRepository.findAll();
        for (RFQ rfq : rfqs) {
            List<Quotation> rfqQuotations = quotationRepository.findByRfqId(rfq.getId());
            if (!rfqQuotations.isEmpty()) {
                double maxAmount = rfqQuotations.stream()
                        .mapToDouble(Quotation::getTotalAmount)
                        .max()
                        .orElse(0.0);
                
                double acceptedAmount = rfqQuotations.stream()
                        .filter(q -> q.getStatus() == Quotation.QuotationStatus.ACCEPTED)
                        .mapToDouble(Quotation::getTotalAmount)
                        .findFirst()
                        .orElse(0.0);
                
                if (acceptedAmount > 0.0) {
                    totalSavings += (maxAmount - acceptedAmount);
                }
            }
        }

        // 3. Average Lead Time
        List<Delivery> deliveredShipments = deliveryRepository.findAll().stream()
                .filter(d -> d.getStatus() == Delivery.DeliveryStatus.DELIVERED && d.getDeliveryDate() != null && d.getPurchaseOrder() != null)
                .collect(Collectors.toList());

        double avgLeadTime = 0.0;
        if (!deliveredShipments.isEmpty()) {
            double totalLeadTimeDays = 0.0;
            for (Delivery delivery : deliveredShipments) {
                if (delivery.getPurchaseOrder().getIssuedDate() != null) {
                    totalLeadTimeDays += ChronoUnit.DAYS.between(
                            delivery.getPurchaseOrder().getIssuedDate(),
                            delivery.getDeliveryDate()
                    );
                }
            }
            avgLeadTime = totalLeadTimeDays / deliveredShipments.size();
        }

        // 4. Spend by Department
        Map<String, Double> spendByDept = acceptedQuotations.stream()
                .filter(q -> q.getRfq() != null && q.getRfq().getRequest() != null && q.getRfq().getRequest().getDepartment() != null)
                .collect(Collectors.groupingBy(
                        q -> q.getRfq().getRequest().getDepartment(),
                        Collectors.summingDouble(Quotation::getTotalAmount)
                ));

        // 5. Cost Trends (by Month)
        Map<String, Double> trends = getCostTrends();

        // 6. Counts
        long vendorCount = vendorRepository.count();
        long rfqCount = rfqRepository.count();
        long poCount = poRepository.count();

        return new AnalyticsSummaryDto(totalSpend, totalSavings, avgLeadTime, spendByDept, trends, vendorCount, rfqCount, poCount);
    }
}
