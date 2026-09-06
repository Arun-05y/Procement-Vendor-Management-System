package com.procurea.procurementsystem.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "vendor_evaluations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VendorEvaluation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "vendor_id")
    private Vendor vendor;

    @ManyToOne
    @JoinColumn(name = "purchase_order_id")
    private PurchaseOrder purchaseOrder;

    private Double deliveryRating = 5.0; // 1 to 5
    private Double qualityRating = 5.0;  // 1 to 5
    private Double priceCompetitiveness = 5.0; // 1 to 5
    private Double overallRating = 5.0;

    @Column(columnDefinition = "TEXT")
    private String comments;

    @CreationTimestamp
    private LocalDateTime evaluationDate;
}
