package com.procurea.procurementsystem.service.impl;

import com.procurea.procurementsystem.dto.RFQDto;
import com.procurea.procurementsystem.entity.PurchaseRequest;
import com.procurea.procurementsystem.entity.RFQ;
import com.procurea.procurementsystem.entity.Vendor;
import com.procurea.procurementsystem.exception.BadRequestException;
import com.procurea.procurementsystem.exception.ResourceNotFoundException;
import com.procurea.procurementsystem.repository.PurchaseRequestRepository;
import com.procurea.procurementsystem.repository.RFQRepository;
import com.procurea.procurementsystem.repository.VendorRepository;
import com.procurea.procurementsystem.service.AuditLogService;
import com.procurea.procurementsystem.service.NotificationService;
import com.procurea.procurementsystem.service.RFQService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RFQServiceImpl implements RFQService {

    @Autowired
    private RFQRepository rfqRepository;

    @Autowired
    private PurchaseRequestRepository prRepository;

    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private NotificationService notificationService;

    @Override
    @Transactional
    public RFQDto createRFQ(RFQDto dto) {
        PurchaseRequest pr = prRepository.findById(dto.getPurchaseRequestId())
                .orElseThrow(() -> new ResourceNotFoundException("Purchase request not found"));

        if (pr.getStatus() != PurchaseRequest.RequestStatus.APPROVED) {
            throw new BadRequestException("Purchase request must be APPROVED to generate an RFQ");
        }

        RFQ rfq = new RFQ();
        rfq.setPurchaseRequest(pr);
        rfq.setTitle(dto.getTitle() == null ? "RFQ for " + pr.getTitle() : dto.getTitle());
        rfq.setDescription(dto.getDescription() == null ? pr.getDescription() : dto.getDescription());
        rfq.setDeadline(dto.getDeadline());
        rfq.setStatus(RFQ.RFQStatus.DRAFT);

        if (dto.getInvitedVendorIds() != null && !dto.getInvitedVendorIds().isEmpty()) {
            Set<Vendor> vendors = new HashSet<>(vendorRepository.findAllById(dto.getInvitedVendorIds()));
            rfq.setInvitedVendors(vendors);
        }

        RFQ saved = rfqRepository.save(rfq);

        auditLogService.log("SYSTEM", "CREATE_RFQ", "RFQ", saved.getId(), null, saved.getTitle());

        return convertToDto(saved);
    }

    @Override
    public RFQDto getRFQById(Long id) {
        RFQ rfq = rfqRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RFQ not found with id: " + id));
        return convertToDto(rfq);
    }

    @Override
    public Page<RFQDto> getAllRFQs(RFQ.RFQStatus status, String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<RFQ> rfqs = rfqRepository.searchRFQs(status, search, pageable);
        return rfqs.map(this::convertToDto);
    }

    @Override
    public Page<RFQDto> getRFQsForVendor(Long vendorId, RFQ.RFQStatus status, String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<RFQ> rfqs = rfqRepository.searchRFQsForVendor(vendorId, status, search, pageable);
        return rfqs.map(this::convertToDto);
    }

    @Override
    @Transactional
    public RFQDto updateRFQStatus(Long id, RFQ.RFQStatus status) {
        RFQ rfq = rfqRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RFQ not found with id: " + id));

        String oldStatus = rfq.getStatus().name();
        rfq.setStatus(status);
        RFQ updated = rfqRepository.save(rfq);

        auditLogService.log("SYSTEM", "UPDATE_RFQ_STATUS", "RFQ", updated.getId(), oldStatus, status.name());

        if (status == RFQ.RFQStatus.OPEN || status == RFQ.RFQStatus.SENT) {
            // Notify invited vendors
            for (Vendor vendor : rfq.getInvitedVendors()) {
                if (vendor.getUser() != null) {
                    notificationService.sendNotification(
                            vendor.getUser().getId(),
                            "You have been invited to submit a bid for RFQ: '" + rfq.getTitle() + "' (Deadline: " + rfq.getDeadline() + ")",
                            "RFQ"
                    );
                }
            }
        }

        return convertToDto(updated);
    }

    @Override
    @Transactional
    public RFQDto inviteVendors(Long id, Set<Long> vendorIds) {
        RFQ rfq = rfqRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RFQ not found"));

        Set<Vendor> vendors = new HashSet<>(vendorRepository.findAllById(vendorIds));
        rfq.getInvitedVendors().addAll(vendors);
        RFQ updated = rfqRepository.save(rfq);

        auditLogService.log("SYSTEM", "INVITE_VENDORS_RFQ", "RFQ", updated.getId(), null, vendorIds.toString());

        if (updated.getStatus() == RFQ.RFQStatus.OPEN || updated.getStatus() == RFQ.RFQStatus.SENT) {
            for (Vendor v : vendors) {
                if (v.getUser() != null) {
                    notificationService.sendNotification(
                            v.getUser().getId(),
                            "You have been invited to submit a bid for RFQ: '" + rfq.getTitle() + "' (Deadline: " + rfq.getDeadline() + ")",
                            "RFQ"
                    );
                }
            }
        }

        return convertToDto(updated);
    }

    private RFQDto convertToDto(RFQ rfq) {
        RFQDto dto = new RFQDto();
        dto.setId(rfq.getId());
        if (rfq.getPurchaseRequest() != null) {
            dto.setPurchaseRequestId(rfq.getPurchaseRequest().getId());
            dto.setPurchaseRequestTitle(rfq.getPurchaseRequest().getTitle());
        }
        dto.setTitle(rfq.getTitle());
        dto.setDescription(rfq.getDescription());
        dto.setDeadline(rfq.getDeadline());
        dto.setStatus(rfq.getStatus().name());
        dto.setInvitedVendorIds(rfq.getInvitedVendors().stream().map(Vendor::getId).collect(Collectors.toSet()));
        dto.setInvitedVendorNames(rfq.getInvitedVendors().stream().map(Vendor::getCompanyName).collect(Collectors.toSet()));
        dto.setCreatedAt(rfq.getCreatedAt());
        return dto;
    }
}
