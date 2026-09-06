package com.procurea.procurementsystem.repository;

import com.procurea.procurementsystem.entity.Quotation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuotationRepository extends JpaRepository<Quotation, Long> {
    List<Quotation> findByRfqId(Long rfqId);
    List<Quotation> findByVendorId(Long vendorId);
    Page<Quotation> findByVendorId(Long vendorId, Pageable pageable);
}
