package com.procurea.procurementsystem.controller;

import com.procurea.procurementsystem.dto.ApiResponse;
import com.procurea.procurementsystem.dto.VendorDto;
import com.procurea.procurementsystem.entity.Vendor;
import com.procurea.procurementsystem.security.UserDetailsImpl;
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
@RequestMapping("/api/vendors")
public class VendorController {

    @Autowired
    private VendorService vendorService;

    @GetMapping
    @PreAuthorize("hasRole('PROCUREMENT_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<VendorDto>>> getAllVendors(
            @RequestParam(required = false) Vendor.VendorStatus status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "companyName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Page<VendorDto> vendors = vendorService.getAllVendors(status, category, search, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success("Vendors fetched successfully", vendors));
    }

    @GetMapping("/active")
    @PreAuthorize("hasRole('PROCUREMENT_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<VendorDto>>> getActiveVendors() {
        List<VendorDto> vendors = vendorService.getActiveVendors();
        return ResponseEntity.ok(ApiResponse.success("Active vendors fetched successfully", vendors));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('PROCUREMENT_MANAGER') or hasRole('ADMIN') or hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<VendorDto>> getVendorById(@PathVariable Long id) {
        VendorDto vendor = vendorService.getVendorById(id);
        return ResponseEntity.ok(ApiResponse.success("Vendor fetched successfully", vendor));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<VendorDto>> getMyVendorProfile(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        VendorDto vendor = vendorService.getVendorByUserId(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Vendor profile fetched successfully", vendor));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('VENDOR') and #id == authentication.principal.id)") // simplified auth check
    public ResponseEntity<ApiResponse<VendorDto>> updateVendor(@PathVariable Long id, @RequestBody VendorDto vendorDto) {
        VendorDto updated = vendorService.updateVendor(id, vendorDto);
        return ResponseEntity.ok(ApiResponse.success("Vendor updated successfully", updated));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROCUREMENT_MANAGER')")
    public ResponseEntity<ApiResponse<VendorDto>> updateVendorStatus(
            @PathVariable Long id, 
            @RequestParam Vendor.VendorStatus status) {
        VendorDto updated = vendorService.updateVendorStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Vendor status updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteVendor(@PathVariable Long id) {
        vendorService.deleteVendor(id);
        return ResponseEntity.ok(ApiResponse.success("Vendor deleted successfully", null));
    }
}
