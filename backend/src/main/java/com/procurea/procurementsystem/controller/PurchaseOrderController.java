package com.procurea.procurementsystem.controller;

import com.procurea.procurementsystem.model.PurchaseOrder;
import com.procurea.procurementsystem.service.PurchaseOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/purchase-orders")
public class PurchaseOrderController {
    @Autowired
    private PurchaseOrderService poService;

    @PostMapping("/generate")
    @PreAuthorize("hasRole('PROCUREMENT_OFFICER')")
    public ResponseEntity<PurchaseOrder> generatePO(@RequestParam Long quotationId) {
        return ResponseEntity.ok(poService.generatePO(quotationId));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<PurchaseOrder> updateStatus(
            @PathVariable Long id, 
            @RequestParam PurchaseOrder.POStatus status) {
        return ResponseEntity.ok(poService.updatePOStatus(id, status));
    }
}
