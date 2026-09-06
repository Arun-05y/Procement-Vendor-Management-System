package com.procurea.procurementsystem.service;

import com.procurea.procurementsystem.dto.VendorDto;
import com.procurea.procurementsystem.entity.Vendor;
import org.springframework.data.domain.Page;

import java.util.List;

public interface VendorService {
    VendorDto registerVendor(VendorDto vendorDto, Long userId);
    Page<VendorDto> getAllVendors(Vendor.VendorStatus status, String category, String search, int page, int size, String sortBy, String sortDir);
    List<VendorDto> getActiveVendors();
    VendorDto getVendorById(Long id);
    VendorDto getVendorByUserId(Long userId);
    VendorDto updateVendor(Long id, VendorDto vendorDto);
    VendorDto updateVendorStatus(Long id, Vendor.VendorStatus status);
    void deleteVendor(Long id);
    void updatePerformanceMetrics(Long vendorId, Double onTimeRate, Double fulfillmentRate, Double qualityScore, Double responseHours);
}
