package com.procurea.procurementsystem.repository;

import com.procurea.procurementsystem.entity.Vendor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VendorRepository extends JpaRepository<Vendor, Long> {
    Optional<Vendor> findByUserId(Long userId);
    List<Vendor> findByStatus(Vendor.VendorStatus status);
    Optional<Vendor> findByVendorCode(String vendorCode);

    @Query("SELECT v FROM Vendor v WHERE " +
           "(:status IS NULL OR v.status = :status) AND " +
           "(:category IS NULL OR LOWER(v.category) = LOWER(:category)) AND " +
           "(:search IS NULL OR LOWER(v.companyName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(v.vendorCode) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(v.contactPerson) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(v.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Vendor> searchVendors(
            @Param("status") Vendor.VendorStatus status,
            @Param("category") String category,
            @Param("search") String search,
            Pageable pageable
    );
}
