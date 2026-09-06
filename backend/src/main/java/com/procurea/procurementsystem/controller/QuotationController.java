package com.procurea.procurementsystem.controller;

import com.procurea.procurementsystem.dto.ApiResponse;
import com.procurea.procurementsystem.dto.QuotationDto;
import com.procurea.procurementsystem.entity.Quotation;
import com.procurea.procurementsystem.security.UserDetailsImpl;
import com.procurea.procurementsystem.service.QuotationService;
import com.procurea.procurementsystem.service.VendorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/quotations")
public class QuotationController {

    @Autowired
    private QuotationService quotationService;

    @Autowired
    private VendorService vendorService;

    @PostMapping("/submit")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<QuotationDto>> submitQuotation(
            @RequestBody QuotationDto dto,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        Long vendorId = vendorService.getVendorByUserId(userDetails.getId()).getId();
        QuotationDto created = quotationService.submitQuotation(dto, vendorId);
        return ResponseEntity.ok(ApiResponse.success("Quotation submitted successfully", created));
    }

    @GetMapping("/rfq/{rfqId}")
    @PreAuthorize("hasRole('PROCUREMENT_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<QuotationDto>>> getQuotationsByRFQ(@PathVariable Long rfqId) {
        List<QuotationDto> quotations = quotationService.getQuotationsForRFQ(rfqId);
        return ResponseEntity.ok(ApiResponse.success("Quotations fetched successfully", quotations));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('PROCUREMENT_MANAGER') or hasRole('ADMIN') or hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<QuotationDto>> getQuotationById(@PathVariable Long id) {
        QuotationDto quotation = quotationService.getQuotationById(id);
        return ResponseEntity.ok(ApiResponse.success("Quotation fetched successfully", quotation));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('PROCUREMENT_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<QuotationDto>> updateQuotationStatus(
            @PathVariable Long id, 
            @RequestParam Quotation.QuotationStatus status) {
        QuotationDto updated = quotationService.updateQuotationStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Quotation status updated successfully", updated));
    }
}
