package com.procurea.procurementsystem.service.impl;

import com.procurea.procurementsystem.dto.AIAssistantResponse;
import com.procurea.procurementsystem.entity.PurchaseOrder;
import com.procurea.procurementsystem.entity.Quotation;
import com.procurea.procurementsystem.entity.Vendor;
import com.procurea.procurementsystem.repository.PurchaseOrderRepository;
import com.procurea.procurementsystem.repository.QuotationRepository;
import com.procurea.procurementsystem.repository.VendorRepository;
import com.procurea.procurementsystem.service.AIAssistantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AIAssistantServiceImpl implements AIAssistantService {

    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private PurchaseOrderRepository poRepository;

    @Autowired
    private QuotationRepository quotationRepository;

    @Override
    public AIAssistantResponse askQuestion(String question) {
        String query = question.toLowerCase();
        
        // 1. Delivery performance
        if (query.contains("best delivery performance") || query.contains("best delivery")) {
            List<Vendor> best = vendorRepository.findAll().stream()
                    .filter(v -> v.getStatus() == Vendor.VendorStatus.ACTIVE)
                    .sorted((v1, v2) -> Double.compare(v2.getOnTimeDeliveryRate(), v1.getOnTimeDeliveryRate()))
                    .limit(3)
                    .collect(Collectors.toList());

            if (best.isEmpty()) {
                return new AIAssistantResponse("No active vendors found in database.", null);
            }

            Vendor top = best.get(0);
            String answer = String.format(
                "The vendor with the best delivery performance is **%s** (Code: %s) with an On-Time Delivery Rate of **%.1f%%** " +
                "and an overall performance score of **%.1f%%** (%s).",
                top.getCompanyName(), top.getVendorCode(), top.getOnTimeDeliveryRate(), top.getPerformanceScore(), top.getPerformanceCategory()
            );
            return new AIAssistantResponse(answer, best);
        }

        // 2. Poor performance vendors
        if (query.contains("poor performance") || query.contains("worst vendors") || query.contains("poor vendors")) {
            List<Vendor> poor = vendorRepository.findAll().stream()
                    .filter(v -> "POOR".equalsIgnoreCase(v.getPerformanceCategory()) || v.getPerformanceScore() < 50.0)
                    .collect(Collectors.toList());

            if (poor.isEmpty()) {
                return new AIAssistantResponse("All vendors are currently performing well! No vendors classified as 'POOR' (score < 50.0).", poor);
            }

            String listStr = poor.stream()
                    .map(v -> String.format("- **%s** (Score: %.1f%%, Category: %s, Delivery: %.1f%%)", v.getCompanyName(), v.getPerformanceScore(), v.getPerformanceCategory(), v.getOnTimeDeliveryRate()))
                    .collect(Collectors.joining("\n"));

            String answer = "The following vendors show poor performance:\n" + listStr;
            return new AIAssistantResponse(answer, poor);
        }

        // 3. Procurement spending
        if (query.contains("procurement spending") || query.contains("spend") || query.contains("total spend")) {
            List<Quotation> accepted = quotationRepository.findAll().stream()
                    .filter(q -> q.getStatus() == Quotation.QuotationStatus.ACCEPTED)
                    .collect(Collectors.toList());

            double total = accepted.stream().mapToDouble(Quotation::getTotalAmount).sum();
            
            String answer = String.format(
                "Total procurement spending based on accepted quotations is **₹%,.2f** across %d completed/approved orders.",
                total, accepted.size()
            );
            return new AIAssistantResponse(answer, accepted);
        }

        // 4. Delayed purchase orders
        if (query.contains("delayed") || query.contains("overdue") || query.contains("late")) {
            List<PurchaseOrder> delayed = poRepository.findAll().stream()
                    .filter(po -> po.getStatus() != PurchaseOrder.POStatus.DELIVERED &&
                            po.getStatus() != PurchaseOrder.POStatus.CANCELLED &&
                            po.getExpectedDeliveryDate() != null &&
                            po.getExpectedDeliveryDate().isBefore(LocalDateTime.now()))
                    .collect(Collectors.toList());

            if (delayed.isEmpty()) {
                return new AIAssistantResponse("Excellent! There are no delayed purchase orders in the system.", delayed);
            }

            String listStr = delayed.stream()
                    .map(po -> String.format("- **%s** (Vendor: %s, Expected: %s, Status: %s)", po.getPoNumber(), po.getQuotation().getVendor().getCompanyName(), po.getExpectedDeliveryDate().toString().replace("T", " "), po.getStatus().name()))
                    .collect(Collectors.joining("\n"));

            String answer = "The following purchase orders are currently delayed:\n" + listStr;
            return new AIAssistantResponse(answer, delayed);
        }

        // 5. Contracts expiring soon
        if (query.contains("contract expire") || query.contains("expiring") || query.contains("expired")) {
            LocalDate nextMonth = LocalDate.now().plusDays(30);
            List<Vendor> expiring = vendorRepository.findAll().stream()
                    .filter(v -> v.getContractEndDate() != null &&
                            (v.getContractEndDate().isBefore(nextMonth) || v.getContractEndDate().isEqual(nextMonth)))
                    .collect(Collectors.toList());

            if (expiring.isEmpty()) {
                return new AIAssistantResponse("No contracts are expiring within the next 30 days.", expiring);
            }

            String listStr = expiring.stream()
                    .map(v -> String.format("- **%s** (Contract Ends: %s, Status: %s)", v.getCompanyName(), v.getContractEndDate(), v.getStatus().name()))
                    .collect(Collectors.joining("\n"));

            String answer = "The following vendor contracts are expiring soon or have already expired:\n" + listStr;
            return new AIAssistantResponse(answer, expiring);
        }

        // 6. Compare Vendor A and Vendor B
        if (query.contains("compare")) {
            List<Vendor> all = vendorRepository.findAll();
            Vendor match1 = null;
            Vendor match2 = null;

            for (Vendor v : all) {
                if (query.contains(v.getCompanyName().toLowerCase())) {
                    if (match1 == null) {
                        match1 = v;
                    } else if (!match1.getId().equals(v.getId())) {
                        match2 = v;
                        break;
                    }
                }
            }

            if (match1 != null && match2 != null) {
                String answer = String.format(
                    "### Vendor Comparison:\n" +
                    "- **%s**:\n" +
                    "  - Vendor Code: %s\n" +
                    "  - Rating: %.1f ★\n" +
                    "  - Performance Score: %.1f%%\n" +
                    "  - On-Time Delivery: %.1f%%\n" +
                    "  - Fulfillment Rate: %.1f%%\n" +
                    "  - Category: %s\n" +
                    "- **%s**:\n" +
                    "  - Vendor Code: %s\n" +
                    "  - Rating: %.1f ★\n" +
                    "  - Performance Score: %.1f%%\n" +
                    "  - On-Time Delivery: %.1f%%\n" +
                    "  - Fulfillment Rate: %.1f%%\n" +
                    "  - Category: %s\n\n" +
                    "**Recommendation:** %s has the higher performance score (%.1f%% vs %.1f%%).",
                    match1.getCompanyName(), match1.getVendorCode(), match1.getRating(), match1.getPerformanceScore(), match1.getOnTimeDeliveryRate(), match1.getFulfillmentRate(), match1.getCategory(),
                    match2.getCompanyName(), match2.getVendorCode(), match2.getRating(), match2.getPerformanceScore(), match2.getOnTimeDeliveryRate(), match2.getFulfillmentRate(), match2.getCategory(),
                    match1.getPerformanceScore() > match2.getPerformanceScore() ? match1.getCompanyName() : match2.getCompanyName(),
                    Math.max(match1.getPerformanceScore(), match2.getPerformanceScore()),
                    Math.min(match1.getPerformanceScore(), match2.getPerformanceScore())
                );
                return new AIAssistantResponse(answer, List.of(match1, match2));
            } else if (match1 != null) {
                return new AIAssistantResponse("I found vendor **" + match1.getCompanyName() + "**, but please specify another vendor name to compare with.", match1);
            }
        }

        // Default response: help prompt
        String answer = "Hello! I am **ProcureAI**, your procurement assistant. I can query live database records to answer questions without hallucination.\n\n" +
                "Here are some examples of what you can ask me:\n" +
                "- *\"Which vendor has the best delivery performance?\"*\n" +
                "- *\"Show vendors with poor performance.\"*\n" +
                "- *\"What is our total procurement spend?\"*\n" +
                "- *\"Which purchase orders are delayed?\"*\n" +
                "- *\"Which contracts are expiring?\"*\n" +
                "- *\"Compare TechCorp Company and Global Supplies Inc\"* (uses company names)";
        return new AIAssistantResponse(answer, null);
    }
}
