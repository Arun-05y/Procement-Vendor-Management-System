package com.procurea.procurementsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryDto {
    private Long id;
    private Long purchaseOrderId;
    private String poNumber;
    private String vendorCompanyName;
    private String trackingNumber;
    private String carrier;
    private String status;
    private LocalDateTime deliveryDate;
    private String notes;

    // Quality check
    private Double qualityScore;
    private Double rejectionRate;
    private String qualityComments;
}
