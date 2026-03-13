package com.procurea.procurementsystem.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "procurement_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcurementRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private Double estimatedBudget;
    private String department;
    
    @ManyToOne
    @JoinColumn(name = "requested_by")
    private User requestedBy;

    @Enumerated(EnumType.STRING)
    private RequestStatus status = RequestStatus.DRAFT;

    private LocalDateTime createdAt = LocalDateTime.now();

    public enum RequestStatus {
        DRAFT, SUBMITTED, APPROVED, REJECTED, RFQ_GENERATED
    }
}
