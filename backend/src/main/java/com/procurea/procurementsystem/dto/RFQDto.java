package com.procurea.procurementsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RFQDto {
    private Long id;
    private Long purchaseRequestId;
    private String purchaseRequestTitle;
    private String title;
    private String description;
    private LocalDateTime deadline;
    private String status;
    private Set<Long> invitedVendorIds;
    private Set<String> invitedVendorNames;
    private LocalDateTime createdAt;
}
