package com.procurea.procurementsystem.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    @JoinColumn(name = "request_id")
    private ProcurementRequest request;

    private LocalDateTime deadline;
    
    @Enumerated(EnumType.STRING)
    private RFQStatus status = RFQStatus.OPEN;

    @ManyToMany
    @JoinTable(name = "rfq_invited_vendors",
            joinColumns = @JoinColumn(name = "rfq_id"),
            inverseJoinColumns = @JoinColumn(name = "vendor_id"))
    private Set<Vendor> invitedVendors = new HashSet<>();

    public enum RFQStatus {
        OPEN, CLOSED, COMPLETED, CANCELLED
    }
}
