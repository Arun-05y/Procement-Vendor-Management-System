package com.procurea.procurementsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {
    private Long id;
    private String productCode;
    private String name;
    private String category;
    private String description;
    private Double unitPrice;
    private Integer currentStock;
    private Integer minimumStockLevel;
    private String unit;
    private Long supplierId;
    private String supplierName;
    private String status;
    private boolean lowStock;
    private LocalDateTime createdAt;
}
