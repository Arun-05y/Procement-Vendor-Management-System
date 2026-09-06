package com.procurea.procurementsystem.service.impl;

import com.procurea.procurementsystem.dto.AnalyticsSummaryDto;
import com.procurea.procurementsystem.entity.Delivery;
import com.procurea.procurementsystem.entity.Quotation;
import com.procurea.procurementsystem.entity.RFQ;
import com.procurea.procurementsystem.repository.DeliveryRepository;
import com.procurea.procurementsystem.repository.PurchaseOrderRepository;
import com.procurea.procurementsystem.repository.QuotationRepository;
import com.procurea.procurementsystem.repository.RFQRepository;
import com.procurea.procurementsystem.repository.VendorRepository;
import com.procurea.procurementsystem.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AnalyticsServiceImpl implements AnalyticsService {

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

    @Override
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

    @Override
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

        // 3. Average Lead Time (issuedDate to deliveryDate)
        List<Delivery> deliveredShipments = deliveryRepository.findAll().stream()
                .filter(d -> d.getStatus() == Delivery.DeliveryStatus.DELIVERED && d.getDeliveryDate() != null && d.getPurchaseOrder() != null)
                .collect(Collectors.toList());

        double avgLeadTime = 0.0;
        if (!deliveredShipments.isEmpty()) {
            double totalLeadTimeDays = 0.0;
            int count = 0;
            for (Delivery delivery : deliveredShipments) {
                if (delivery.getPurchaseOrder().getIssuedDate() != null) {
                    totalLeadTimeDays += ChronoUnit.DAYS.between(
                            delivery.getPurchaseOrder().getIssuedDate(),
                            delivery.getDeliveryDate()
                    );
                    count++;
                }
            }
            if (count > 0) {
                avgLeadTime = totalLeadTimeDays / count;
            }
        }

        // 4. Spend by Department
        Map<String, Double> spendByDept = acceptedQuotations.stream()
                .filter(q -> q.getRfq() != null && q.getRfq().getPurchaseRequest() != null && q.getRfq().getPurchaseRequest().getDepartment() != null)
                .collect(Collectors.groupingBy(
                        q -> q.getRfq().getPurchaseRequest().getDepartment(),
                        Collectors.summingDouble(Quotation::getTotalAmount)
                ));

        // 5. Cost Trends (by Month)
        Map<String, Double> trends = getCostTrends();
        
        // Ensure month keys are formatted nicely or default initialized
        Map<String, Double> formattedTrends = new HashMap<>();
        for (Map.Entry<String, Double> entry : trends.entrySet()) {
            formattedTrends.put(entry.getKey(), entry.getValue());
        }

        // 6. Counts
        long vendorCount = vendorRepository.count();
        long rfqCount = rfqRepository.count();
        long poCount = poRepository.count();

        return new AnalyticsSummaryDto(
                totalSpend,
                totalSavings,
                avgLeadTime,
                spendByDept,
                formattedTrends,
                vendorCount,
                rfqCount,
                poCount
        );
    }
}
