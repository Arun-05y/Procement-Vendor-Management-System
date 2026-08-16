package com.procurea.procurementsystem.service;

import com.procurea.procurementsystem.model.Delivery;
import com.procurea.procurementsystem.model.PurchaseOrder;
import com.procurea.procurementsystem.repository.DeliveryRepository;
import com.procurea.procurementsystem.repository.PurchaseOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class DeliveryService {

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Autowired
    private PurchaseOrderRepository poRepository;

    public Delivery trackDelivery(Long poId, String trackingNumber, String carrier) {
        PurchaseOrder po = poRepository.findById(poId)
                .orElseThrow(() -> new RuntimeException("Purchase Order not found"));

        Optional<Delivery> existingDelivery = deliveryRepository.findByPurchaseOrderId(poId);
        if (existingDelivery.isPresent()) {
            throw new RuntimeException("Delivery is already being tracked for this Purchase Order");
        }

        Delivery delivery = new Delivery();
        delivery.setPurchaseOrder(po);
        delivery.setTrackingNumber(trackingNumber);
        delivery.setCarrier(carrier);
        delivery.setStatus(Delivery.DeliveryStatus.IN_TRANSIT);
        
        // Update PO status to SHIPPED
        po.setStatus(PurchaseOrder.POStatus.SHIPPED);
        poRepository.save(po);

        return deliveryRepository.save(delivery);
    }

    public Delivery updateDeliveryStatus(Long id, Delivery.DeliveryStatus status) {
        Delivery delivery = deliveryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Delivery tracking not found"));

        delivery.setStatus(status);
        if (status == Delivery.DeliveryStatus.DELIVERED) {
            delivery.setDeliveryDate(LocalDateTime.now());
            // Update PO status to DELIVERED
            PurchaseOrder po = delivery.getPurchaseOrder();
            if (po != null) {
                po.setStatus(PurchaseOrder.POStatus.DELIVERED);
                poRepository.save(po);
            }
        }
        return deliveryRepository.save(delivery);
    }

    public List<Delivery> getAllDeliveries() {
        return deliveryRepository.findAll();
    }

    public Optional<Delivery> getDeliveryById(Long id) {
        return deliveryRepository.findById(id);
    }

    public Optional<Delivery> getDeliveryByPoId(Long poId) {
        return deliveryRepository.findByPurchaseOrderId(poId);
    }
}
