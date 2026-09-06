package com.procurea.procurementsystem.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "purchase_request_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseRequestItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_request_id")
    @JsonIgnore
    private PurchaseRequest purchaseRequest;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    private String productName;
    private Integer quantity = 1;
    private Double unitPrice = 0.0;
    private Double estimatedTotal = 0.0;

    public void calculateTotal() {
        if (quantity != null && unitPrice != null) {
            this.estimatedTotal = quantity * unitPrice;
        }
    }
}
