package com.procurea.procurementsystem.controller;

import com.procurea.procurementsystem.dto.ApiResponse;
import com.procurea.procurementsystem.dto.PurchaseRequestDto;
import com.procurea.procurementsystem.entity.PurchaseRequest;
import com.procurea.procurementsystem.security.UserDetailsImpl;
import com.procurea.procurementsystem.service.PurchaseRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/purchase-requests")
public class PurchaseRequestController {

    @Autowired
    private PurchaseRequestService prService;

    @PostMapping
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('PROCUREMENT_MANAGER')")
    public ResponseEntity<ApiResponse<PurchaseRequestDto>> createRequest(
            @RequestBody PurchaseRequestDto dto,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        PurchaseRequestDto created = prService.createRequest(dto, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Purchase request created successfully", created));
    }

    @GetMapping
    @PreAuthorize("hasRole('PROCUREMENT_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<PurchaseRequestDto>>> getAllRequests(
            @RequestParam(required = false) PurchaseRequest.RequestStatus status,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Page<PurchaseRequestDto> requests = prService.getAllRequests(status, department, search, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success("Purchase requests fetched successfully", requests));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<ApiResponse<Page<PurchaseRequestDto>>> getMyRequests(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(required = false) PurchaseRequest.RequestStatus status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<PurchaseRequestDto> requests = prService.getOwnRequests(userDetails.getId(), status, search, page, size);
        return ResponseEntity.ok(ApiResponse.success("My purchase requests fetched successfully", requests));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('PROCUREMENT_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PurchaseRequestDto>> getRequestById(@PathVariable Long id) {
        PurchaseRequestDto pr = prService.getRequestById(id);
        return ResponseEntity.ok(ApiResponse.success("Purchase request fetched successfully", pr));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('PROCUREMENT_MANAGER')")
    public ResponseEntity<ApiResponse<PurchaseRequestDto>> updateRequest(@PathVariable Long id, @RequestBody PurchaseRequestDto dto) {
        PurchaseRequestDto updated = prService.updateRequest(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Purchase request updated successfully", updated));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('PROCUREMENT_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PurchaseRequestDto>> approveRequest(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(required = false, defaultValue = "") String comments) {
        PurchaseRequestDto approved = prService.approveRequest(id, userDetails.getId(), comments);
        return ResponseEntity.ok(ApiResponse.success("Purchase request approved successfully", approved));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('PROCUREMENT_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PurchaseRequestDto>> rejectRequest(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(required = false, defaultValue = "") String comments) {
        PurchaseRequestDto rejected = prService.rejectRequest(id, userDetails.getId(), comments);
        return ResponseEntity.ok(ApiResponse.success("Purchase request rejected successfully", rejected));
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<ApiResponse<PurchaseRequestDto>> cancelRequest(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        PurchaseRequestDto cancelled = prService.cancelRequest(id, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Purchase request cancelled successfully", cancelled));
    }
}
