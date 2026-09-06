package com.procurea.procurementsystem.controller;

import com.procurea.procurementsystem.dto.ApiResponse;
import com.procurea.procurementsystem.dto.PurchaseOrderDto;
import com.procurea.procurementsystem.entity.PurchaseOrder;
import com.procurea.procurementsystem.security.UserDetailsImpl;
import com.procurea.procurementsystem.service.PurchaseOrderService;
import com.procurea.procurementsystem.service.VendorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/purchase-orders")
public class PurchaseOrderController {

    @Autowired
    private PurchaseOrderService poService;

    @Autowired
    private VendorService vendorService;

    @PostMapping("/generate")
    @PreAuthorize("hasRole('PROCUREMENT_MANAGER') or hasRole('PROCUREMENT_EXECUTIVE') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PurchaseOrderDto>> generatePO(
            @RequestParam Long quotationId,
            @RequestParam String deliveryAddress,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime expectedDeliveryDate,
            @RequestParam(required = false, defaultValue = "") String termsAndConditions) {
        
        PurchaseOrderDto po = poService.createPurchaseOrder(quotationId, deliveryAddress, expectedDeliveryDate, termsAndConditions);
        return ResponseEntity.ok(ApiResponse.success("Purchase Order generated successfully", po));
    }

    @PostMapping("/from-request")
    @PreAuthorize("hasRole('PROCUREMENT_MANAGER') or hasRole('PROCUREMENT_EXECUTIVE') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PurchaseOrderDto>> createPOFromRequest(
            @RequestParam Long purchaseRequestId,
            @RequestParam Long vendorId,
            @RequestParam String deliveryAddress,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime expectedDeliveryDate,
            @RequestParam(required = false, defaultValue = "0.0") Double discount,
            @RequestParam(required = false, defaultValue = "Net 30 Days") String termsAndConditions) {
        
        PurchaseOrderDto po = poService.createPurchaseOrderFromRequest(
                purchaseRequestId,
                vendorId,
                deliveryAddress,
                expectedDeliveryDate != null ? expectedDeliveryDate : LocalDateTime.now().plusDays(14),
                discount,
                termsAndConditions
        );
        return ResponseEntity.ok(ApiResponse.success("Purchase Order created from request successfully", po));
    }

    @PostMapping("/direct")
    @PreAuthorize("hasRole('PROCUREMENT_MANAGER') or hasRole('PROCUREMENT_EXECUTIVE') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PurchaseOrderDto>> createDirectPO(@RequestBody PurchaseOrderDto dto) {
        PurchaseOrderDto po = poService.createDirectPO(dto);
        return ResponseEntity.ok(ApiResponse.success("Direct Purchase Order created successfully", po));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<PurchaseOrderDto>>> getAllPurchaseOrders(
            @RequestParam(required = false) PurchaseOrder.POStatus status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<PurchaseOrderDto> pos = poService.getAllPurchaseOrders(status, search, page, size);
        return ResponseEntity.ok(ApiResponse.success("Purchase Orders fetched successfully", pos));
    }

    @GetMapping("/vendor")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<Page<PurchaseOrderDto>>> getPurchaseOrdersForVendor(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(required = false) PurchaseOrder.POStatus status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Long vendorId = vendorService.getVendorByUserId(userDetails.getId()).getId();
        Page<PurchaseOrderDto> pos = poService.getPurchaseOrdersForVendor(vendorId, status, search, page, size);
        return ResponseEntity.ok(ApiResponse.success("Purchase Orders fetched successfully", pos));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PurchaseOrderDto>> getPurchaseOrderById(@PathVariable Long id) {
        PurchaseOrderDto po = poService.getPurchaseOrderById(id);
        return ResponseEntity.ok(ApiResponse.success("Purchase Order fetched successfully", po));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('PROCUREMENT_MANAGER') or hasRole('ADMIN') or hasRole('PROCUREMENT_EXECUTIVE')")
    public ResponseEntity<ApiResponse<PurchaseOrderDto>> updateStatus(
            @PathVariable Long id, 
            @RequestParam PurchaseOrder.POStatus status) {
        PurchaseOrderDto updated = poService.updatePOStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Purchase Order status updated successfully", updated));
    }

    @PutMapping("/{id}/acknowledge")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<PurchaseOrderDto>> acknowledgePO(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        Long vendorId = vendorService.getVendorByUserId(userDetails.getId()).getId();
        PurchaseOrderDto acknowledged = poService.acknowledgePO(id, vendorId);
        return ResponseEntity.ok(ApiResponse.success("Purchase Order acknowledged successfully", acknowledged));
    }
}
