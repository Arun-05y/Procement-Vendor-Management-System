package com.procurea.procurementsystem.service;

import com.procurea.procurementsystem.dto.InvoiceDto;
import com.procurea.procurementsystem.entity.Invoice;
import org.springframework.data.domain.Page;

import java.util.List;

public interface InvoiceService {
    InvoiceDto createInvoice(InvoiceDto dto);
    InvoiceDto getInvoiceById(Long id);
    InvoiceDto getInvoiceByPoId(Long poId);
    Page<InvoiceDto> getAllInvoices(Invoice.PaymentStatus status, String search, int page, int size);
    Page<InvoiceDto> getInvoicesForVendor(Long vendorId, Invoice.PaymentStatus status, String search, int page, int size);
    InvoiceDto recordPayment(Long id, Double amountPaid, String paymentMethod, String notes);
    List<InvoiceDto> getOverdueInvoices();
    long countOverdueInvoices();
    Double getTotalPendingAmount();
}
