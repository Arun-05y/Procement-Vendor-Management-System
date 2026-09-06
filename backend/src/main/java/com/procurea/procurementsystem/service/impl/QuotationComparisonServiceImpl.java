package com.procurea.procurementsystem.service.impl;

import com.procurea.procurementsystem.dto.QuotationComparisonDto;
import com.procurea.procurementsystem.entity.Quotation;
import com.procurea.procurementsystem.entity.RFQ;
import com.procurea.procurementsystem.exception.ResourceNotFoundException;
import com.procurea.procurementsystem.repository.QuotationRepository;
import com.procurea.procurementsystem.repository.RFQRepository;
import com.procurea.procurementsystem.service.QuotationComparisonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class QuotationComparisonServiceImpl implements QuotationComparisonService {

    @Autowired
    private QuotationRepository quotationRepository;

    @Autowired
    private RFQRepository rfqRepository;

    @Override
    public QuotationComparisonDto compareQuotations(Long rfqId) {
        RFQ rfq = rfqRepository.findById(rfqId)
                .orElseThrow(() -> new ResourceNotFoundException("RFQ not found with id: " + rfqId));

        List<Quotation> quotations = quotationRepository.findByRfqId(rfqId);
        
        QuotationComparisonDto comparisonDto = new QuotationComparisonDto();
        comparisonDto.setRfqId(rfq.getId());
        comparisonDto.setRfqTitle(rfq.getTitle());
        if (rfq.getPurchaseRequest() != null) {
            comparisonDto.setEstimatedBudget(rfq.getPurchaseRequest().getEstimatedBudget());
        } else {
            comparisonDto.setEstimatedBudget(0.0);
        }

        if (quotations.isEmpty()) {
            comparisonDto.setQuotations(new ArrayList<>());
            comparisonDto.setRecommendedVendorId(null);
            comparisonDto.setRecommendedVendorName("None");
            comparisonDto.setRecommendationReason("No quotations submitted yet.");
            return comparisonDto;
        }

        // Find min price and min delivery days for normalization
        double minPrice = quotations.stream()
                .mapToDouble(Quotation::getTotalAmount)
                .min()
                .orElse(1.0);

        int minDeliveryDays = quotations.stream()
                .mapToInt(Quotation::getDeliveryDays)
                .min()
                .orElse(1);

        List<QuotationComparisonDto.QuotationComparisonItem> comparisonItems = new ArrayList<>();
        QuotationComparisonDto.QuotationComparisonItem bestItem = null;
        double highestScore = -1.0;

        for (Quotation q : quotations) {
            QuotationComparisonDto.QuotationComparisonItem item = new QuotationComparisonDto.QuotationComparisonItem();
            item.setQuotationId(q.getId());
            if (q.getVendor() != null) {
                item.setVendorId(q.getVendor().getId());
                item.setVendorCompanyName(q.getVendor().getCompanyName());
                item.setQualityRating(q.getVendor().getRating());
                item.setPreviousPerformanceScore(q.getVendor().getPerformanceScore());
            } else {
                item.setVendorId(null);
                item.setVendorCompanyName("Unknown");
                item.setQualityRating(0.0);
                item.setPreviousPerformanceScore(0.0);
            }
            item.setTotalAmount(q.getTotalAmount());
            item.setDeliveryDays(q.getDeliveryDays());
            item.setWarrantyMonths(q.getWarrantyMonths());

            // Compute normalized subscores
            // Price: Lower is better. MinPrice/QPrice * 100
            double priceScore = (minPrice / q.getTotalAmount()) * 100.0;
            // Delivery: Lower is better. MinDays/QDays * 100
            double deliveryDaysScore = ((double) minDeliveryDays / q.getDeliveryDays()) * 100.0;
            // Quality: vendor rating scaled (1-5 to 1-100)
            double qualityScore = item.getQualityRating() * 20.0;
            // Vendor previous performance score (already 0-100)
            double ratingScore = item.getPreviousPerformanceScore() == null ? 0.0 : item.getPreviousPerformanceScore();

            item.setPriceScore(priceScore);
            item.setDeliveryDaysScore(deliveryDaysScore);
            item.setQualityScore(qualityScore);
            item.setVendorRatingScore(ratingScore);

            // Compute overall weighted score
            // Price (40%), Delivery (25%), Quality (20%), Rating (15%)
            double overallScore = (priceScore * 0.40) + (deliveryDaysScore * 0.25) + (qualityScore * 0.20) + (ratingScore * 0.15);
            item.setOverallScore(overallScore);

            comparisonItems.add(item);

            if (overallScore > highestScore) {
                highestScore = overallScore;
                bestItem = item;
            }
        }

        comparisonDto.setQuotations(comparisonItems);

        if (bestItem != null) {
            comparisonDto.setRecommendedVendorId(bestItem.getVendorId());
            comparisonDto.setRecommendedVendorName(bestItem.getVendorCompanyName());
            
            // Format reason
            String reason = String.format(
                "Vendor %s is recommended because it provides the best combined overall score of %.2f%%. " +
                "They offered a price of ₹%,.2f (Price Score: %.1f%%) and delivery in %d days (Delivery Score: %.1f%%), " +
                "backed by a vendor quality rating of %.1f/5 and past performance score of %.1f%%.",
                bestItem.getVendorCompanyName(),
                bestItem.getOverallScore(),
                bestItem.getTotalAmount(),
                bestItem.getPriceScore(),
                bestItem.getDeliveryDays(),
                bestItem.getDeliveryDaysScore(),
                bestItem.getQualityRating(),
                bestItem.getPreviousPerformanceScore()
            );
            comparisonDto.setRecommendationReason(reason);
        }

        return comparisonDto;
    }
}
