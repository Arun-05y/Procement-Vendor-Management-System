package com.procurea.procurementsystem.service.impl;

import com.procurea.procurementsystem.dto.*;
import com.procurea.procurementsystem.entity.*;
import com.procurea.procurementsystem.repository.*;
import com.procurea.procurementsystem.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;
import java.util.*;
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

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Override
    public Map<String, Double> getCostTrends() {
        List<PurchaseOrder> pos = poRepository.findAll();
        Map<String, Double> monthly = new LinkedHashMap<>();

        // Initialize last 6 months
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        for (String m : months) {
            monthly.put(m, 0.0);
        }

        for (PurchaseOrder po : pos) {
            if (po.getOrderDate() != null && po.getGrandTotal() != null) {
                String monthName = po.getOrderDate().getMonth().toString().substring(0, 3);
                monthName = monthName.substring(0, 1).toUpperCase() + monthName.substring(1).toLowerCase();
                monthly.put(monthName, monthly.getOrDefault(monthName, 0.0) + po.getGrandTotal());
            }
        }

        return monthly;
    }

    @Override
    public AnalyticsSummaryDto getAnalyticsSummary() {
        List<Quotation> allQuotations = quotationRepository.findAll();
        List<Quotation> acceptedQuotations = allQuotations.stream()
                .filter(q -> q.getStatus() == Quotation.QuotationStatus.ACCEPTED)
                .collect(Collectors.toList());

        Double totalSpend = acceptedQuotations.stream()
                .mapToDouble(Quotation::getTotalAmount)
                .sum();

        double totalSavings = 0.0;
        List<RFQ> rfqs = rfqRepository.findAll();
        for (RFQ rfq : rfqs) {
            List<Quotation> rfqQuotations = quotationRepository.findByRfqId(rfq.getId());
            if (!rfqQuotations.isEmpty()) {
                double maxAmount = rfqQuotations.stream().mapToDouble(Quotation::getTotalAmount).max().orElse(0.0);
                double acceptedAmount = rfqQuotations.stream()
                        .filter(q -> q.getStatus() == Quotation.QuotationStatus.ACCEPTED)
                        .mapToDouble(Quotation::getTotalAmount)
                        .findFirst().orElse(0.0);
                if (acceptedAmount > 0.0) {
                    totalSavings += (maxAmount - acceptedAmount);
                }
            }
        }

        List<Delivery> deliveredShipments = deliveryRepository.findAll().stream()
                .filter(d -> d.getStatus() == Delivery.DeliveryStatus.DELIVERED && d.getDeliveryDate() != null && d.getPurchaseOrder() != null)
                .collect(Collectors.toList());

        double avgLeadTime = 0.0;
        if (!deliveredShipments.isEmpty()) {
            double totalLeadTimeDays = 0.0;
            int count = 0;
            for (Delivery delivery : deliveredShipments) {
                if (delivery.getPurchaseOrder().getOrderDate() != null) {
                    totalLeadTimeDays += ChronoUnit.DAYS.between(
                            delivery.getPurchaseOrder().getOrderDate(),
                            delivery.getDeliveryDate()
                    );
                    count++;
                }
            }
            if (count > 0) avgLeadTime = totalLeadTimeDays / count;
        }

        Map<String, Double> spendByDept = acceptedQuotations.stream()
                .filter(q -> q.getRfq() != null && q.getRfq().getPurchaseRequest() != null && q.getRfq().getPurchaseRequest().getDepartment() != null)
                .collect(Collectors.groupingBy(
                        q -> q.getRfq().getPurchaseRequest().getDepartment(),
                        Collectors.summingDouble(Quotation::getTotalAmount)
                ));

        Map<String, Double> trends = getCostTrends();
        long vendorCount = vendorRepository.count();
        long rfqCount = rfqRepository.count();
        long poCount = poRepository.count();

        return new AnalyticsSummaryDto(totalSpend, totalSavings, avgLeadTime, spendByDept, trends, vendorCount, rfqCount, poCount);
    }

    @Override
    public DashboardStatsDto getDashboardStats() {
        DashboardStatsDto dto = new DashboardStatsDto();

        List<Vendor> vendors = vendorRepository.findAll();
        dto.setTotalVendors(vendors.size());
        dto.setActiveVendors(vendors.stream().filter(v -> v.getStatus() == Vendor.VendorStatus.ACTIVE).count());

        List<PurchaseOrder> pos = poRepository.findAll();
        dto.setTotalPurchaseOrders(pos.size());
        dto.setPendingPurchaseOrders(pos.stream().filter(p -> p.getStatus() == PurchaseOrder.POStatus.PENDING || p.getStatus() == PurchaseOrder.POStatus.DRAFT).count());
        dto.setCompletedOrders(pos.stream().filter(p -> p.getStatus() == PurchaseOrder.POStatus.DELIVERED).count());

        double totalSpend = pos.stream().mapToDouble(p -> p.getGrandTotal() != null ? p.getGrandTotal() : 0.0).sum();
        dto.setTotalProcurementAmount(totalSpend);

        Double pendingAmount = invoiceRepository.sumPendingInvoiceAmount();
        dto.setPendingPaymentsAmount(pendingAmount != null ? pendingAmount : 0.0);

        dto.setLowStockItemsCount(productRepository.countLowStockProducts());

        dto.setAverageVendorRating(vendors.stream().mapToDouble(v -> v.getRating() != null ? v.getRating() : 0.0).average().orElse(0.0));
        dto.setAveragePerformanceScore(vendors.stream().mapToDouble(v -> v.getPerformanceScore() != null ? v.getPerformanceScore() : 0.0).average().orElse(0.0));

        // Charts
        dto.setMonthlySpending(getCostTrends());

        // Vendor-wise spending
        Map<String, Double> vendorSpend = pos.stream()
                .filter(p -> p.getVendor() != null && p.getGrandTotal() != null)
                .collect(Collectors.groupingBy(
                        p -> p.getVendor().getCompanyName(),
                        Collectors.summingDouble(PurchaseOrder::getGrandTotal)
                ));
        dto.setVendorWiseSpending(vendorSpend);

        // PO Status distribution
        Map<String, Long> statusDist = pos.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getStatus().name(),
                        Collectors.counting()
                ));
        dto.setPoStatusDistribution(statusDist);

        // Category spending
        Map<String, Double> catSpend = pos.stream()
                .filter(p -> p.getVendor() != null && p.getVendor().getCategory() != null && p.getGrandTotal() != null)
                .collect(Collectors.groupingBy(
                        p -> p.getVendor().getCategory(),
                        Collectors.summingDouble(PurchaseOrder::getGrandTotal)
                ));
        dto.setCategorySpending(catSpend);

        // Top 5 vendors
        List<VendorDto> topVendors = vendors.stream()
                .sorted((v1, v2) -> Double.compare(
                        v2.getPerformanceScore() != null ? v2.getPerformanceScore() : 0.0,
                        v1.getPerformanceScore() != null ? v1.getPerformanceScore() : 0.0))
                .limit(5)
                .map(v -> {
                    VendorDto vdto = new VendorDto();
                    vdto.setId(v.getId());
                    vdto.setVendorCode(v.getVendorCode());
                    vdto.setCompanyName(v.getCompanyName());
                    vdto.setCategory(v.getCategory());
                    vdto.setRating(v.getRating());
                    vdto.setPerformanceScore(v.getPerformanceScore());
                    vdto.setStatus(v.getStatus().name());
                    return vdto;
                }).collect(Collectors.toList());
        dto.setTopVendors(topVendors);

        // Recent activities
        List<AuditLogDto> recentActivities = auditLogRepository.findAll(
                PageRequest.of(0, 10, Sort.by("timestamp").descending()))
                .getContent().stream()
                .map(al -> {
                    AuditLogDto aldto = new AuditLogDto();
                    aldto.setId(al.getId());
                    aldto.setUsername(al.getUsername());
                    aldto.setAction(al.getAction());
                    aldto.setModule(al.getModule());
                    aldto.setEntityName(al.getEntityName());
                    aldto.setEntityId(al.getEntityId());
                    aldto.setTimestamp(al.getTimestamp());
                    aldto.setDescription(al.getDescription() != null ? al.getDescription() : al.getAction() + " on " + al.getEntityName());
                    return aldto;
                }).collect(Collectors.toList());
        dto.setRecentActivities(recentActivities);

        return dto;
    }
}
