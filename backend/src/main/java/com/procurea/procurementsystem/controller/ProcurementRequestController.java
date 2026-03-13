package com.procurea.procurementsystem.controller;

import com.procurea.procurementsystem.model.ProcurementRequest;
import com.procurea.procurementsystem.model.RFQ;
import com.procurea.procurementsystem.service.ProcurementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/procurement")
public class ProcurementRequestController {
    @Autowired
    private ProcurementService procurementService;

    @PostMapping("/requests")
    @PreAuthorize("hasRole('USER') or hasRole('PROCUREMENT_OFFICER')")
    public ResponseEntity<ProcurementRequest> createRequest(@RequestBody ProcurementRequest request) {
        return ResponseEntity.ok(procurementService.createRequest(request));
    }

    @GetMapping("/requests")
    @PreAuthorize("hasRole('PROCUREMENT_OFFICER') or hasRole('ADMIN')")
    public List<ProcurementRequest> getAllRequests() {
        return procurementService.getAllRequests();
    }

    @PutMapping("/requests/{id}/approve")
    @PreAuthorize("hasRole('PROCUREMENT_OFFICER') or hasRole('ADMIN')")
    public ResponseEntity<ProcurementRequest> approveRequest(@PathVariable Long id) {
        return ResponseEntity.ok(procurementService.approveRequest(id));
    }

    @PostMapping("/rfq")
    @PreAuthorize("hasRole('PROCUREMENT_OFFICER')")
    public ResponseEntity<RFQ> createRFQ(
            @RequestParam Long requestId, 
            @RequestParam String deadline,
            @RequestBody(required = false) Set<Long> vendorIds) {
        return ResponseEntity.ok(procurementService.createRFQ(requestId, LocalDateTime.parse(deadline), vendorIds));
    }
}
