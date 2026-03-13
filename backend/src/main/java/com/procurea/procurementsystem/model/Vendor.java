package com.procurea.procurementsystem.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String companyName;
    private String taxId;
    private String address;
    private String phoneNumber;
    private String website;
    
    @Enumerated(EnumType.STRING)
    private VendorStatus status = VendorStatus.PENDING;

    private Double rating = 0.0;
    private LocalDateTime registeredAt = LocalDateTime.now();

    public enum VendorStatus {
        PENDING, APPROVED, REJECTED, SUSPENDED
    }
}
