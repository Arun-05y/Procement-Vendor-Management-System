package com.procurea.procurementsystem.repository;

import com.procurea.procurementsystem.entity.PurchaseOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {
    
    @Query("SELECT po FROM PurchaseOrder po WHERE " +
           "(:status IS NULL OR po.status = :status) AND " +
           "(:search IS NULL OR LOWER(po.poNumber) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(po.quotation.vendor.companyName) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<PurchaseOrder> searchPurchaseOrders(
            @Param("status") PurchaseOrder.POStatus status,
            @Param("search") String search,
            Pageable pageable
    );

    @Query("SELECT po FROM PurchaseOrder po WHERE po.quotation.vendor.id = :vendorId AND " +
           "(:status IS NULL OR po.status = :status) AND " +
           "(:search IS NULL OR LOWER(po.poNumber) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<PurchaseOrder> searchPurchaseOrdersForVendor(
            @Param("vendorId") Long vendorId,
            @Param("status") PurchaseOrder.POStatus status,
            @Param("search") String search,
            Pageable pageable
    );
}
