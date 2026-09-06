package com.procurea.procurementsystem.controller;

import com.procurea.procurementsystem.dto.ApiResponse;
import com.procurea.procurementsystem.dto.DeliveryDto;
import com.procurea.procurementsystem.entity.Delivery;
import com.procurea.procurementsystem.security.UserDetailsImpl;
import com.procurea.procurementsystem.service.DeliveryService;
import com.procurea.procurementsystem.service.VendorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/deliveries")
public class DeliveryController {

    @Autowired
    private DeliveryService deliveryService;

    @Autowired
    private VendorService vendorService;

    @GetMapping
    @PreAuthorize("hasRole('PROCUREMENT_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<DeliveryDto>>> getAllDeliveries(
            @RequestParam(required = false) Delivery.DeliveryStatus status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<DeliveryDto> deliveries = deliveryService.getAllDeliveries(status, search, page, size);
        return ResponseEntity.ok(ApiResponse.success("Deliveries fetched successfully", deliveries));
    }

    @GetMapping("/vendor")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<Page<DeliveryDto>>> getDeliveriesForVendor(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(required = false) Delivery.DeliveryStatus status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Long vendorId = vendorService.getVendorByUserId(userDetails.getId()).getId();
        Page<DeliveryDto> deliveries = deliveryService.getDeliveriesForVendor(vendorId, status, search, page, size);
        return ResponseEntity.ok(ApiResponse.success("Deliveries fetched successfully", deliveries));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('PROCUREMENT_MANAGER') or hasRole('ADMIN') or hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<DeliveryDto>> getDeliveryById(@PathVariable Long id) {
        DeliveryDto delivery = deliveryService.getDeliveryById(id);
        return ResponseEntity.ok(ApiResponse.success("Delivery fetched successfully", delivery));
    }

    @GetMapping("/po/{poId}")
    @PreAuthorize("hasRole('PROCUREMENT_MANAGER') or hasRole('ADMIN') or hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<DeliveryDto>> getDeliveryByPoId(@PathVariable Long poId) {
        DeliveryDto delivery = deliveryService.getDeliveryByPoId(poId);
        return ResponseEntity.ok(ApiResponse.success("Delivery fetched successfully", delivery));
    }

    @PostMapping("/track")
    @PreAuthorize("hasRole('VENDOR') or hasRole('PROCUREMENT_MANAGER')")
    public ResponseEntity<ApiResponse<DeliveryDto>> trackDelivery(
            @RequestParam Long poId,
            @RequestParam String trackingNumber,
            @RequestParam String carrier,
            @RequestParam(required = false, defaultValue = "") String notes) {
        DeliveryDto delivery = deliveryService.createDelivery(poId, trackingNumber, carrier, notes);
        return ResponseEntity.ok(ApiResponse.success("Delivery tracking initialized successfully", delivery));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('PROCUREMENT_MANAGER') or hasRole('ADMIN') or hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<DeliveryDto>> updateDeliveryStatus(
            @PathVariable Long id,
            @RequestParam Delivery.DeliveryStatus status,
            @RequestParam(required = false, defaultValue = "") String notes) {
        DeliveryDto updated = deliveryService.updateDeliveryStatus(id, status, notes);
        return ResponseEntity.ok(ApiResponse.success("Delivery status updated successfully", updated));
    }

    @PutMapping("/{id}/quality-check")
    @PreAuthorize("hasRole('PROCUREMENT_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DeliveryDto>> performQualityCheck(
            @PathVariable Long id,
            @RequestParam Double qualityScore,
            @RequestParam Double rejectionRate,
            @RequestParam(required = false, defaultValue = "") String comments) {
        
        DeliveryDto updated = deliveryService.performQualityCheck(id, qualityScore, rejectionRate, comments);
        return ResponseEntity.ok(ApiResponse.success("Quality check recorded successfully", updated));
    }
}
