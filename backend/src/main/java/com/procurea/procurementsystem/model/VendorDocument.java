package com.procurea.procurementsystem.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "vendor_documents")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VendorDocument {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "vendor_id")
    private Vendor vendor;

    private String documentType; // e.g., Tax Certificate, License
    private String fileName;
    private String fileUrl;
    private LocalDateTime uploadedAt = LocalDateTime.now();
}
