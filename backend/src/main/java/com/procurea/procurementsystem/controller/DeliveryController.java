package com.procurea.procurementsystem.controller;

import com.procurea.procurementsystem.model.Delivery;
import com.procurea.procurementsystem.service.DeliveryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/deliveries")
public class DeliveryController {

    @Autowired
    private DeliveryService deliveryService;

    @GetMapping
    @PreAuthorize("hasRole('PROCUREMENT_OFFICER') or hasRole('ADMIN') or hasRole('VENDOR')")
    public List<Delivery> getAllDeliveries() {
        return deliveryService.getAllDeliveries();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('PROCUREMENT_OFFICER') or hasRole('ADMIN') or hasRole('VENDOR')")
    public ResponseEntity<Delivery> getDeliveryById(@PathVariable Long id) {
        return deliveryService.getDeliveryById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/po/{poId}")
    @PreAuthorize("hasRole('PROCUREMENT_OFFICER') or hasRole('ADMIN') or hasRole('VENDOR')")
    public ResponseEntity<Delivery> getDeliveryByPoId(@PathVariable Long poId) {
        return deliveryService.getDeliveryByPoId(poId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/track")
    @PreAuthorize("hasRole('VENDOR') or hasRole('PROCUREMENT_OFFICER')")
    public ResponseEntity<Delivery> trackDelivery(
            @RequestParam Long poId,
            @RequestParam String trackingNumber,
            @RequestParam String carrier) {
        return ResponseEntity.ok(deliveryService.trackDelivery(poId, trackingNumber, carrier));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('PROCUREMENT_OFFICER') or hasRole('ADMIN') or hasRole('VENDOR')")
    public ResponseEntity<Delivery> updateDeliveryStatus(
            @PathVariable Long id,
            @RequestParam Delivery.DeliveryStatus status) {
        return ResponseEntity.ok(deliveryService.updateDeliveryStatus(id, status));
    }
}
