package com.procurea.procurementsystem.repository;

import com.procurea.procurementsystem.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByPurchaseOrderId(Long poId);

    @Query("SELECT p FROM Payment p WHERE " +
           "(:status IS NULL OR p.status = :status) AND " +
           "(:search IS NULL OR LOWER(p.transactionReference) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(p.purchaseOrder.poNumber) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(p.purchaseOrder.quotation.vendor.companyName) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Payment> searchPayments(
            @Param("status") Payment.PaymentStatus status,
            @Param("search") String search,
            Pageable pageable
    );

    @Query("SELECT p FROM Payment p WHERE p.purchaseOrder.quotation.vendor.id = :vendorId AND " +
           "(:status IS NULL OR p.status = :status) AND " +
           "(:search IS NULL OR LOWER(p.transactionReference) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(p.purchaseOrder.poNumber) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Payment> searchPaymentsForVendor(
            @Param("vendorId") Long vendorId,
            @Param("status") Payment.PaymentStatus status,
            @Param("search") String search,
            Pageable pageable
    );
}
