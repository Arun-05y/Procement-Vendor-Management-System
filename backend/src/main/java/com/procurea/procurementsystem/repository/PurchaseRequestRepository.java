package com.procurea.procurementsystem.repository;

import com.procurea.procurementsystem.entity.PurchaseRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseRequestRepository extends JpaRepository<PurchaseRequest, Long> {
    List<PurchaseRequest> findByRequestedById(Long userId);
    
    @Query("SELECT pr FROM PurchaseRequest pr WHERE " +
           "(:status IS NULL OR pr.status = :status) AND " +
           "(:department IS NULL OR pr.department = :department) AND " +
           "(:search IS NULL OR LOWER(pr.title) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(pr.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<PurchaseRequest> searchRequests(
            @Param("status") PurchaseRequest.RequestStatus status,
            @Param("department") String department,
            @Param("search") String search,
            Pageable pageable
    );

    @Query("SELECT pr FROM PurchaseRequest pr WHERE pr.requestedBy.id = :userId AND " +
           "(:status IS NULL OR pr.status = :status) AND " +
           "(:search IS NULL OR LOWER(pr.title) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<PurchaseRequest> searchOwnRequests(
            @Param("userId") Long userId,
            @Param("status") PurchaseRequest.RequestStatus status,
            @Param("search") String search,
            Pageable pageable
    );
}
