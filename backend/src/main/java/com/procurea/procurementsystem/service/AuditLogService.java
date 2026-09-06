package com.procurea.procurementsystem.service;

import com.procurea.procurementsystem.dto.AuditLogDto;
import org.springframework.data.domain.Page;

public interface AuditLogService {
    void log(String username, String action, String entityName, Long entityId, String oldValue, String newValue);
    Page<AuditLogDto> getAllLogs(String username, String action, String entityName, int page, int size);
}
