package com.procurea.procurementsystem.repository;

import com.procurea.procurementsystem.entity.VendorEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VendorEvaluationRepository extends JpaRepository<VendorEvaluation, Long> {
    List<VendorEvaluation> findByVendorId(Long vendorId);
    List<VendorEvaluation> findByPurchaseOrderId(Long purchaseOrderId);
}
