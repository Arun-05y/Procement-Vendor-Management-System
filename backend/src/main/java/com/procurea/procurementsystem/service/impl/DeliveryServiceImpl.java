package com.procurea.procurementsystem.service.impl;

import com.procurea.procurementsystem.dto.DeliveryDto;
import com.procurea.procurementsystem.entity.Delivery;
import com.procurea.procurementsystem.entity.PurchaseOrder;
import com.procurea.procurementsystem.entity.Vendor;
import com.procurea.procurementsystem.exception.BadRequestException;
import com.procurea.procurementsystem.exception.ResourceNotFoundException;
import com.procurea.procurementsystem.repository.DeliveryRepository;
import com.procurea.procurementsystem.repository.PurchaseOrderRepository;
import com.procurea.procurementsystem.repository.VendorRepository;
import com.procurea.procurementsystem.service.AuditLogService;
import com.procurea.procurementsystem.service.DeliveryService;
import com.procurea.procurementsystem.service.NotificationService;
import com.procurea.procurementsystem.service.VendorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class DeliveryServiceImpl implements DeliveryService {

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Autowired
    private PurchaseOrderRepository poRepository;

    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private VendorService vendorService;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private NotificationService notificationService;

    @Override
    @Transactional
    public DeliveryDto createDelivery(Long poId, String trackingNumber, String carrier, String notes) {
        PurchaseOrder po = poRepository.findById(poId)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase Order not found"));

        Optional<Delivery> existing = deliveryRepository.findByPurchaseOrderId(poId);
        if (existing.isPresent()) {
            throw new BadRequestException("Delivery tracking is already set up for this PO");
        }

        Delivery delivery = new Delivery();
        delivery.setPurchaseOrder(po);
        delivery.setTrackingNumber(trackingNumber);
        delivery.setCarrier(carrier);
        delivery.setNotes(notes);
        delivery.setStatus(Delivery.DeliveryStatus.IN_TRANSIT);

        // Update PO status to ISSUED if not already
        if (po.getStatus() == PurchaseOrder.POStatus.ACKNOWLEDGED || po.getStatus() == PurchaseOrder.POStatus.CREATED) {
            po.setStatus(PurchaseOrder.POStatus.ISSUED);
            poRepository.save(po);
        }

        Delivery saved = deliveryRepository.save(delivery);

        auditLogService.log("SYSTEM", "CREATE_DELIVERY", "Delivery", saved.getId(), null, saved.getTrackingNumber());

        // Notify manager
        if (po.getQuotation() != null && po.getQuotation().getRfq() != null && po.getQuotation().getRfq().getPurchaseRequest() != null) {
            User requester = po.getQuotation().getRfq().getPurchaseRequest().getRequestedBy();
            if (requester != null) {
                notificationService.sendNotification(
                        requester.getId(),
                        "Delivery in transit for PO " + po.getPoNumber() + " via " + carrier + " (Tracking: " + trackingNumber + ")",
                        "DELIVERY"
                );
            }
        }

        return convertToDto(saved);
    }

    @Override
    public DeliveryDto getDeliveryById(Long id) {
        Delivery delivery = deliveryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery details not found with id: " + id));
        return convertToDto(delivery);
    }

    @Override
    public DeliveryDto getDeliveryByPoId(Long poId) {
        Delivery delivery = deliveryRepository.findByPurchaseOrderId(poId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery tracking not found for PO: " + poId));
        return convertToDto(delivery);
    }

    @Override
    public Page<DeliveryDto> getAllDeliveries(Delivery.DeliveryStatus status, String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Delivery> deliveries = deliveryRepository.searchDeliveries(status, search, pageable);
        return deliveries.map(this::convertToDto);
    }

    @Override
    public Page<DeliveryDto> getDeliveriesForVendor(Long vendorId, Delivery.DeliveryStatus status, String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Delivery> deliveries = deliveryRepository.searchDeliveriesForVendor(vendorId, status, search, pageable);
        return deliveries.map(this::convertToDto);
    }

    @Override
    @Transactional
    public DeliveryDto updateDeliveryStatus(Long id, Delivery.DeliveryStatus status, String notes) {
        Delivery delivery = deliveryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery tracking record not found"));

        String oldStatus = delivery.getStatus().name();
        delivery.setStatus(status);
        if (notes != null) {
            delivery.setNotes(notes);
        }

        if (status == Delivery.DeliveryStatus.DELIVERED) {
            delivery.setDeliveryDate(LocalDateTime.now());
            // Update PO Status
            PurchaseOrder po = delivery.getPurchaseOrder();
            if (po != null) {
                po.setStatus(PurchaseOrder.POStatus.DELIVERED);
                poRepository.save(po);
            }
        }

        Delivery updated = deliveryRepository.save(delivery);

        auditLogService.log("SYSTEM", "UPDATE_DELIVERY_STATUS", "Delivery", updated.getId(), oldStatus, status.name());

        // Notify vendor user
        if (delivery.getPurchaseOrder() != null && delivery.getPurchaseOrder().getQuotation() != null) {
            Vendor v = delivery.getPurchaseOrder().getQuotation().getVendor();
            if (v != null && v.getUser() != null) {
                notificationService.sendNotification(
                        v.getUser().getId(),
                        "Your delivery status for PO " + delivery.getPurchaseOrder().getPoNumber() + " has been updated to: " + status.name(),
                        "DELIVERY"
                );
            }
        }

        return convertToDto(updated);
    }

    @Override
    @Transactional
    public DeliveryDto performQualityCheck(Long id, Double qualityScore, Double rejectionRate, String qualityComments) {
        Delivery delivery = deliveryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery record not found"));

        delivery.setQualityScore(qualityScore);
        delivery.setRejectionRate(rejectionRate);
        delivery.setQualityComments(qualityComments);

        Delivery saved = deliveryRepository.save(delivery);

        auditLogService.log("SYSTEM", "PERFORM_QUALITY_CHECK", "Delivery", saved.getId(), null, "Score: " + qualityScore + ", Rejection: " + rejectionRate + "%");

        // Trigger performance calculations for this vendor
        if (delivery.getPurchaseOrder() != null && delivery.getPurchaseOrder().getQuotation() != null && delivery.getPurchaseOrder().getQuotation().getVendor() != null) {
            Vendor vendor = delivery.getPurchaseOrder().getQuotation().getVendor();
            recalculateVendorMetrics(vendor.getId());
        }

        return convertToDto(saved);
    }

    private void recalculateVendorMetrics(Long vendorId) {
        List<Quotation> quotations = quotationRepository.findByVendorId(vendorId);
        // Find all deliveries for this vendor
        List<Delivery> deliveries = deliveryRepository.findAll().stream()
                .filter(d -> d.getPurchaseOrder() != null &&
                        d.getPurchaseOrder().getQuotation() != null &&
                        d.getPurchaseOrder().getQuotation().getVendor() != null &&
                        d.getPurchaseOrder().getQuotation().getVendor().getId().equals(vendorId))
                .toList();

        if (deliveries.isEmpty()) return;

        double onTimeDeliveries = 0.0;
        double totalQualityScore = 0.0;
        int qualityCount = 0;
        double totalFulfillmentRate = 0.0;
        int fulfillmentCount = 0;
        
        // Mock response time for performance calculation
        double avgResponseHours = 12.0; 

        for (Delivery d : deliveries) {
            PurchaseOrder po = d.getPurchaseOrder();
            if (po != null && po.getExpectedDeliveryDate() != null && d.getDeliveryDate() != null) {
                if (d.getDeliveryDate().isBefore(po.getExpectedDeliveryDate()) || d.getDeliveryDate().isEqual(po.getExpectedDeliveryDate())) {
                    onTimeDeliveries += 1.0;
                }
            } else {
                onTimeDeliveries += 1.0; // default if dates missing
            }

            if (d.getQualityScore() != null) {
                totalQualityScore += d.getQualityScore();
                qualityCount++;
            }

            if (d.getRejectionRate() != null) {
                totalFulfillmentRate += (100.0 - d.getRejectionRate());
                fulfillmentCount++;
            }
        }

        double onTimeRate = (onTimeDeliveries / deliveries.size()) * 100.0;
        double finalQualityScore = qualityCount > 0 ? (totalQualityScore / qualityCount) : 4.0; // default 4 stars
        double finalFulfillmentRate = fulfillmentCount > 0 ? (totalFulfillmentRate / fulfillmentCount) : 100.0;

        vendorService.updatePerformanceMetrics(vendorId, onTimeRate, finalFulfillmentRate, finalQualityScore, avgResponseHours);
    }

    @Autowired
    private QuotationRepository quotationRepository;

    private DeliveryDto convertToDto(Delivery d) {
        DeliveryDto dto = new DeliveryDto();
        dto.setId(d.getId());
        if (d.getPurchaseOrder() != null) {
            dto.setPurchaseOrderId(d.getPurchaseOrder().getId());
            dto.setPoNumber(d.getPurchaseOrder().getPoNumber());
            if (d.getPurchaseOrder().getQuotation() != null && d.getPurchaseOrder().getQuotation().getVendor() != null) {
                dto.setVendorCompanyName(d.getPurchaseOrder().getQuotation().getVendor().getCompanyName());
            }
        }
        dto.setTrackingNumber(d.getTrackingNumber());
        dto.setCarrier(d.getCarrier());
        dto.setStatus(d.getStatus().name());
        dto.setDeliveryDate(d.getDeliveryDate());
        dto.setNotes(d.getNotes());
        
        dto.setQualityScore(d.getQualityScore());
        dto.setRejectionRate(d.getRejectionRate());
        dto.setQualityComments(d.getQualityComments());
        return dto;
    }
}
