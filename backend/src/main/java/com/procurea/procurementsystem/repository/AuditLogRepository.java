package com.procurea.procurementsystem.repository;

import com.procurea.procurementsystem.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    @Query("SELECT al FROM AuditLog al WHERE " +
           "(:username IS NULL OR LOWER(al.username) LIKE LOWER(CONCAT('%', :username, '%'))) AND " +
           "(:action IS NULL OR al.action = :action) AND " +
           "(:module IS NULL OR al.module = :module) AND " +
           "(:entityName IS NULL OR al.entityName = :entityName)")
    Page<AuditLog> searchLogs(
            @Param("username") String username,
            @Param("action") String action,
            @Param("module") String module,
            @Param("entityName") String entityName,
            Pageable pageable
    );
}
