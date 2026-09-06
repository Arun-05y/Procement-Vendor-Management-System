package com.procurea.procurementsystem.repository;

import com.procurea.procurementsystem.entity.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findByPurchaseOrderId(Long poId);
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    @Query("SELECT i FROM Invoice i WHERE i.status != 'PAID' AND i.paymentDueDate < :currentDate")
    List<Invoice> findOverdueInvoices(@Param("currentDate") LocalDate currentDate);

    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.status != 'PAID' AND i.paymentDueDate < :currentDate")
    long countOverdueInvoices(@Param("currentDate") LocalDate currentDate);

    @Query("SELECT SUM(i.invoiceAmount - i.paidAmount) FROM Invoice i WHERE i.status != 'PAID'")
    Double sumPendingInvoiceAmount();

    @Query("SELECT i FROM Invoice i WHERE " +
           "(:status IS NULL OR i.status = :status) AND " +
           "(:search IS NULL OR LOWER(i.invoiceNumber) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(i.purchaseOrder.poNumber) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(i.vendor.companyName) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Invoice> searchInvoices(
            @Param("status") Invoice.PaymentStatus status,
            @Param("search") String search,
            Pageable pageable
    );

    @Query("SELECT i FROM Invoice i WHERE i.vendor.id = :vendorId AND " +
           "(:status IS NULL OR i.status = :status) AND " +
           "(:search IS NULL OR LOWER(i.invoiceNumber) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(i.purchaseOrder.poNumber) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Invoice> searchInvoicesForVendor(
            @Param("vendorId") Long vendorId,
            @Param("status") Invoice.PaymentStatus status,
            @Param("search") String search,
            Pageable pageable
    );
}
