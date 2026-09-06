package com.procurea.procurementsystem.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "deliveries")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Delivery {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "po_id")
    private PurchaseOrder purchaseOrder;

    private String trackingNumber;
    private String carrier;
    
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private DeliveryStatus status = DeliveryStatus.PENDING;

    private LocalDateTime deliveryDate;
    private String notes;

    // Quality Check Fields
    private Double qualityScore; // e.g. 1.0 to 5.0
    private Double rejectionRate; // e.g. 0.0 to 100.0 (percentage of items rejected)
    private String qualityComments;

    public enum DeliveryStatus {
        PENDING, IN_TRANSIT, DELIVERED, RETURNED
    }
}
