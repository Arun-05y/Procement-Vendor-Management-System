package com.procurea.procurementsystem.service.impl;

import com.procurea.procurementsystem.dto.QuotationDto;
import com.procurea.procurementsystem.entity.Quotation;
import com.procurea.procurementsystem.entity.RFQ;
import com.procurea.procurementsystem.entity.User;
import com.procurea.procurementsystem.entity.Vendor;
import com.procurea.procurementsystem.exception.BadRequestException;
import com.procurea.procurementsystem.exception.ResourceNotFoundException;
import com.procurea.procurementsystem.repository.QuotationRepository;
import com.procurea.procurementsystem.repository.RFQRepository;
import com.procurea.procurementsystem.repository.UserRepository;
import com.procurea.procurementsystem.repository.VendorRepository;
import com.procurea.procurementsystem.service.AuditLogService;
import com.procurea.procurementsystem.service.NotificationService;
import com.procurea.procurementsystem.service.QuotationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuotationServiceImpl implements QuotationService {

    @Autowired
    private QuotationRepository quotationRepository;

    @Autowired
    private RFQRepository rfqRepository;

    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private NotificationService notificationService;

    @Override
    @Transactional
    public QuotationDto submitQuotation(QuotationDto dto, Long vendorId) {
        RFQ rfq = rfqRepository.findById(dto.getRfqId())
                .orElseThrow(() -> new ResourceNotFoundException("RFQ not found"));

        if (rfq.getStatus() == RFQ.RFQStatus.CLOSED) {
            throw new BadRequestException("Cannot submit quotation for a CLOSED RFQ");
        }

        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor profile not found"));

        Quotation quotation = new Quotation();
        quotation.setRfq(rfq);
        quotation.setVendor(vendor);
        quotation.setTotalAmount(dto.getTotalAmount());
        quotation.setDeliveryDays(dto.getDeliveryDays());
        quotation.setQualityRating(vendor.getQualityRating()); // Pull from current vendor record
        quotation.setWarrantyMonths(dto.getWarrantyMonths() == null ? 12 : dto.getWarrantyMonths());
        quotation.setPreviousPerformanceScore(vendor.getPerformanceScore()); // Pull from vendor record
        quotation.setTermsAndConditions(dto.getTermsAndConditions());
        quotation.setStatus(Quotation.QuotationStatus.SUBMITTED);

        Quotation saved = quotationRepository.save(quotation);

        String username = (vendor.getUser() != null) ? vendor.getUser().getUsername() : "VENDOR-" + vendorId;
        auditLogService.log(username, "SUBMIT_QUOTATION", "Quotation", saved.getId(), null, saved.getTotalAmount().toString());

        // Notify Managers
        List<User> managers = userRepository.findAll().stream()
                .filter(u -> u.getRoles().stream().anyMatch(r -> r.getName() == com.procurea.procurementsystem.entity.Role.ERole.ROLE_PROCUREMENT_MANAGER))
                .collect(Collectors.toList());
        for (User manager : managers) {
            notificationService.sendNotification(
                    manager.getId(),
                    "Vendor " + vendor.getCompanyName() + " submitted a quotation for RFQ: " + rfq.getTitle(),
                    "RFQ"
            );
        }

        return convertToDto(saved);
    }

    @Override
    public QuotationDto getQuotationById(Long id) {
        Quotation quotation = quotationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quotation not found with id: " + id));
        return convertToDto(quotation);
    }

    @Override
    public List<QuotationDto> getQuotationsForRFQ(Long rfqId) {
        return quotationRepository.findByRfqId(rfqId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<QuotationDto> getQuotationsForVendor(Long vendorId) {
        return quotationRepository.findByVendorId(vendorId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public QuotationDto updateQuotationStatus(Long id, Quotation.QuotationStatus status) {
        Quotation quotation = quotationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quotation not found with id: " + id));

        String oldStatus = quotation.getStatus().name();
        quotation.setStatus(status);
        Quotation updated = quotationRepository.save(quotation);

        auditLogService.log("SYSTEM", "UPDATE_QUOTATION_STATUS", "Quotation", updated.getId(), oldStatus, status.name());

        // Notify vendor user
        if (quotation.getVendor() != null && quotation.getVendor().getUser() != null) {
            notificationService.sendNotification(
                    quotation.getVendor().getUser().getId(),
                    "Your quotation for RFQ '" + quotation.getRfq().getTitle() + "' has been " + status.name() + ".",
                    "RFQ"
            );
        }

        return convertToDto(updated);
    }

    private QuotationDto convertToDto(Quotation quotation) {
        QuotationDto dto = new QuotationDto();
        dto.setId(quotation.getId());
        if (quotation.getRfq() != null) {
            dto.setRfqId(quotation.getRfq().getId());
            dto.setRfqTitle(quotation.getRfq().getTitle());
        }
        if (quotation.getVendor() != null) {
            dto.setVendorId(quotation.getVendor().getId());
            dto.setVendorCompanyName(quotation.getVendor().getCompanyName());
        }
        dto.setTotalAmount(quotation.getTotalAmount());
        dto.setDeliveryDays(quotation.getDeliveryDays());
        dto.setQualityRating(quotation.getQualityRating());
        dto.setWarrantyMonths(quotation.getWarrantyMonths());
        dto.setPreviousPerformanceScore(quotation.getPreviousPerformanceScore());
        dto.setTermsAndConditions(quotation.getTermsAndConditions());
        dto.setStatus(quotation.getStatus().name());
        dto.setSubmittedAt(quotation.getSubmittedAt());
        return dto;
    }
}
