package com.procurea.procurementsystem.service.impl;

import com.procurea.procurementsystem.dto.InvoiceDto;
import com.procurea.procurementsystem.entity.Invoice;
import com.procurea.procurementsystem.entity.PurchaseOrder;
import com.procurea.procurementsystem.entity.User;
import com.procurea.procurementsystem.entity.Vendor;
import com.procurea.procurementsystem.exception.BadRequestException;
import com.procurea.procurementsystem.exception.ResourceNotFoundException;
import com.procurea.procurementsystem.repository.InvoiceRepository;
import com.procurea.procurementsystem.repository.PurchaseOrderRepository;
import com.procurea.procurementsystem.repository.UserRepository;
import com.procurea.procurementsystem.repository.VendorRepository;
import com.procurea.procurementsystem.service.AuditLogService;
import com.procurea.procurementsystem.service.InvoiceService;
import com.procurea.procurementsystem.service.NotificationService;
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
public class InvoiceServiceImpl implements InvoiceService {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private PurchaseOrderRepository poRepository;

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
    public InvoiceDto createInvoice(InvoiceDto dto) {
        PurchaseOrder po = poRepository.findById(dto.getPurchaseOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Purchase Order not found"));

        if (invoiceRepository.findByPurchaseOrderId(po.getId()).isPresent()) {
            throw new BadRequestException("Invoice already exists for this purchase order");
        }

        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber(dto.getInvoiceNumber() != null && !dto.getInvoiceNumber().isBlank()
                ? dto.getInvoiceNumber()
                : "INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        invoice.setPurchaseOrder(po);
        invoice.setVendor(po.getVendor());
        invoice.setInvoiceAmount(dto.getInvoiceAmount() != null ? dto.getInvoiceAmount() : po.getGrandTotal());
        invoice.setPaidAmount(0.0);
        invoice.setPaymentDueDate(dto.getPaymentDueDate() != null ? dto.getPaymentDueDate() : LocalDate.now().plusDays(30));
        invoice.setStatus(Invoice.PaymentStatus.PENDING);
        invoice.setPaymentMethod(dto.getPaymentMethod() != null ? dto.getPaymentMethod() : "BANK_TRANSFER");
        invoice.setNotes(dto.getNotes());

        Invoice saved = invoiceRepository.save(invoice);

        auditLogService.log("SYSTEM", "CREATE_INVOICE", "Invoice", saved.getId(), null, saved.getInvoiceNumber());

        // Notify Finance
        notifyFinance("New Invoice " + saved.getInvoiceNumber() + " created for PO " + po.getPoNumber() + " (Amount: ₹" + saved.getInvoiceAmount() + ")");

        return convertToDto(saved);
    }

    @Override
    public InvoiceDto getInvoiceById(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + id));
        invoice.checkAndSetOverdue();
        return convertToDto(invoice);
    }

