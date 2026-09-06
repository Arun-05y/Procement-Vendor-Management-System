package com.procurea.procurementsystem.repository;

import com.procurea.procurementsystem.entity.RFQ;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RFQRepository extends JpaRepository<RFQ, Long> {
    
    @Query("SELECT r FROM RFQ r WHERE " +
           "(:status IS NULL OR r.status = :status) AND " +
           "(:search IS NULL OR LOWER(r.title) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(r.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<RFQ> searchRFQs(
            @Param("status") RFQ.RFQStatus status,
            @Param("search") String search,
            Pageable pageable
    );

    @Query("SELECT r FROM RFQ r JOIN r.invitedVendors iv WHERE iv.id = :vendorId AND " +
           "(:status IS NULL OR r.status = :status) AND " +
           "(:search IS NULL OR LOWER(r.title) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<RFQ> searchRFQsForVendor(
            @Param("vendorId") Long vendorId,
            @Param("status") RFQ.RFQStatus status,
            @Param("search") String search,
            Pageable pageable
    );
}
