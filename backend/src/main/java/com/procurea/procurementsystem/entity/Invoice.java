package com.procurea.procurementsystem.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "invoices")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 35)
    private String invoiceNumber;

    @ManyToOne
    @JoinColumn(name = "purchase_order_id")
    private PurchaseOrder purchaseOrder;

    @ManyToOne
    @JoinColumn(name = "vendor_id")
    private Vendor vendor;

    private Double invoiceAmount = 0.0;
    private Double paidAmount = 0.0;

    @CreationTimestamp
    private LocalDateTime invoiceDate;

    private LocalDate paymentDueDate;
    private LocalDateTime paymentDate;

    @Enumerated(EnumType.STRING)
    @Column(length = 25)
    private PaymentStatus status = PaymentStatus.PENDING;

    private String paymentMethod = "BANK_TRANSFER"; // BANK_TRANSFER, UPI, CREDIT_CARD, CHEQUE

    @Column(columnDefinition = "TEXT")
    private String notes;

    public void checkAndSetOverdue() {
        if (status != PaymentStatus.PAID && paymentDueDate != null && LocalDate.now().isAfter(paymentDueDate)) {
            this.status = PaymentStatus.OVERDUE;
        }
    }

    public enum PaymentStatus {
        PENDING, PARTIALLY_PAID, PAID, OVERDUE
    }
}
