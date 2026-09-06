package com.procurea.procurementsystem.controller;

import com.procurea.procurementsystem.dto.ApiResponse;
import com.procurea.procurementsystem.dto.VendorEvaluationDto;
import com.procurea.procurementsystem.entity.PurchaseOrder;
import com.procurea.procurementsystem.entity.Vendor;
import com.procurea.procurementsystem.entity.VendorEvaluation;
import com.procurea.procurementsystem.exception.ResourceNotFoundException;
import com.procurea.procurementsystem.repository.PurchaseOrderRepository;
import com.procurea.procurementsystem.repository.VendorEvaluationRepository;
import com.procurea.procurementsystem.repository.VendorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/vendor-evaluations")
public class VendorEvaluationController {

    @Autowired
    private VendorEvaluationRepository evaluationRepository;

    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private PurchaseOrderRepository poRepository;

    @GetMapping("/vendor/{vendorId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<VendorEvaluationDto>>> getEvaluationsByVendor(@PathVariable Long vendorId) {
        List<VendorEvaluationDto> list = evaluationRepository.findByVendorId(vendorId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Evaluations fetched successfully", list));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROCUREMENT_MANAGER') or hasRole('PROCUREMENT_EXECUTIVE')")
    public ResponseEntity<ApiResponse<VendorEvaluationDto>> submitEvaluation(@RequestBody VendorEvaluationDto dto) {
        Vendor vendor = vendorRepository.findById(dto.getVendorId())
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));

        PurchaseOrder po = null;
        if (dto.getPurchaseOrderId() != null) {
            po = poRepository.findById(dto.getPurchaseOrderId()).orElse(null);
        }

        VendorEvaluation eval = new VendorEvaluation();
        eval.setVendor(vendor);
        eval.setPurchaseOrder(po);
        eval.setDeliveryRating(dto.getDeliveryRating() != null ? dto.getDeliveryRating() : 5.0);
        eval.setQualityRating(dto.getQualityRating() != null ? dto.getQualityRating() : 5.0);
        eval.setPriceCompetitiveness(dto.getPriceCompetitiveness() != null ? dto.getPriceCompetitiveness() : 5.0);
        
        double overall = (eval.getDeliveryRating() + eval.getQualityRating() + eval.getPriceCompetitiveness()) / 3.0;
        eval.setOverallRating(overall);
        eval.setComments(dto.getComments());

        VendorEvaluation saved = evaluationRepository.save(eval);

        // Update vendor overall rating
        List<VendorEvaluation> allEvals = evaluationRepository.findByVendorId(vendor.getId());
        double avg = allEvals.stream().mapToDouble(VendorEvaluation::getOverallRating).average().orElse(overall);
        vendor.setRating(Math.round(avg * 10.0) / 10.0);
        vendorRepository.save(vendor);

        return ResponseEntity.ok(ApiResponse.success("Evaluation submitted successfully", convertToDto(saved)));
    }

    private VendorEvaluationDto convertToDto(VendorEvaluation e) {
        VendorEvaluationDto dto = new VendorEvaluationDto();
        dto.setId(e.getId());
        if (e.getVendor() != null) {
            dto.setVendorId(e.getVendor().getId());
            dto.setVendorCompanyName(e.getVendor().getCompanyName());
        }
        if (e.getPurchaseOrder() != null) {
            dto.setPurchaseOrderId(e.getPurchaseOrder().getId());
            dto.setPoNumber(e.getPurchaseOrder().getPoNumber());
        }
        dto.setDeliveryRating(e.getDeliveryRating());
        dto.setQualityRating(e.getQualityRating());
        dto.setPriceCompetitiveness(e.getPriceCompetitiveness());
        dto.setOverallRating(e.getOverallRating());
        dto.setComments(e.getComments());
        dto.setEvaluationDate(e.getEvaluationDate());
        return dto;
    }
}
