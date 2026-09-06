package com.procurea.procurementsystem.service;

import com.procurea.procurementsystem.dto.DeliveryDto;
import com.procurea.procurementsystem.entity.Delivery;
import org.springframework.data.domain.Page;

public interface DeliveryService {
    DeliveryDto createDelivery(Long poId, String trackingNumber, String carrier, String notes);
    DeliveryDto getDeliveryById(Long id);
    DeliveryDto getDeliveryByPoId(Long poId);
    Page<DeliveryDto> getAllDeliveries(Delivery.DeliveryStatus status, String search, int page, int size);
    Page<DeliveryDto> getDeliveriesForVendor(Long vendorId, Delivery.DeliveryStatus status, String search, int page, int size);
    DeliveryDto updateDeliveryStatus(Long id, Delivery.DeliveryStatus status, String notes);
    DeliveryDto performQualityCheck(Long id, Double qualityScore, Double rejectionRate, String qualityComments);
}
