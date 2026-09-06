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
public class PurchaseOrderDto {
    private Long id;
    private String poNumber;
    private Long quotationId;
    private Long purchaseRequestId;
    private Long vendorId;
    private String vendorCompanyName;
    private String purchaseRequestTitle;
    private LocalDateTime orderDate;
    private LocalDateTime expectedDeliveryDate;
    private String deliveryAddress;
    private String termsAndConditions;
    
    private Double subtotal = 0.0;
    private Double taxRate = 18.0;
    private Double tax = 0.0;
    private Double discount = 0.0;
    private Double grandTotal = 0.0;
    
    private String paymentStatus;
    private String status;
    private List<PurchaseOrderItemDto> items = new ArrayList<>();
}
