package com.procurea.procurementsystem.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "quotations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Quotation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "rfq_id")
    private RFQ rfq;

    @ManyToOne
    @JoinColumn(name = "vendor_id")
    private Vendor vendor;

    private Double totalAmount;
    private String termsAndConditions;
    private String deliveryTime;
    
    @Enumerated(EnumType.STRING)
    private QuotationStatus status = QuotationStatus.SUBMITTED;

    private LocalDateTime submittedAt = LocalDateTime.now();

    public enum QuotationStatus {
        SUBMITTED, UNDER_REVIEW, ACCEPTED, REJECTED
    }
}
