package com.procurea.procurementsystem.service.impl;

import com.procurea.procurementsystem.dto.PurchaseOrderDto;
import com.procurea.procurementsystem.entity.PurchaseOrder;
import com.procurea.procurementsystem.entity.Quotation;
import com.procurea.procurementsystem.entity.Vendor;
import com.procurea.procurementsystem.exception.BadRequestException;
import com.procurea.procurementsystem.exception.ResourceNotFoundException;
import com.procurea.procurementsystem.repository.PurchaseOrderRepository;
import com.procurea.procurementsystem.repository.QuotationRepository;
import com.procurea.procurementsystem.service.AuditLogService;
import com.procurea.procurementsystem.service.NotificationService;
import com.procurea.procurementsystem.service.PurchaseOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PurchaseOrderServiceImpl implements PurchaseOrderService {

    @Autowired
    private PurchaseOrderRepository poRepository;

    @Autowired
    private QuotationRepository quotationRepository;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private NotificationService notificationService;

    @Override
    @Transactional
    public PurchaseOrderDto createPurchaseOrder(Long quotationId, String deliveryAddress, LocalDateTime expectedDeliveryDate, String termsAndConditions) {
        Quotation quotation = quotationRepository.findById(quotationId)
                .orElseThrow(() -> new ResourceNotFoundException("Quotation not found"));

        if (quotation.getStatus() == Quotation.QuotationStatus.ACCEPTED) {
            throw new BadRequestException("Purchase Order has already been generated for this quotation");
        }

        // Accept quotation
        quotation.setStatus(Quotation.QuotationStatus.ACCEPTED);
        quotationRepository.save(quotation);

        PurchaseOrder po = new PurchaseOrder();
        po.setQuotation(quotation);
        po.setPoNumber("PO-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        po.setDeliveryAddress(deliveryAddress);
        po.setExpectedDeliveryDate(expectedDeliveryDate);
        po.setTermsAndConditions(termsAndConditions);
        po.setStatus(PurchaseOrder.POStatus.CREATED);

        PurchaseOrder saved = poRepository.save(po);

        auditLogService.log("SYSTEM", "CREATE_PURCHASE_ORDER", "PurchaseOrder", saved.getId(), null, saved.getPoNumber());

        // Notify vendor
        Vendor vendor = quotation.getVendor();
        if (vendor != null && vendor.getUser() != null) {
            notificationService.sendNotification(
                    vendor.getUser().getId(),
                    "A new Purchase Order has been generated: " + saved.getPoNumber() + ". Please acknowledge it.",
                    "PO"
            );
        }

        return convertToDto(saved);
    }

    @Override
    public PurchaseOrderDto getPurchaseOrderById(Long id) {
        PurchaseOrder po = poRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase Order not found with id: " + id));
        return convertToDto(po);
    }

    @Override
    public Page<PurchaseOrderDto> getAllPurchaseOrders(PurchaseOrder.POStatus status, String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("issuedDate").descending());
        Page<PurchaseOrder> pos = poRepository.searchPurchaseOrders(status, search, pageable);
        return pos.map(this::convertToDto);
    }

    @Override
    public Page<PurchaseOrderDto> getPurchaseOrdersForVendor(Long vendorId, PurchaseOrder.POStatus status, String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("issuedDate").descending());
        Page<PurchaseOrder> pos = poRepository.searchPurchaseOrdersForVendor(vendorId, status, search, pageable);
        return pos.map(this::convertToDto);
    }

    @Override
    @Transactional
    public PurchaseOrderDto updatePOStatus(Long id, PurchaseOrder.POStatus status) {
        PurchaseOrder po = poRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase Order not found"));

        String oldStatus = po.getStatus().name();
        po.setStatus(status);
        PurchaseOrder updated = poRepository.save(po);

        auditLogService.log("SYSTEM", "UPDATE_PO_STATUS", "PurchaseOrder", updated.getId(), oldStatus, status.name());

        // Notify vendor and manager
        if (po.getQuotation() != null && po.getQuotation().getVendor() != null && po.getQuotation().getVendor().getUser() != null) {
            notificationService.sendNotification(
                    po.getQuotation().getVendor().getUser().getId(),
                    "Your Purchase Order " + po.getPoNumber() + " status is now: " + status.name(),
                    "PO"
            );
        }

        return convertToDto(updated);
    }

    @Override
    @Transactional
    public PurchaseOrderDto acknowledgePO(Long id, Long vendorId) {
        PurchaseOrder po = poRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase Order not found"));

        if (!po.getQuotation().getVendor().getId().equals(vendorId)) {
            throw new BadRequestException("You are not authorized to acknowledge this Purchase Order");
        }

        if (po.getStatus() != PurchaseOrder.POStatus.CREATED && po.getStatus() != PurchaseOrder.POStatus.ISSUED) {
            throw new BadRequestException("Purchase Order cannot be acknowledged in this state: " + po.getStatus());
        }

        po.setStatus(PurchaseOrder.POStatus.ACKNOWLEDGED);
        PurchaseOrder updated = poRepository.save(po);

        auditLogService.log("SYSTEM", "ACKNOWLEDGE_PO", "PurchaseOrder", updated.getId(), "CREATED", "ACKNOWLEDGED");

        // Notify managers
        if (po.getQuotation() != null && po.getQuotation().getRfq() != null && po.getQuotation().getRfq().getPurchaseRequest() != null) {
            User requester = po.getQuotation().getRfq().getPurchaseRequest().getRequestedBy();
            if (requester != null) {
                notificationService.sendNotification(
                        requester.getId(),
                        "Purchase Order " + po.getPoNumber() + " has been acknowledged by vendor: " + po.getQuotation().getVendor().getCompanyName(),
                        "PO"
                );
            }
        }

        return convertToDto(updated);
    }

    private PurchaseOrderDto convertToDto(PurchaseOrder po) {
        PurchaseOrderDto dto = new PurchaseOrderDto();
        dto.setId(po.getId());
        if (po.getQuotation() != null) {
            dto.setQuotationId(po.getQuotation().getId());
            dto.setTotalAmount(po.getQuotation().getTotalAmount());
            if (po.getQuotation().getVendor() != null) {
                dto.setVendorId(po.getQuotation().getVendor().getId());
                dto.setVendorCompanyName(po.getQuotation().getVendor().getCompanyName());
            }
            if (po.getQuotation().getRfq() != null && po.getQuotation().getRfq().getPurchaseRequest() != null) {
                dto.setPurchaseRequestTitle(po.getQuotation().getRfq().getPurchaseRequest().getTitle());
            }
        }
        dto.setPoNumber(po.getPoNumber());
        dto.setIssuedDate(po.getIssuedDate());
        dto.setStatus(po.getStatus().name());
        dto.setDeliveryAddress(po.getDeliveryAddress());
        dto.setExpectedDeliveryDate(po.getExpectedDeliveryDate());
        dto.setTermsAndConditions(po.getTermsAndConditions());
        return dto;
    }
}
