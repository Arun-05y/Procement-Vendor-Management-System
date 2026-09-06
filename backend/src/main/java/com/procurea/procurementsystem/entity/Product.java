package com.procurea.procurementsystem.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 30)
    private String productCode;

    @NotBlank
    private String name;

    private String category; // e.g., Electronics, Raw Material, Office Supplies, Construction

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull
    private Double unitPrice;

    @NotNull
    private Integer currentStock = 0;

    @NotNull
    private Integer minimumStockLevel = 10;

    private String unit = "Units"; // e.g. Units, Kg, Packs, Boxes

    @ManyToOne
    @JoinColumn(name = "supplier_id")
    private Vendor supplier;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ProductStatus status = ProductStatus.ACTIVE;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public boolean isLowStock() {
        return currentStock != null && minimumStockLevel != null && currentStock <= minimumStockLevel;
    }

    public void updateStockStatus() {
        if (currentStock == null || currentStock <= 0) {
            this.status = ProductStatus.OUT_OF_STOCK;
        } else if (currentStock <= minimumStockLevel) {
            this.status = ProductStatus.LOW_STOCK;
        } else {
            this.status = ProductStatus.ACTIVE;
        }
    }

    public enum ProductStatus {
        ACTIVE, LOW_STOCK, OUT_OF_STOCK, DISCONTINUED
    }
}
