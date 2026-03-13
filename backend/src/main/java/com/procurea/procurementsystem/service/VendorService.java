package com.procurea.procurementsystem.service;

import com.procurea.procurementsystem.model.Vendor;
import com.procurea.procurementsystem.repository.VendorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class VendorService {
    @Autowired
    private VendorRepository vendorRepository;

    public Vendor registerVendor(Vendor vendor) {
        return vendorRepository.save(vendor);
    }

    public List<Vendor> getAllVendors() {
        return vendorRepository.findAll();
    }

    public Optional<Vendor> getVendorById(Long id) {
        return vendorRepository.findById(id);
    }

    public List<Vendor> getVendorsByStatus(Vendor.VendorStatus status) {
        return vendorRepository.findByStatus(status);
    }

    public Vendor updateVendorStatus(Long id, Vendor.VendorStatus status) {
        Vendor vendor = vendorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vendor not found"));
        vendor.setStatus(status);
        return vendorRepository.save(vendor);
    }
}
