package com.procurea.procurementsystem.repository;

import com.procurea.procurementsystem.entity.Delivery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
    Optional<Delivery> findByPurchaseOrderId(Long poId);

    @Query("SELECT d FROM Delivery d WHERE " +
           "(:status IS NULL OR d.status = :status) AND " +
           "(:search IS NULL OR LOWER(d.trackingNumber) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(d.purchaseOrder.poNumber) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(d.purchaseOrder.quotation.vendor.companyName) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Delivery> searchDeliveries(
            @Param("status") Delivery.DeliveryStatus status,
            @Param("search") String search,
            Pageable pageable
    );

    @Query("SELECT d FROM Delivery d WHERE d.purchaseOrder.quotation.vendor.id = :vendorId AND " +
           "(:status IS NULL OR d.status = :status) AND " +
           "(:search IS NULL OR LOWER(d.trackingNumber) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(d.purchaseOrder.poNumber) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Delivery> searchDeliveriesForVendor(
            @Param("vendorId") Long vendorId,
            @Param("status") Delivery.DeliveryStatus status,
            @Param("search") String search,
            Pageable pageable
    );
}
