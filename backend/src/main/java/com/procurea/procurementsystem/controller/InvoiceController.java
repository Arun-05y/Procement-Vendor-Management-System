package com.procurea.procurementsystem.controller;

import com.procurea.procurementsystem.dto.ApiResponse;
import com.procurea.procurementsystem.dto.InvoiceDto;
import com.procurea.procurementsystem.entity.Invoice;
import com.procurea.procurementsystem.security.UserDetailsImpl;
import com.procurea.procurementsystem.service.InvoiceService;
import com.procurea.procurementsystem.service.VendorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private VendorService vendorService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('FINANCE') or hasRole('PROCUREMENT_MANAGER')")
    public ResponseEntity<ApiResponse<Page<InvoiceDto>>> getAllInvoices(
            @RequestParam(required = false) Invoice.PaymentStatus status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<InvoiceDto> invoices = invoiceService.getAllInvoices(status, search, page, size);
        return ResponseEntity.ok(ApiResponse.success("Invoices fetched successfully", invoices));
    }

    @GetMapping("/vendor")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<Page<InvoiceDto>>> getInvoicesForVendor(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(required = false) Invoice.PaymentStatus status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long vendorId = vendorService.getVendorByUserId(userDetails.getId()).getId();
        Page<InvoiceDto> invoices = invoiceService.getInvoicesForVendor(vendorId, status, search, page, size);
        return ResponseEntity.ok(ApiResponse.success("Invoices fetched successfully", invoices));
    }

    @GetMapping("/overdue")
    @PreAuthorize("hasRole('ADMIN') or hasRole('FINANCE') or hasRole('PROCUREMENT_MANAGER')")
    public ResponseEntity<ApiResponse<List<InvoiceDto>>> getOverdueInvoices() {
        List<InvoiceDto> overdue = invoiceService.getOverdueInvoices();
        return ResponseEntity.ok(ApiResponse.success("Overdue invoices fetched successfully", overdue));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<InvoiceDto>> getInvoiceById(@PathVariable Long id) {
        InvoiceDto invoice = invoiceService.getInvoiceById(id);
        return ResponseEntity.ok(ApiResponse.success("Invoice fetched successfully", invoice));
    }

    @GetMapping("/po/{poId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<InvoiceDto>> getInvoiceByPoId(@PathVariable Long poId) {
        InvoiceDto invoice = invoiceService.getInvoiceByPoId(poId);
        return ResponseEntity.ok(ApiResponse.success("Invoice fetched successfully", invoice));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('FINANCE') or hasRole('PROCUREMENT_MANAGER') or hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<InvoiceDto>> createInvoice(@RequestBody InvoiceDto dto) {
        InvoiceDto created = invoiceService.createInvoice(dto);
        return ResponseEntity.ok(ApiResponse.success("Invoice created successfully", created));
    }

    @PutMapping("/{id}/pay")
    @PreAuthorize("hasRole('ADMIN') or hasRole('FINANCE')")
    public ResponseEntity<ApiResponse<InvoiceDto>> recordPayment(
            @PathVariable Long id,
            @RequestParam Double amountPaid,
            @RequestParam(required = false, defaultValue = "BANK_TRANSFER") String paymentMethod,
            @RequestParam(required = false) String notes) {
        InvoiceDto updated = invoiceService.recordPayment(id, amountPaid, paymentMethod, notes);
        return ResponseEntity.ok(ApiResponse.success("Payment recorded successfully", updated));
    }
}
