package com.procurea.procurementsystem.service;

import com.procurea.procurementsystem.dto.PaymentDto;
import com.procurea.procurementsystem.entity.Payment;
import org.springframework.data.domain.Page;

public interface PaymentService {
    PaymentDto createPayment(Long poId, Double amount, String paymentMethod);
    PaymentDto getPaymentById(Long id);
    PaymentDto getPaymentByPoId(Long poId);
    Page<PaymentDto> getAllPayments(Payment.PaymentStatus status, String search, int page, int size);
    Page<PaymentDto> getPaymentsForVendor(Long vendorId, Payment.PaymentStatus status, String search, int page, int size);
    PaymentDto processPayment(Long id, String transactionReference);
    PaymentDto completePayment(Long id, String transactionReference);
    PaymentDto failPayment(Long id, String reason);
}
