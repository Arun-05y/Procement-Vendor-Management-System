package com.procurea.procurementsystem.service.impl;

import com.procurea.procurementsystem.dto.PaymentDto;
import com.procurea.procurementsystem.entity.Payment;
import com.procurea.procurementsystem.entity.PurchaseOrder;
import com.procurea.procurementsystem.exception.BadRequestException;
import com.procurea.procurementsystem.exception.ResourceNotFoundException;
import com.procurea.procurementsystem.repository.PaymentRepository;
import com.procurea.procurementsystem.repository.PurchaseOrderRepository;
import com.procurea.procurementsystem.service.AuditLogService;
import com.procurea.procurementsystem.service.NotificationService;
import com.procurea.procurementsystem.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PurchaseOrderRepository poRepository;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private NotificationService notificationService;

    @Override
    @Transactional
    public PaymentDto createPayment(Long poId, Double amount, String paymentMethod) {
        PurchaseOrder po = poRepository.findById(poId)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase Order not found"));

        Optional<Payment> existing = paymentRepository.findByPurchaseOrderId(poId);
        if (existing.isPresent()) {
            throw new BadRequestException("Payment is already set up or processing for this PO");
        }

        Payment payment = new Payment();
        payment.setPurchaseOrder(po);
        payment.setAmount(amount == null ? po.getQuotation().getTotalAmount() : amount);
        payment.setPaymentMethod(paymentMethod == null ? "BANK_TRANSFER" : paymentMethod);
        payment.setStatus(Payment.PaymentStatus.PENDING);

        Payment saved = paymentRepository.save(payment);

        auditLogService.log("SYSTEM", "CREATE_PAYMENT", "Payment", saved.getId(), null, saved.getAmount().toString());

        return convertToDto(saved);
    }

    @Override
    public PaymentDto getPaymentById(Long id) {
        Payment p = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment record not found with id: " + id));
        return convertToDto(p);
    }

    @Override
    public PaymentDto getPaymentByPoId(Long poId) {
        Payment p = paymentRepository.findByPurchaseOrderId(poId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment record not found for PO: " + poId));
        return convertToDto(p);
    }

    @Override
    public Page<PaymentDto> getAllPayments(Payment.PaymentStatus status, String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Payment> payments = paymentRepository.searchPayments(status, search, pageable);
        return payments.map(this::convertToDto);
    }

    @Override
    public Page<PaymentDto> getPaymentsForVendor(Long vendorId, Payment.PaymentStatus status, String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Payment> payments = paymentRepository.searchPaymentsForVendor(vendorId, status, search, pageable);
        return payments.map(this::convertToDto);
    }

    @Override
    @Transactional
    public PaymentDto processPayment(Long id, String transactionReference) {
        Payment p = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment record not found"));

        if (p.getStatus() != Payment.PaymentStatus.PENDING && p.getStatus() != Payment.PaymentStatus.FAILED) {
            throw new BadRequestException("Payment cannot be processed from its current state: " + p.getStatus());
        }

        p.setStatus(Payment.PaymentStatus.PROCESSING);
        if (transactionReference != null) {
            p.setTransactionReference(transactionReference);
        }
        Payment updated = paymentRepository.save(p);

        auditLogService.log("SYSTEM", "PROCESS_PAYMENT", "Payment", updated.getId(), "PENDING", "PROCESSING");

        return convertToDto(updated);
    }

    @Override
    @Transactional
    public PaymentDto completePayment(Long id, String transactionReference) {
        Payment p = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment record not found"));

        String oldStatus = p.getStatus().name();
        p.setStatus(Payment.PaymentStatus.PAID);
        if (transactionReference != null) {
            p.setTransactionReference(transactionReference);
        }
        Payment updated = paymentRepository.save(p);

        auditLogService.log("SYSTEM", "COMPLETE_PAYMENT", "Payment", updated.getId(), oldStatus, "PAID");

        // Notify vendor user
        if (p.getPurchaseOrder() != null && p.getPurchaseOrder().getQuotation() != null) {
            Vendor v = p.getPurchaseOrder().getQuotation().getVendor();
            if (v != null && v.getUser() != null) {
                notificationService.sendNotification(
                        v.getUser().getId(),
                        "Payment of ₹" + p.getAmount() + " completed for Purchase Order: " + p.getPurchaseOrder().getPoNumber(),
                        "PAYMENT"
                );
            }
        }

        return convertToDto(updated);
    }

    @Override
    @Transactional
    public PaymentDto failPayment(Long id, String reason) {
        Payment p = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment record not found"));

        String oldStatus = p.getStatus().name();
        p.setStatus(Payment.PaymentStatus.FAILED);
        Payment updated = paymentRepository.save(p);

        auditLogService.log("SYSTEM", "FAIL_PAYMENT", "Payment", updated.getId(), oldStatus, "FAILED - " + reason);

        // Notify vendor user
        if (p.getPurchaseOrder() != null && p.getPurchaseOrder().getQuotation() != null) {
            Vendor v = p.getPurchaseOrder().getQuotation().getVendor();
            if (v != null && v.getUser() != null) {
                notificationService.sendNotification(
                        v.getUser().getId(),
                        "Payment failed for Purchase Order: " + p.getPurchaseOrder().getPoNumber() + ". Reason: " + reason,
                        "PAYMENT"
                );
            }
        }

        return convertToDto(updated);
    }

    private PaymentDto convertToDto(Payment p) {
        PaymentDto dto = new PaymentDto();
        dto.setId(p.getId());
        if (p.getPurchaseOrder() != null) {
            dto.setPurchaseOrderId(p.getPurchaseOrder().getId());
            dto.setPoNumber(p.getPurchaseOrder().getPoNumber());
            if (p.getPurchaseOrder().getQuotation() != null && p.getPurchaseOrder().getQuotation().getVendor() != null) {
                dto.setVendorCompanyName(p.getPurchaseOrder().getQuotation().getVendor().getCompanyName());
            }
        }
        dto.setAmount(p.getAmount());
        dto.setPaymentDate(p.getPaymentDate());
        dto.setStatus(p.getStatus().name());
        dto.setTransactionReference(p.getTransactionReference());
        dto.setPaymentMethod(p.getPaymentMethod());
        return dto;
    }
}
