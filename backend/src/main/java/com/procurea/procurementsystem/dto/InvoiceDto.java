package com.procurea.procurementsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceDto {
    private Long id;
    private String invoiceNumber;
    private Long purchaseOrderId;
    private String poNumber;
    private Long vendorId;
    private String vendorCompanyName;
    private Double invoiceAmount;
    private Double paidAmount;
    private LocalDateTime invoiceDate;
    private LocalDate paymentDueDate;
    private LocalDateTime paymentDate;
    private String status;
    private String paymentMethod;
    private String notes;
    private boolean overdue;
}
