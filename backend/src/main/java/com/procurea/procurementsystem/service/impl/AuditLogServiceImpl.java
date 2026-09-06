package com.procurea.procurementsystem.service.impl;

import com.procurea.procurementsystem.dto.AuditLogDto;
import com.procurea.procurementsystem.entity.AuditLog;
import com.procurea.procurementsystem.repository.AuditLogRepository;
import com.procurea.procurementsystem.service.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class AuditLogServiceImpl implements AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Override
    public void log(String username, String action, String entityName, Long entityId, String oldValue, String newValue) {
        AuditLog log = new AuditLog();
        log.setUsername(username);
        log.setAction(action);
        log.setEntityName(entityName);
        log.setEntityId(entityId);
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
        auditLogRepository.save(log);
    }

    @Override
    public Page<AuditLogDto> getAllLogs(String username, String action, String entityName, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        Page<AuditLog> logs = auditLogRepository.searchLogs(
                username == null || username.isEmpty() ? null : username,
                action == null || action.isEmpty() ? null : action,
                entityName == null || entityName.isEmpty() ? null : entityName,
                pageable
        );
        return logs.map(this::convertToDto);
    }

    private AuditLogDto convertToDto(AuditLog log) {
        return new AuditLogDto(
                log.getId(),
                log.getUsername(),
                log.getAction(),
                log.getEntityName(),
                log.getEntityId(),
                log.getTimestamp(),
                log.getIpAddress(),
                log.getOldValue(),
                log.getNewValue()
        );
    }
}
