package com.procurea.procurementsystem.model;

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
    private DeliveryStatus status = DeliveryStatus.IN_TRANSIT;

    private LocalDateTime deliveryDate;
    private String notes;

    public enum DeliveryStatus {
        PENDING, IN_TRANSIT, DELIVERED, RETURNED
    }
}
