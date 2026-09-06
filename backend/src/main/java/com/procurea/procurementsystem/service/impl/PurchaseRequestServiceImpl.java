package com.procurea.procurementsystem.service.impl;

import com.procurea.procurementsystem.dto.PurchaseRequestDto;
import com.procurea.procurementsystem.entity.Approval;
import com.procurea.procurementsystem.entity.PurchaseRequest;
import com.procurea.procurementsystem.entity.User;
import com.procurea.procurementsystem.exception.BadRequestException;
import com.procurea.procurementsystem.exception.ResourceNotFoundException;
import com.procurea.procurementsystem.repository.ApprovalRepository;
import com.procurea.procurementsystem.repository.PurchaseRequestRepository;
import com.procurea.procurementsystem.repository.UserRepository;
import com.procurea.procurementsystem.service.AuditLogService;
import com.procurea.procurementsystem.service.NotificationService;
import com.procurea.procurementsystem.service.PurchaseRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PurchaseRequestServiceImpl implements PurchaseRequestService {

    @Autowired
    private PurchaseRequestRepository prRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ApprovalRepository approvalRepository;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private NotificationService notificationService;

    @Override
    @Transactional
    public PurchaseRequestDto createRequest(PurchaseRequestDto dto, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        PurchaseRequest pr = new PurchaseRequest();
        pr.setTitle(dto.getTitle());
        pr.setDescription(dto.getDescription());
        pr.setEstimatedBudget(dto.getEstimatedBudget());
        pr.setDepartment(dto.getDepartment());
        pr.setRequestedBy(user);
        pr.setStatus(PurchaseRequest.RequestStatus.valueOf(dto.getStatus() == null ? "DRAFT" : dto.getStatus()));

        PurchaseRequest saved = prRepository.save(pr);

        auditLogService.log(user.getUsername(), "CREATE_PURCHASE_REQUEST", "PurchaseRequest", saved.getId(), null, saved.getTitle());

        if (saved.getStatus() == PurchaseRequest.RequestStatus.SUBMITTED) {
            notifyManagers(saved);
        }

        return convertToDto(saved);
    }

    @Override
    public Page<PurchaseRequestDto> getAllRequests(PurchaseRequest.RequestStatus status, String department, String search, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<PurchaseRequest> prs = prRepository.searchRequests(status, department, search, pageable);
        return prs.map(this::convertToDto);
    }

    @Override
    public Page<PurchaseRequestDto> getOwnRequests(Long userId, PurchaseRequest.RequestStatus status, String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<PurchaseRequest> prs = prRepository.searchOwnRequests(userId, status, search, pageable);
        return prs.map(this::convertToDto);
    }

    @Override
    public PurchaseRequestDto getRequestById(Long id) {
        PurchaseRequest pr = prRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase request not found with id: " + id));
        return convertToDto(pr);
    }

    @Override
    @Transactional
    public PurchaseRequestDto updateRequest(Long id, PurchaseRequestDto dto) {
        PurchaseRequest pr = prRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase request not found with id: " + id));

        if (pr.getStatus() != PurchaseRequest.RequestStatus.DRAFT && pr.getStatus() != PurchaseRequest.RequestStatus.REJECTED) {
            throw new BadRequestException("Only DRAFT or REJECTED requests can be updated");
        }

        String oldVal = pr.toString();

        pr.setTitle(dto.getTitle());
        pr.setDescription(dto.getDescription());
        pr.setEstimatedBudget(dto.getEstimatedBudget());
        pr.setDepartment(dto.getDepartment());
        
        if (dto.getStatus() != null) {
            PurchaseRequest.RequestStatus newStatus = PurchaseRequest.RequestStatus.valueOf(dto.getStatus());
            if (newStatus == PurchaseRequest.RequestStatus.SUBMITTED && pr.getStatus() == PurchaseRequest.RequestStatus.DRAFT) {
                pr.setStatus(newStatus);
            }
        }

        PurchaseRequest updated = prRepository.save(pr);
        String username = pr.getRequestedBy() != null ? pr.getRequestedBy().getUsername() : "SYSTEM";
        
        auditLogService.log(username, "UPDATE_PURCHASE_REQUEST", "PurchaseRequest", updated.getId(), oldVal, updated.toString());

        if (updated.getStatus() == PurchaseRequest.RequestStatus.SUBMITTED) {
            notifyManagers(updated);
        }

        return convertToDto(updated);
    }

    @Override
    @Transactional
    public PurchaseRequestDto approveRequest(Long id, Long approverId, String comments) {
        PurchaseRequest pr = prRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase request not found"));

        if (pr.getStatus() != PurchaseRequest.RequestStatus.SUBMITTED) {
            throw new BadRequestException("Request must be in SUBMITTED state to approve");
        }

        User approver = userRepository.findById(approverId)
                .orElseThrow(() -> new ResourceNotFoundException("Approver user not found"));

        pr.setStatus(PurchaseRequest.RequestStatus.APPROVED);
        PurchaseRequest updated = prRepository.save(pr);

        // Record Approval entry
        Approval approval = new Approval();
        approval.setPurchaseRequest(updated);
        approval.setApprover(approver);
        approval.setStatus(Approval.ApprovalStatus.APPROVED);
        approval.setComments(comments);
        approvalRepository.save(approval);

        auditLogService.log(approver.getUsername(), "APPROVE_PURCHASE_REQUEST", "PurchaseRequest", updated.getId(), "SUBMITTED", "APPROVED");

        // Notify Requestor
        if (updated.getRequestedBy() != null) {
            notificationService.sendNotification(
                    updated.getRequestedBy().getId(),
                    "Your purchase request '" + updated.getTitle() + "' has been APPROVED.",
                    "REQUEST"
            );
        }

        return convertToDto(updated);
    }

    @Override
    @Transactional
    public PurchaseRequestDto rejectRequest(Long id, Long approverId, String comments) {
        PurchaseRequest pr = prRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase request not found"));

        if (pr.getStatus() != PurchaseRequest.RequestStatus.SUBMITTED) {
            throw new BadRequestException("Request must be in SUBMITTED state to reject");
        }

        User approver = userRepository.findById(approverId)
                .orElseThrow(() -> new ResourceNotFoundException("Approver user not found"));

        pr.setStatus(PurchaseRequest.RequestStatus.REJECTED);
        PurchaseRequest updated = prRepository.save(pr);

        // Record Approval entry
        Approval approval = new Approval();
        approval.setPurchaseRequest(updated);
        approval.setApprover(approver);
        approval.setStatus(Approval.ApprovalStatus.REJECTED);
        approval.setComments(comments);
        approvalRepository.save(approval);

        auditLogService.log(approver.getUsername(), "REJECT_PURCHASE_REQUEST", "PurchaseRequest", updated.getId(), "SUBMITTED", "REJECTED");

        // Notify Requestor
        if (updated.getRequestedBy() != null) {
            notificationService.sendNotification(
                    updated.getRequestedBy().getId(),
                    "Your purchase request '" + updated.getTitle() + "' has been REJECTED. Comments: " + comments,
                    "REQUEST"
            );
        }

        return convertToDto(updated);
    }

    @Override
    @Transactional
    public PurchaseRequestDto cancelRequest(Long id, Long userId) {
        PurchaseRequest pr = prRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase request not found"));

        if (!pr.getRequestedBy().getId().equals(userId)) {
            throw new BadRequestException("You can only cancel your own purchase requests");
        }

        if (pr.getStatus() == PurchaseRequest.RequestStatus.APPROVED) {
            throw new BadRequestException("Approved requests cannot be cancelled");
        }

        String oldStatus = pr.getStatus().name();
        pr.setStatus(PurchaseRequest.RequestStatus.CANCELLED);
        PurchaseRequest updated = prRepository.save(pr);

        auditLogService.log(pr.getRequestedBy().getUsername(), "CANCEL_PURCHASE_REQUEST", "PurchaseRequest", updated.getId(), oldStatus, "CANCELLED");

        return convertToDto(updated);
    }

    private void notifyManagers(PurchaseRequest pr) {
        List<User> managers = userRepository.findAll().stream()
                .filter(u -> u.getRoles().stream().anyMatch(r -> r.getName() == com.procurea.procurementsystem.entity.Role.ERole.ROLE_PROCUREMENT_MANAGER))
                .collect(Collectors.toList());
        for (User manager : managers) {
            notificationService.sendNotification(
                    manager.getId(),
                    "New purchase request requires approval: '" + pr.getTitle() + "' (Dept: " + pr.getDepartment() + ")",
                    "REQUEST"
            );
        }
    }

    private PurchaseRequestDto convertToDto(PurchaseRequest pr) {
        PurchaseRequestDto dto = new PurchaseRequestDto();
        dto.setId(pr.getId());
        dto.setTitle(pr.getTitle());
        dto.setDescription(pr.getDescription());
        dto.setEstimatedBudget(pr.getEstimatedBudget());
        dto.setDepartment(pr.getDepartment());
        if (pr.getRequestedBy() != null) {
            dto.setRequestedById(pr.getRequestedBy().getId());
            dto.setRequestedByUsername(pr.getRequestedBy().getUsername());
        }
        dto.setStatus(pr.getStatus().name());
        dto.setCreatedAt(pr.getCreatedAt());
        dto.setUpdatedAt(pr.getUpdatedAt());
        return dto;
    }
}
