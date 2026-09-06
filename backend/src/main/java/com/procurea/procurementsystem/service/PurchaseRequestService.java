package com.procurea.procurementsystem.service;

import com.procurea.procurementsystem.dto.PurchaseRequestDto;
import com.procurea.procurementsystem.entity.PurchaseRequest;
import org.springframework.data.domain.Page;

public interface PurchaseRequestService {
    PurchaseRequestDto createRequest(PurchaseRequestDto dto, Long userId);
    Page<PurchaseRequestDto> getAllRequests(PurchaseRequest.RequestStatus status, String department, String search, int page, int size, String sortBy, String sortDir);
    Page<PurchaseRequestDto> getOwnRequests(Long userId, PurchaseRequest.RequestStatus status, String search, int page, int size);
    PurchaseRequestDto getRequestById(Long id);
    PurchaseRequestDto updateRequest(Long id, PurchaseRequestDto dto);
    PurchaseRequestDto approveRequest(Long id, Long approverId, String comments);
    PurchaseRequestDto rejectRequest(Long id, Long approverId, String comments);
    PurchaseRequestDto cancelRequest(Long id, Long userId);
}
