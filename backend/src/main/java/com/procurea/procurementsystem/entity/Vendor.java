package com.procurea.procurementsystem.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "vendors")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Vendor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(unique = true, length = 30)
    private String vendorCode;

    @NotBlank
    private String companyName;

    private String contactPerson;

    @NotBlank
    @Email
    private String email;

    private String phoneNumber;
    private String address;
    private String city;
    private String state;
    private String country;

    @Column(length = 20)
    private String gstNumber;

    @Column(length = 15)
    private String panNumber;

    private String category; // e.g., Electronics, Construction, IT Services, Raw Materials

    private String paymentTerms = "Net 30"; // Net 15, Net 30, Net 60, Advance, Immediate

    // Bank Details
    private String bankAccountNumber;
    private String bankName;
    private String bankIfscCode;

    private LocalDate contractStartDate;
    private LocalDate contractEndDate;

    @Enumerated(EnumType.STRING)
    @Column(length = 25)
    private VendorStatus status = VendorStatus.ACTIVE;

    private Double rating = 4.5;
    
    // Performance analytics metrics
    private Double onTimeDeliveryRate = 95.0;
    private Double fulfillmentRate = 98.0;
    private Double qualityRating = 4.5;
    private Double responseTimeHours = 12.0;
    private Double performanceScore = 92.0;
    
    private Integer completedOrdersCount = 0;
    private Integer delayedOrdersCount = 0;
    private Integer cancelledOrdersCount = 0;

    @Column(length = 20)
    private String performanceCategory = "EXCELLENT"; // EXCELLENT, GOOD, AVERAGE, POOR

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum VendorStatus {
        ACTIVE, INACTIVE, SUSPENDED, PENDING_APPROVAL, CONTRACT_EXPIRED
    }
}
