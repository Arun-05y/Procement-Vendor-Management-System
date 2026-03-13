package com.procurea.procurementsystem.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "purchase_orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "quotation_id")
    private Quotation quotation;

    private String poNumber;
    private LocalDateTime issuedDate = LocalDateTime.now();
    
    @Enumerated(EnumType.STRING)
    private POStatus status = POStatus.ISSUED;

    private String deliveryAddress;
    private LocalDateTime expectedDeliveryDate;

    public enum POStatus {
        ISSUED, ACKNOWLEDGED, SHIPPED, DELIVERED, CANCELLED
    }
}
