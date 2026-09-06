package com.procurea.procurementsystem.service;

import com.procurea.procurementsystem.dto.PurchaseOrderDto;
import com.procurea.procurementsystem.entity.PurchaseOrder;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;

public interface PurchaseOrderService {
    PurchaseOrderDto createPurchaseOrder(Long quotationId, String deliveryAddress, LocalDateTime expectedDeliveryDate, String termsAndConditions);
    PurchaseOrderDto createPurchaseOrderFromRequest(Long purchaseRequestId, Long vendorId, String deliveryAddress, LocalDateTime expectedDeliveryDate, Double discount, String termsAndConditions);
    PurchaseOrderDto createDirectPO(PurchaseOrderDto dto);
    PurchaseOrderDto getPurchaseOrderById(Long id);
    Page<PurchaseOrderDto> getAllPurchaseOrders(PurchaseOrder.POStatus status, String search, int page, int size);
    Page<PurchaseOrderDto> getPurchaseOrdersForVendor(Long vendorId, PurchaseOrder.POStatus status, String search, int page, int size);
    PurchaseOrderDto updatePOStatus(Long id, PurchaseOrder.POStatus status);
    PurchaseOrderDto acknowledgePO(Long id, Long vendorId);
}
