package com.procurea.procurementsystem.repository;

import com.procurea.procurementsystem.model.ProcurementRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProcurementRequestRepository extends JpaRepository<ProcurementRequest, Long> {
    List<ProcurementRequest> findByRequestedById(Long userId);
}
