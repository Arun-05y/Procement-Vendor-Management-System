package com.procurea.procurementsystem.repository;

import com.procurea.procurementsystem.model.RFQ;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RFQRepository extends JpaRepository<RFQ, Long> {
}
