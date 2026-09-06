package com.procurea.procurementsystem.service.impl;

import com.procurea.procurementsystem.dto.PurchaseOrderDto;
import com.procurea.procurementsystem.dto.PurchaseOrderItemDto;
import com.procurea.procurementsystem.entity.*;
import com.procurea.procurementsystem.exception.BadRequestException;
import com.procurea.procurementsystem.exception.ResourceNotFoundException;
import com.procurea.procurementsystem.repository.*;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PurchaseOrderServiceImpl implements PurchaseOrderService {

    @Autowired
    private PurchaseOrderRepository poRepository;

    @Autowired
    private PurchaseRequestRepository prRepository;

    @Autowired
    private QuotationRepository quotationRepository;

    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

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

        quotation.setStatus(Quotation.QuotationStatus.ACCEPTED);
        quotationRepository.save(quotation);

        PurchaseOrder po = new PurchaseOrder();
        po.setQuotation(quotation);
        po.setVendor(quotation.getVendor());
        if (quotation.getRfq() != null && quotation.getRfq().getPurchaseRequest() != null) {
            po.setPurchaseRequest(quotation.getRfq().getPurchaseRequest());
        }
        po.setPoNumber("PO-" + LocalDate.now().getYear() + "-" + String.format("%04d", (poRepository.count() + 1)));
        po.setDeliveryAddress(deliveryAddress);
        po.setExpectedDeliveryDate(expectedDeliveryDate);
        po.setTermsAndConditions(termsAndConditions);
        po.setStatus(PurchaseOrder.POStatus.PENDING);
        po.setPaymentStatus(PurchaseOrder.PaymentStatus.PENDING);

        po.setSubtotal(quotation.getTotalAmount());
        po.setTax((po.getSubtotal() * 18.0) / 100.0);
        po.setDiscount(0.0);
        po.setGrandTotal(po.getSubtotal() + po.getTax());

        PurchaseOrder saved = poRepository.save(po);

        auditLogService.log("SYSTEM", "CREATE_PO", "PurchaseOrder", saved.getId(), null, saved.getPoNumber());
        notifyVendor(saved);

        return convertToDto(saved);
    }

    @Override
    @Transactional
    public PurchaseOrderDto createPurchaseOrderFromRequest(Long purchaseRequestId, Long vendorId, String deliveryAddress, LocalDateTime expectedDeliveryDate, Double discount, String termsAndConditions) {
        PurchaseRequest pr = prRepository.findById(purchaseRequestId)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase Request not found"));

        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));

        PurchaseOrder po = new PurchaseOrder();
        po.setPoNumber("PO-" + LocalDate.now().getYear() + "-" + String.format("%04d", (poRepository.count() + 1)));
        po.setPurchaseRequest(pr);
        po.setVendor(vendor);
        po.setDeliveryAddress(deliveryAddress);
        po.setExpectedDeliveryDate(expectedDeliveryDate);
        po.setTermsAndConditions(termsAndConditions);
        po.setDiscount(discount != null ? discount : 0.0);
        po.setStatus(PurchaseOrder.POStatus.PENDING);
        po.setPaymentStatus(PurchaseOrder.PaymentStatus.PENDING);

        // Copy items from PurchaseRequest
        if (pr.getItems() != null && !pr.getItems().isEmpty()) {
            for (PurchaseRequestItem prItem : pr.getItems()) {
                PurchaseOrderItem poItem = new PurchaseOrderItem();
                poItem.setPurchaseOrder(po);
                poItem.setProduct(prItem.getProduct());
                poItem.setProductName(prItem.getProductName());
                poItem.setQuantity(prItem.getQuantity());
                poItem.setUnitPrice(prItem.getUnitPrice());
                poItem.calculateTotal();
                po.getItems().add(poItem);
            }
        }

        po.calculateTotals();
        PurchaseOrder saved = poRepository.save(po);

        pr.setStatus(PurchaseRequest.RequestStatus.COMPLETED);
        prRepository.save(pr);

        auditLogService.log("SYSTEM", "CREATE_PO_FROM_REQUEST", "PurchaseOrder", saved.getId(), pr.getRequestNumber(), saved.getPoNumber());
        notifyVendor(saved);

        return convertToDto(saved);
    }

    @Override
    @Transactional
    public PurchaseOrderDto createDirectPO(PurchaseOrderDto dto) {
        Vendor vendor = vendorRepository.findById(dto.getVendorId())
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));

        PurchaseOrder po = new PurchaseOrder();
        po.setPoNumber("PO-" + LocalDate.now().getYear() + "-" + String.format("%04d", (poRepository.count() + 1)));
        po.setVendor(vendor);
        po.setDeliveryAddress(dto.getDeliveryAddress());
        po.setExpectedDeliveryDate(dto.getExpectedDeliveryDate());
        po.setTermsAndConditions(dto.getTermsAndConditions());
        po.setDiscount(dto.getDiscount() != null ? dto.getDiscount() : 0.0);
        po.setStatus(PurchaseOrder.POStatus.PENDING);
        po.setPaymentStatus(PurchaseOrder.PaymentStatus.PENDING);

        if (dto.getItems() != null) {
            for (PurchaseOrderItemDto itemDto : dto.getItems()) {
                PurchaseOrderItem item = new PurchaseOrderItem();
                item.setPurchaseOrder(po);
                if (itemDto.getProductId() != null) {
                    Product product = productRepository.findById(itemDto.getProductId()).orElse(null);
                    item.setProduct(product);
                    item.setProductName(product != null ? product.getName() : itemDto.getProductName());
                    item.setUnitPrice(product != null ? product.getUnitPrice() : itemDto.getUnitPrice());
                } else {
                    item.setProductName(itemDto.getProductName());
                    item.setUnitPrice(itemDto.getUnitPrice() != null ? itemDto.getUnitPrice() : 0.0);
                }
                item.setQuantity(itemDto.getQuantity() != null ? itemDto.getQuantity() : 1);
                item.calculateTotal();
                po.getItems().add(item);
            }
        }

        po.calculateTotals();
        PurchaseOrder saved = poRepository.save(po);

        auditLogService.log("SYSTEM", "CREATE_DIRECT_PO", "PurchaseOrder", saved.getId(), null, saved.getPoNumber());
        notifyVendor(saved);

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
        Pageable pageable = PageRequest.of(page, size, Sort.by("orderDate").descending());
        Page<PurchaseOrder> pos = poRepository.searchPurchaseOrders(status, search, pageable);
        return pos.map(this::convertToDto);
    }

    @Override
    public Page<PurchaseOrderDto> getPurchaseOrdersForVendor(Long vendorId, PurchaseOrder.POStatus status, String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("orderDate").descending());
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

        // Auto-create Invoice when PO is APPROVED or ORDERED if not already created
        if ((status == PurchaseOrder.POStatus.APPROVED || status == PurchaseOrder.POStatus.ORDERED) &&
                invoiceRepository.findByPurchaseOrderId(po.getId()).isEmpty()) {
            Invoice invoice = new Invoice();
            invoice.setInvoiceNumber("INV-" + LocalDate.now().getYear() + "-" + String.format("%04d", (invoiceRepository.count() + 1)));
            invoice.setPurchaseOrder(po);
            invoice.setVendor(po.getVendor());
            invoice.setInvoiceAmount(po.getGrandTotal());
            invoice.setPaidAmount(0.0);
            invoice.setPaymentDueDate(LocalDate.now().plusDays(30));
            invoice.setStatus(Invoice.PaymentStatus.PENDING);
            invoice.setPaymentMethod("BANK_TRANSFER");
            invoiceRepository.save(invoice);
        }

        // If completed, update vendor completed order count
        if (status == PurchaseOrder.POStatus.DELIVERED && po.getVendor() != null) {
            Vendor v = po.getVendor();
            v.setCompletedOrdersCount((v.getCompletedOrdersCount() != null ? v.getCompletedOrdersCount() : 0) + 1);
            vendorRepository.save(v);
        }

        PurchaseOrder updated = poRepository.save(po);
        auditLogService.log("SYSTEM", "UPDATE_PO_STATUS", "PurchaseOrder", updated.getId(), oldStatus, status.name());

        return convertToDto(updated);
    }

    @Override
    @Transactional
    public PurchaseOrderDto acknowledgePO(Long id, Long vendorId) {
        PurchaseOrder po = poRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase Order not found"));

        po.setStatus(PurchaseOrder.POStatus.ORDERED);
        PurchaseOrder updated = poRepository.save(po);

        auditLogService.log("VENDOR", "ACKNOWLEDGE_PO", "PurchaseOrder", updated.getId(), "PENDING", "ORDERED");
        return convertToDto(updated);
    }

    private void notifyVendor(PurchaseOrder po) {
        if (po.getVendor() != null && po.getVendor().getUser() != null) {
            notificationService.sendNotification(
                    po.getVendor().getUser().getId(),
                    "New Purchase Order received: " + po.getPoNumber() + " (Amount: ₹" + po.getGrandTotal() + "). Please review.",
                    "PO_APPROVED"
            );
        }
    }

    private PurchaseOrderDto convertToDto(PurchaseOrder po) {
        PurchaseOrderDto dto = new PurchaseOrderDto();
        dto.setId(po.getId());
        dto.setPoNumber(po.getPoNumber());
        if (po.getQuotation() != null) {
            dto.setQuotationId(po.getQuotation().getId());
        }
        if (po.getPurchaseRequest() != null) {
            dto.setPurchaseRequestId(po.getPurchaseRequest().getId());
            dto.setPurchaseRequestTitle(po.getPurchaseRequest().getTitle());
        }
        if (po.getVendor() != null) {
            dto.setVendorId(po.getVendor().getId());
            dto.setVendorCompanyName(po.getVendor().getCompanyName());
        }
        dto.setOrderDate(po.getOrderDate());
        dto.setExpectedDeliveryDate(po.getExpectedDeliveryDate());
        dto.setDeliveryAddress(po.getDeliveryAddress());
        dto.setTermsAndConditions(po.getTermsAndConditions());
        dto.setSubtotal(po.getSubtotal());
        dto.setTaxRate(po.getTaxRate());
        dto.setTax(po.getTax());
        dto.setDiscount(po.getDiscount());
        dto.setGrandTotal(po.getGrandTotal());
        dto.setPaymentStatus(po.getPaymentStatus() != null ? po.getPaymentStatus().name() : "PENDING");
        dto.setStatus(po.getStatus() != null ? po.getStatus().name() : "PENDING");

        if (po.getItems() != null) {
            List<PurchaseOrderItemDto> itemDtos = po.getItems().stream().map(i -> {
                PurchaseOrderItemDto idto = new PurchaseOrderItemDto();
                idto.setId(i.getId());
                if (i.getProduct() != null) idto.setProductId(i.getProduct().getId());
                idto.setProductName(i.getProductName());
                idto.setQuantity(i.getQuantity());
                idto.setUnitPrice(i.getUnitPrice());
                idto.setLineTotal(i.getLineTotal());
                return idto;
            }).collect(Collectors.toList());
            dto.setItems(itemDtos);
        }

        return dto;
    }
}
