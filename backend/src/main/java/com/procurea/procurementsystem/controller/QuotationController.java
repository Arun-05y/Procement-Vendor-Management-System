package com.procurea.procurementsystem.controller;

import com.procurea.procurementsystem.model.Quotation;
import com.procurea.procurementsystem.service.QuotationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/quotations")
public class QuotationController {
    @Autowired
    private QuotationService quotationService;

    @PostMapping("/submit")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<Quotation> submitQuotation(@RequestBody Quotation quotation) {
        return ResponseEntity.ok(quotationService.submitQuotation(quotation));
    }

    @GetMapping("/rfq/{rfqId}")
    @PreAuthorize("hasRole('PROCUREMENT_OFFICER')")
    public List<Quotation> getQuotations(@PathVariable Long rfqId) {
        return quotationService.getQuotationsByRFQ(rfqId);
    }

    @GetMapping("/rfq/{rfqId}/best")
    @PreAuthorize("hasRole('PROCUREMENT_OFFICER')")
    public ResponseEntity<Quotation> getBestQuotation(@PathVariable Long rfqId) {
        Quotation best = quotationService.getBestQuotation(rfqId);
        return best != null ? ResponseEntity.ok(best) : ResponseEntity.noContent().build();
    }
}
