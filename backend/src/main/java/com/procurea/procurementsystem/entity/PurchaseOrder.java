package com.procurea.procurementsystem.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "purchase_orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 35)
    private String poNumber;

    @ManyToOne
    @JoinColumn(name = "vendor_id")
    private Vendor vendor;

    @ManyToOne
    @JoinColumn(name = "purchase_request_id")
    private PurchaseRequest purchaseRequest;

    @OneToOne
    @JoinColumn(name = "quotation_id")
    private Quotation quotation;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime orderDate;

    private LocalDateTime expectedDeliveryDate;

    private Double subtotal = 0.0;
    private Double taxRate = 18.0; // 18% GST standard
    private Double tax = 0.0;
    private Double discount = 0.0;
    private Double grandTotal = 0.0;

    @Enumerated(EnumType.STRING)
    @Column(length = 25)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(length = 25)
    private POStatus status = POStatus.PENDING;

    private String deliveryAddress;

    @Column(columnDefinition = "TEXT")
    private String termsAndConditions;

    @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<PurchaseOrderItem> items = new ArrayList<>();

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public void addItem(PurchaseOrderItem item) {
        items.add(item);
        item.setPurchaseOrder(this);
    }

    public void calculateTotals() {
        double sum = 0.0;
        if (items != null) {
            for (PurchaseOrderItem item : items) {
                if (item.getLineTotal() != null) {
                    sum += item.getLineTotal();
                }
            }
        }
        this.subtotal = sum;
        this.tax = (this.subtotal * (taxRate != null ? taxRate : 18.0)) / 100.0;
        double disc = this.discount != null ? this.discount : 0.0;
        this.grandTotal = Math.max(0.0, this.subtotal + this.tax - disc);
    }

    public enum POStatus {
        DRAFT, PENDING, APPROVED, ORDERED, DELIVERED, CANCELLED
    }

    public enum PaymentStatus {
        PENDING, PARTIALLY_PAID, PAID, OVERDUE
    }
}
