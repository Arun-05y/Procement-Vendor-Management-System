package com.procurea.procurementsystem.service.impl;

import com.procurea.procurementsystem.dto.VendorDto;
import com.procurea.procurementsystem.entity.User;
import com.procurea.procurementsystem.entity.Vendor;
import com.procurea.procurementsystem.exception.BadRequestException;
import com.procurea.procurementsystem.exception.ResourceNotFoundException;
import com.procurea.procurementsystem.repository.UserRepository;
import com.procurea.procurementsystem.repository.VendorRepository;
import com.procurea.procurementsystem.service.AuditLogService;
import com.procurea.procurementsystem.service.NotificationService;
import com.procurea.procurementsystem.service.VendorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class VendorServiceImpl implements VendorService {

    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private NotificationService notificationService;

    @Override
    @Transactional
    public VendorDto registerVendor(VendorDto dto, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found for vendor registration"));

        if (vendorRepository.findByUserId(userId).isPresent()) {
            throw new BadRequestException("Vendor profile already exists for this user");
        }

        Vendor vendor = new Vendor();
        vendor.setUser(user);
        vendor.setVendorCode("VEND-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        vendor.setCompanyName(dto.getCompanyName());
        vendor.setContactPerson(dto.getContactPerson());
        vendor.setEmail(dto.getEmail());
        vendor.setPhoneNumber(dto.getPhoneNumber());
        vendor.setAddress(dto.getAddress());
        vendor.setCity(dto.getCity());
        vendor.setState(dto.getState());
        vendor.setCountry(dto.getCountry());
        vendor.setGstNumber(dto.getGstNumber());
        vendor.setPanNumber(dto.getPanNumber());
        vendor.setCategory(dto.getCategory());
        
        vendor.setBankAccountNumber(dto.getBankAccountNumber());
        vendor.setBankName(dto.getBankName());
        vendor.setBankIfscCode(dto.getBankIfscCode());
        
        vendor.setContractStartDate(dto.getContractStartDate());
        vendor.setContractEndDate(dto.getContractEndDate());
        vendor.setStatus(Vendor.VendorStatus.PENDING_APPROVAL);

        Vendor saved = vendorRepository.save(vendor);
        
        auditLogService.log(user.getUsername(), "REGISTER_VENDOR", "Vendor", saved.getId(), null, saved.getCompanyName());
        
        // Notify Admins
        List<User> admins = userRepository.findAll().stream()
                .filter(u -> u.getRoles().stream().anyMatch(r -> r.getName() == com.procurea.procurementsystem.entity.Role.ERole.ROLE_ADMIN))
                .collect(Collectors.toList());
        for (User admin : admins) {
            notificationService.sendNotification(admin.getId(), "New Vendor registration pending approval: " + saved.getCompanyName(), "VENDOR");
        }

        return convertToDto(saved);
    }

    @Override
    public Page<VendorDto> getAllVendors(Vendor.VendorStatus status, String category, String search, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Vendor> vendors = vendorRepository.searchVendors(status, category, search, pageable);
        return vendors.map(this::convertToDto);
    }

    @Override
    public List<VendorDto> getActiveVendors() {
        return vendorRepository.findByStatus(Vendor.VendorStatus.ACTIVE).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public VendorDto getVendorById(Long id) {
        Vendor vendor = vendorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found with id: " + id));
        return convertToDto(vendor);
    }

    @Override
    public VendorDto getVendorByUserId(Long userId) {
        Vendor vendor = vendorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor profile not found for user: " + userId));
        return convertToDto(vendor);
    }

    @Override
    @Transactional
    public VendorDto updateVendor(Long id, VendorDto dto) {
        Vendor vendor = vendorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found with id: " + id));

        String oldVal = vendor.toString();

        vendor.setCompanyName(dto.getCompanyName());
        vendor.setContactPerson(dto.getContactPerson());
        vendor.setPhoneNumber(dto.getPhoneNumber());
        vendor.setAddress(dto.getAddress());
        vendor.setCity(dto.getCity());
        vendor.setState(dto.getState());
        vendor.setCountry(dto.getCountry());
        vendor.setGstNumber(dto.getGstNumber());
        vendor.setPanNumber(dto.getPanNumber());
        vendor.setCategory(dto.getCategory());
        vendor.setBankAccountNumber(dto.getBankAccountNumber());
        vendor.setBankName(dto.getBankName());
        vendor.setBankIfscCode(dto.getBankIfscCode());
        vendor.setContractStartDate(dto.getContractStartDate());
        vendor.setContractEndDate(dto.getContractEndDate());

        Vendor updated = vendorRepository.save(vendor);
        
        String username = (vendor.getUser() != null) ? vendor.getUser().getUsername() : "SYSTEM";
        auditLogService.log(username, "UPDATE_VENDOR", "Vendor", updated.getId(), oldVal, updated.toString());

        return convertToDto(updated);
    }

    @Override
    @Transactional
    public VendorDto updateVendorStatus(Long id, Vendor.VendorStatus status) {
        Vendor vendor = vendorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found with id: " + id));

        String oldVal = vendor.getStatus().name();
        vendor.setStatus(status);
        Vendor updated = vendorRepository.save(vendor);

        auditLogService.log("SYSTEM", "UPDATE_VENDOR_STATUS", "Vendor", updated.getId(), oldVal, status.name());

        // Notify vendor user
        if (vendor.getUser() != null) {
            notificationService.sendNotification(
                    vendor.getUser().getId(),
                    "Your Vendor profile status has been updated to: " + status.name(),
                    "VENDOR"
            );
        }

        return convertToDto(updated);
    }

    @Override
    @Transactional
    public void deleteVendor(Long id) {
        Vendor vendor = vendorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found with id: " + id));
        vendorRepository.delete(vendor);
        auditLogService.log("SYSTEM", "DELETE_VENDOR", "Vendor", id, vendor.getCompanyName(), "DELETED");
    }

    @Override
    @Transactional
    public void updatePerformanceMetrics(Long vendorId, Double onTimeRate, Double fulfillmentRate, Double qualityScore, Double responseHours) {
        Vendor vendor = vendorRepository.findById(vendorId).orElse(null);
        if (vendor != null) {
            vendor.setOnTimeDeliveryRate(onTimeRate);
            vendor.setFulfillmentRate(fulfillmentRate);
            vendor.setQualityRating(qualityScore);
            vendor.setResponseTimeHours(responseHours);
            
            // Weighted Performance Score: 30% Delivery, 25% Quality, 20% Price (mapped to Fulfillment), 15% Reliability, 10% Response Time
            // Let's simplify: 
            // Delivery (onTimeRate) -> 30%
            // Quality (qualityScore * 20 to scale 1-5 to 100) -> 25%
            // Fulfillment -> 20%
            // Reliability (e.g. 100 - rejectionRate, let's use 100 here as base) -> 15%
            // Response Time (scaled: e.g. 100 - hours*4, max 100, min 0) -> 10%
            double deliveryWeight = onTimeRate * 0.30;
            double qualityWeight = (qualityScore * 20.0) * 0.25;
            double fulfillmentWeight = fulfillmentRate * 0.20;
            double reliabilityWeight = 100.0 * 0.15;
            double responseWeight = Math.max(0.0, 100.0 - responseHours * 4) * 0.10;
            
            double totalScore = deliveryWeight + qualityWeight + fulfillmentWeight + reliabilityWeight + responseWeight;
            vendor.setPerformanceScore(totalScore);
            
            if (totalScore >= 85) {
                vendor.setPerformanceCategory("EXCELLENT");
            } else if (totalScore >= 70) {
                vendor.setPerformanceCategory("GOOD");
            } else if (totalScore >= 50) {
                vendor.setPerformanceCategory("AVERAGE");
            } else {
                vendor.setPerformanceCategory("POOR");
            }
            
            vendorRepository.save(vendor);
        }
    }

    private VendorDto convertToDto(Vendor vendor) {
        VendorDto dto = new VendorDto();
        dto.setId(vendor.getId());
        if (vendor.getUser() != null) {
            dto.setUserId(vendor.getUser().getId());
            dto.setUsername(vendor.getUser().getUsername());
        }
        dto.setVendorCode(vendor.getVendorCode());
        dto.setCompanyName(vendor.getCompanyName());
        dto.setContactPerson(vendor.getContactPerson());
        dto.setEmail(vendor.getEmail());
        dto.setPhoneNumber(vendor.getPhoneNumber());
        dto.setAddress(vendor.getAddress());
        dto.setCity(vendor.getCity());
        dto.setState(vendor.getState());
        dto.setCountry(vendor.getCountry());
        dto.setGstNumber(vendor.getGstNumber());
        dto.setPanNumber(vendor.getPanNumber());
        dto.setCategory(vendor.getCategory());
        
        dto.setBankAccountNumber(vendor.getBankAccountNumber());
        dto.setBankName(vendor.getBankName());
        dto.setBankIfscCode(vendor.getBankIfscCode());
        
        dto.setContractStartDate(vendor.getContractStartDate());
        dto.setContractEndDate(vendor.getContractEndDate());
        dto.setStatus(vendor.getStatus().name());
        dto.setRating(vendor.getRating());
        
        dto.setOnTimeDeliveryRate(vendor.getOnTimeDeliveryRate());
        dto.setFulfillmentRate(vendor.getFulfillmentRate());
        dto.setQualityRating(vendor.getQualityRating());
        dto.setResponseTimeHours(vendor.getResponseTimeHours());
        dto.setPerformanceScore(vendor.getPerformanceScore());
        dto.setPerformanceCategory(vendor.getPerformanceCategory());
        
        dto.setCreatedAt(vendor.getCreatedAt());
        dto.setUpdatedAt(vendor.getUpdatedAt());
        return dto;
    }
}
