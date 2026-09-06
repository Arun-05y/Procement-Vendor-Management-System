package com.procurea.procurementsystem.controller;

import com.procurea.procurementsystem.dto.ApiResponse;
import com.procurea.procurementsystem.dto.PaymentDto;
import com.procurea.procurementsystem.entity.Payment;
import com.procurea.procurementsystem.security.UserDetailsImpl;
import com.procurea.procurementsystem.service.PaymentService;
import com.procurea.procurementsystem.service.VendorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private VendorService vendorService;

    @PostMapping
    @PreAuthorize("hasRole('PROCUREMENT_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PaymentDto>> createPayment(
            @RequestParam Long poId,
            @RequestParam(required = false) Double amount,
            @RequestParam(required = false) String paymentMethod) {
        
        PaymentDto payment = paymentService.createPayment(poId, amount, paymentMethod);
        return ResponseEntity.ok(ApiResponse.success("Payment request created successfully", payment));
    }

    @GetMapping
    @PreAuthorize("hasRole('PROCUREMENT_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<PaymentDto>>> getAllPayments(
            @RequestParam(required = false) Payment.PaymentStatus status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Page<PaymentDto> payments = paymentService.getAllPayments(status, search, page, size);
        return ResponseEntity.ok(ApiResponse.success("Payments fetched successfully", payments));
    }

    @GetMapping("/vendor")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<Page<PaymentDto>>> getPaymentsForVendor(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(required = false) Payment.PaymentStatus status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Long vendorId = vendorService.getVendorByUserId(userDetails.getId()).getId();
        Page<PaymentDto> payments = paymentService.getPaymentsForVendor(vendorId, status, search, page, size);
        return ResponseEntity.ok(ApiResponse.success("Payments fetched successfully", payments));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('PROCUREMENT_MANAGER') or hasRole('ADMIN') or hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<PaymentDto>> getPaymentById(@PathVariable Long id) {
        PaymentDto payment = paymentService.getPaymentById(id);
        return ResponseEntity.ok(ApiResponse.success("Payment fetched successfully", payment));
    }

    @PutMapping("/{id}/process")
    @PreAuthorize("hasRole('PROCUREMENT_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PaymentDto>> processPayment(
            @PathVariable Long id,
            @RequestParam(required = false) String transactionReference) {
        
        PaymentDto payment = paymentService.processPayment(id, transactionReference);
        return ResponseEntity.ok(ApiResponse.success("Payment processing initiated", payment));
    }

    @PutMapping("/{id}/complete")
    @PreAuthorize("hasRole('PROCUREMENT_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PaymentDto>> completePayment(
            @PathVariable Long id,
            @RequestParam String transactionReference) {
        
        PaymentDto payment = paymentService.completePayment(id, transactionReference);
        return ResponseEntity.ok(ApiResponse.success("Payment completed successfully", payment));
    }

    @PutMapping("/{id}/fail")
    @PreAuthorize("hasRole('PROCUREMENT_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PaymentDto>> failPayment(
            @PathVariable Long id,
            @RequestParam String reason) {
        
        PaymentDto payment = paymentService.failPayment(id, reason);
        return ResponseEntity.ok(ApiResponse.success("Payment marked as failed", payment));
    }
}
