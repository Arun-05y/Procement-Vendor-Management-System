package com.procurea.procurementsystem.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

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
    private Integer deliveryDays;
    private Double qualityRating; // 1.0 to 5.0
    private Integer warrantyMonths;
    private Double previousPerformanceScore; // calculated performance score, e.g., 0.0 to 100.0
    
    @Column(columnDefinition = "TEXT")
    private String termsAndConditions;
    
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private QuotationStatus status = QuotationStatus.SUBMITTED;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime submittedAt;

    public enum QuotationStatus {
        SUBMITTED, SHORTLISTED, REJECTED, ACCEPTED
    }
}
