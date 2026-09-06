package com.procurea.procurementsystem.controller;

import com.procurea.procurementsystem.dto.ApiResponse;
import com.procurea.procurementsystem.dto.QuotationComparisonDto;
import com.procurea.procurementsystem.service.QuotationComparisonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/quotations/compare")
public class QuotationComparisonController {

    @Autowired
    private QuotationComparisonService comparisonService;

    @GetMapping("/{rfqId}")
    @PreAuthorize("hasRole('PROCUREMENT_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<QuotationComparisonDto>> compareQuotations(@PathVariable Long rfqId) {
        QuotationComparisonDto comparison = comparisonService.compareQuotations(rfqId);
        return ResponseEntity.ok(ApiResponse.success("Quotations compared successfully", comparison));
    }
}
