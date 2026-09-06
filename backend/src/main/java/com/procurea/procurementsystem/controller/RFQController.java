package com.procurea.procurementsystem.controller;

import com.procurea.procurementsystem.dto.ApiResponse;
import com.procurea.procurementsystem.dto.RFQDto;
import com.procurea.procurementsystem.entity.RFQ;
import com.procurea.procurementsystem.security.UserDetailsImpl;
import com.procurea.procurementsystem.service.RFQService;
import com.procurea.procurementsystem.service.VendorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/rfqs")
public class RFQController {

    @Autowired
    private RFQService rfqService;

    @Autowired
    private VendorService vendorService;

    @PostMapping
    @PreAuthorize("hasRole('PROCUREMENT_MANAGER')")
    public ResponseEntity<ApiResponse<RFQDto>> createRFQ(@RequestBody RFQDto dto) {
        RFQDto created = rfqService.createRFQ(dto);
        return ResponseEntity.ok(ApiResponse.success("RFQ created successfully", created));
    }

    @GetMapping
    @PreAuthorize("hasRole('PROCUREMENT_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<RFQDto>>> getAllRFQs(
            @RequestParam(required = false) RFQ.RFQStatus status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<RFQDto> rfqs = rfqService.getAllRFQs(status, search, page, size);
        return ResponseEntity.ok(ApiResponse.success("RFQs fetched successfully", rfqs));
    }

    @GetMapping("/vendor")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<Page<RFQDto>>> getRFQsForVendor(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(required = false) RFQ.RFQStatus status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Long vendorId = vendorService.getVendorByUserId(userDetails.getId()).getId();
        Page<RFQDto> rfqs = rfqService.getRFQsForVendor(vendorId, status, search, page, size);
        return ResponseEntity.ok(ApiResponse.success("RFQs fetched successfully", rfqs));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('PROCUREMENT_MANAGER') or hasRole('ADMIN') or hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<RFQDto>> getRFQById(@PathVariable Long id) {
        RFQDto rfq = rfqService.getRFQById(id);
        return ResponseEntity.ok(ApiResponse.success("RFQ fetched successfully", rfq));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('PROCUREMENT_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RFQDto>> updateRFQStatus(
            @PathVariable Long id, 
            @RequestParam RFQ.RFQStatus status) {
        RFQDto updated = rfqService.updateRFQStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("RFQ status updated successfully", updated));
    }

    @PutMapping("/{id}/invite")
    @PreAuthorize("hasRole('PROCUREMENT_MANAGER')")
    public ResponseEntity<ApiResponse<RFQDto>> inviteVendors(
            @PathVariable Long id,
            @RequestBody Set<Long> vendorIds) {
        RFQDto updated = rfqService.inviteVendors(id, vendorIds);
        return ResponseEntity.ok(ApiResponse.success("Vendors invited successfully", updated));
    }
}
