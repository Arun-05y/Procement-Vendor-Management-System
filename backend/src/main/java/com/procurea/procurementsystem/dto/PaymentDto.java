package com.procurea.procurementsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDto {
    private Long id;
    private Long purchaseOrderId;
    private String poNumber;
    private String vendorCompanyName;
    private Double amount;
    private LocalDateTime paymentDate;
    private String status;
    private String transactionReference;
    private String paymentMethod;
}
