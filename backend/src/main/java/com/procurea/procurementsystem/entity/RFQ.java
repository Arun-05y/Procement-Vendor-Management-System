package com.procurea.procurementsystem.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "rfqs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RFQ {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "purchase_request_id")
    private PurchaseRequest purchaseRequest;

    private String title;
    private String description;
    private LocalDateTime deadline;
    
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private RFQStatus status = RFQStatus.DRAFT;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "rfq_invited_vendors",
            joinColumns = @JoinColumn(name = "rfq_id"),
            inverseJoinColumns = @JoinColumn(name = "vendor_id"))
    private Set<Vendor> invitedVendors = new HashSet<>();

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public enum RFQStatus {
        DRAFT, SENT, OPEN, CLOSED
    }
}