    @Override
    public InvoiceDto getInvoiceByPoId(Long poId) {
        Invoice invoice = invoiceRepository.findByPurchaseOrderId(poId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found for PO: " + poId));
        invoice.checkAndSetOverdue();
        return convertToDto(invoice);
    }

    @Override
    public Page<InvoiceDto> getAllInvoices(Invoice.PaymentStatus status, String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Invoice> invoices = invoiceRepository.searchInvoices(status, search, pageable);
        invoices.forEach(Invoice::checkAndSetOverdue);
        return invoices.map(this::convertToDto);
    }

    @Override
    public Page<InvoiceDto> getInvoicesForVendor(Long vendorId, Invoice.PaymentStatus status, String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Invoice> invoices = invoiceRepository.searchInvoicesForVendor(vendorId, status, search, pageable);
        invoices.forEach(Invoice::checkAndSetOverdue);
        return invoices.map(this::convertToDto);
    }

    @Override
    @Transactional
    public InvoiceDto recordPayment(Long id, Double amountPaid, String paymentMethod, String notes) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + id));

        double newPaid = (invoice.getPaidAmount() != null ? invoice.getPaidAmount() : 0.0) + (amountPaid != null ? amountPaid : 0.0);
        invoice.setPaidAmount(newPaid);
        invoice.setPaymentDate(LocalDateTime.now());
        if (paymentMethod != null) invoice.setPaymentMethod(paymentMethod);
        if (notes != null) invoice.setNotes(notes);

        if (newPaid >= invoice.getInvoiceAmount()) {
            invoice.setStatus(Invoice.PaymentStatus.PAID);
        } else if (newPaid > 0) {
            invoice.setStatus(Invoice.PaymentStatus.PARTIALLY_PAID);
        }

        Invoice saved = invoiceRepository.save(invoice);

        // Update PO payment status
        PurchaseOrder po = saved.getPurchaseOrder();
        if (po != null) {
            if (saved.getStatus() == Invoice.PaymentStatus.PAID) {
                po.setPaymentStatus(PurchaseOrder.PaymentStatus.PAID);
            } else if (saved.getStatus() == Invoice.PaymentStatus.PARTIALLY_PAID) {
                po.setPaymentStatus(PurchaseOrder.PaymentStatus.PARTIALLY_PAID);
            }
            poRepository.save(po);
        }

        auditLogService.log("FINANCE", "RECORD_PAYMENT", "Invoice", saved.getId(), null, "Paid: ₹" + amountPaid + ", Status: " + saved.getStatus());

        // Notify Vendor
        if (saved.getVendor() != null && saved.getVendor().getUser() != null) {
            notificationService.sendNotification(
                    saved.getVendor().getUser().getId(),
                    "Payment of ₹" + amountPaid + " received for Invoice " + saved.getInvoiceNumber() + ". Current status: " + saved.getStatus(),
                    "PAYMENT"
            );
        }

        return convertToDto(saved);
    }

    @Override
    public List<InvoiceDto> getOverdueInvoices() {
        return invoiceRepository.findOverdueInvoices(LocalDate.now()).stream()
                .peek(Invoice::checkAndSetOverdue)
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public long countOverdueInvoices() {
        return invoiceRepository.countOverdueInvoices(LocalDate.now());
    }

    @Override
    public Double getTotalPendingAmount() {
        Double total = invoiceRepository.sumPendingInvoiceAmount();
        return total != null ? total : 0.0;
    }

    private void notifyFinance(String message) {
        List<User> financeUsers = userRepository.findAll().stream()
                .filter(u -> u.getRoles().stream().anyMatch(r -> 
                        r.getName() == com.procurea.procurementsystem.entity.Role.ERole.ROLE_FINANCE ||
                        r.getName() == com.procurea.procurementsystem.entity.Role.ERole.ROLE_ADMIN))
                .collect(Collectors.toList());

        for (User u : financeUsers) {
            notificationService.sendNotification(u.getId(), message, "INVOICE");
        }
    }

    private InvoiceDto convertToDto(Invoice i) {
        InvoiceDto dto = new InvoiceDto();
        dto.setId(i.getId());
        dto.setInvoiceNumber(i.getInvoiceNumber());
        if (i.getPurchaseOrder() != null) {
            dto.setPurchaseOrderId(i.getPurchaseOrder().getId());
            dto.setPoNumber(i.getPurchaseOrder().getPoNumber());
        }
        if (i.getVendor() != null) {
            dto.setVendorId(i.getVendor().getId());
            dto.setVendorCompanyName(i.getVendor().getCompanyName());
        }
        dto.setInvoiceAmount(i.getInvoiceAmount());
        dto.setPaidAmount(i.getPaidAmount());
        dto.setInvoiceDate(i.getInvoiceDate());
        dto.setPaymentDueDate(i.getPaymentDueDate());
        dto.setPaymentDate(i.getPaymentDate());
        dto.setStatus(i.getStatus() != null ? i.getStatus().name() : "PENDING");
        dto.setPaymentMethod(i.getPaymentMethod());
        dto.setNotes(i.getNotes());
        dto.setOverdue(i.getStatus() == Invoice.PaymentStatus.OVERDUE || 
                (i.getStatus() != Invoice.PaymentStatus.PAID && i.getPaymentDueDate() != null && LocalDate.now().isAfter(i.getPaymentDueDate())));
        return dto;
    }
}
