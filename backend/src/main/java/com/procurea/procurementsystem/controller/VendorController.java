package com.procurea.procurementsystem.controller;

import com.procurea.procurementsystem.model.Vendor;
import com.procurea.procurementsystem.service.VendorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/vendors")
public class VendorController {
    @Autowired
    private VendorService vendorService;

    @GetMapping
    @PreAuthorize("hasRole('PROCUREMENT_OFFICER') or hasRole('ADMIN')")
    public List<Vendor> getAllVendors() {
        return vendorService.getAllVendors();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Vendor> getVendorById(@PathVariable Long id) {
        return vendorService.getVendorById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Vendor> updateVendorStatus(
            @PathVariable Long id, 
            @RequestParam Vendor.VendorStatus status) {
        return ResponseEntity.ok(vendorService.updateVendorStatus(id, status));
    }
}
