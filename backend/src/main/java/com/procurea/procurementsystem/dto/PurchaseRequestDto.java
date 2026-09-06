package com.procurea.procurementsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseRequestDto {
    private Long id;
    private String requestNumber;
    private String title;
    private String description;
    private Double estimatedBudget;
    private Double totalAmount;
    private String department;
    private Long requestedById;
    private String requestedByUsername;
    private String status;
    private String approvalRemarks;
    private List<PurchaseRequestItemDto> items = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
