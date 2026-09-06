package com.procurea.procurementsystem.service;

import com.procurea.procurementsystem.dto.RFQDto;
import com.procurea.procurementsystem.entity.RFQ;
import org.springframework.data.domain.Page;

import java.util.Set;

public interface RFQService {
    RFQDto createRFQ(RFQDto dto);
    RFQDto getRFQById(Long id);
    Page<RFQDto> getAllRFQs(RFQ.RFQStatus status, String search, int page, int size);
    Page<RFQDto> getRFQsForVendor(Long vendorId, RFQ.RFQStatus status, String search, int page, int size);
    RFQDto updateRFQStatus(Long id, RFQ.RFQStatus status);
    RFQDto inviteVendors(Long id, Set<Long> vendorIds);
}
