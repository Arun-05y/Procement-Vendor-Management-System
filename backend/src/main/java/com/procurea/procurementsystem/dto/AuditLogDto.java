package com.procurea.procurementsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogDto {
    private Long id;
    private String username;
    private String action;
    private String entityName;
    private Long entityId;
    private LocalDateTime timestamp;
    private String ipAddress;
    private String oldValue;
    private String newValue;
}
