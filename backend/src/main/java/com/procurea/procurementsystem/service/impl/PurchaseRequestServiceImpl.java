package com.procurea.procurementsystem.service.impl;

import com.procurea.procurementsystem.dto.PurchaseRequestDto;
import com.procurea.procurementsystem.dto.PurchaseRequestItemDto;
import com.procurea.procurementsystem.entity.Approval;
import com.procurea.procurementsystem.entity.Product;
import com.procurea.procurementsystem.entity.PurchaseRequest;
import com.procurea.procurementsystem.entity.PurchaseRequestItem;
import com.procurea.procurementsystem.entity.User;
import com.procurea.procurementsystem.exception.BadRequestException;
import com.procurea.procurementsystem.exception.ResourceNotFoundException;
import com.procurea.procurementsystem.repository.ApprovalRepository;
import com.procurea.procurementsystem.repository.ProductRepository;
import com.procurea.procurementsystem.repository.PurchaseRequestRepository;
import com.procurea.procurementsystem.repository.UserRepository;
import com.procurea.procurementsystem.service.AuditLogService;
import com.procurea.procurementsystem.service.NotificationService;
import com.procurea.procurementsystem.service.PurchaseRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PurchaseRequestServiceImpl implements PurchaseRequestService {

    @Autowired
    private PurchaseRequestRepository prRepository;

    @Autowired
    private ProductRepository productRepository;

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
        pr.setRequestNumber("PR-" + LocalDate.now().getYear() + "-" + String.format("%04d", (prRepository.count() + 1)));
        pr.setTitle(dto.getTitle());
        pr.setDescription(dto.getDescription());
        pr.setDepartment(dto.getDepartment());
        pr.setRequestedBy(user);
        pr.setStatus(PurchaseRequest.RequestStatus.valueOf(dto.getStatus() == null ? "PENDING" : dto.getStatus()));

        double computedTotal = 0.0;

        if (dto.getItems() != null && !dto.getItems().isEmpty()) {
            for (PurchaseRequestItemDto itemDto : dto.getItems()) {
                PurchaseRequestItem item = new PurchaseRequestItem();
                item.setPurchaseRequest(pr);
                if (itemDto.getProductId() != null) {
                    Product product = productRepository.findById(itemDto.getProductId()).orElse(null);
                    item.setProduct(product);
                    item.setProductName(product != null ? product.getName() : itemDto.getProductName());
                    item.setUnitPrice(product != null ? product.getUnitPrice() : (itemDto.getUnitPrice() != null ? itemDto.getUnitPrice() : 0.0));
                } else {
                    item.setProductName(itemDto.getProductName());
                    item.setUnitPrice(itemDto.getUnitPrice() != null ? itemDto.getUnitPrice() : 0.0);
                }
                item.setQuantity(itemDto.getQuantity() != null ? itemDto.getQuantity() : 1);
                item.calculateTotal();
                computedTotal += item.getEstimatedTotal();
                pr.getItems().add(item);
            }
        }

        pr.setTotalAmount(computedTotal > 0 ? computedTotal : (dto.getTotalAmount() != null ? dto.getTotalAmount() : (dto.getEstimatedBudget() != null ? dto.getEstimatedBudget() : 0.0)));
        pr.setEstimatedBudget(dto.getEstimatedBudget() != null ? dto.getEstimatedBudget() : pr.getTotalAmount());

        PurchaseRequest saved = prRepository.save(pr);

        auditLogService.log(user.getUsername(), "CREATE_PURCHASE_REQUEST", "PurchaseRequest", saved.getId(), null, saved.getRequestNumber() + " - " + saved.getTitle());

        if (saved.getStatus() == PurchaseRequest.RequestStatus.PENDING || saved.getStatus() == PurchaseRequest.RequestStatus.DRAFT) {
            notifyManagers(saved);
        }

        return convertToDto(saved);
    }

    @Override
    public Page<PurchaseRequestDto> getAllRequests(PurchaseRequest.RequestStatus status, String department, String search, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
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

        if (pr.getStatus() != PurchaseRequest.RequestStatus.DRAFT && pr.getStatus() != PurchaseRequest.RequestStatus.REJECTED && pr.getStatus() != PurchaseRequest.RequestStatus.PENDING) {
            throw new BadRequestException("Only PENDING, DRAFT or REJECTED requests can be edited");
        }

        String oldVal = pr.getTitle();

        pr.setTitle(dto.getTitle());
        pr.setDescription(dto.getDescription());
        pr.setDepartment(dto.getDepartment());
        if (dto.getEstimatedBudget() != null) pr.setEstimatedBudget(dto.getEstimatedBudget());

        PurchaseRequest updated = prRepository.save(pr);
        String username = pr.getRequestedBy() != null ? pr.getRequestedBy().getUsername() : "SYSTEM";
        auditLogService.log(username, "UPDATE_PURCHASE_REQUEST", "PurchaseRequest", updated.getId(), oldVal, updated.getTitle());

        return convertToDto(updated);
    }

    @Override
    @Transactional
    public PurchaseRequestDto approveRequest(Long id, Long approverId, String comments) {
        PurchaseRequest pr = prRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase request not found"));

        if (pr.getStatus() != PurchaseRequest.RequestStatus.PENDING && pr.getStatus() != PurchaseRequest.RequestStatus.DRAFT) {
            throw new BadRequestException("Request must be in PENDING state to approve");
        }

        User approver = userRepository.findById(approverId)
                .orElseThrow(() -> new ResourceNotFoundException("Approver user not found"));

        pr.setStatus(PurchaseRequest.RequestStatus.APPROVED);
        pr.setApprovalRemarks(comments);
        PurchaseRequest updated = prRepository.save(pr);

        Approval approval = new Approval();
        approval.setPurchaseRequest(updated);
        approval.setApprover(approver);
        approval.setStatus(Approval.ApprovalStatus.APPROVED);
        approval.setComments(comments);
        approvalRepository.save(approval);

        auditLogService.log(approver.getUsername(), "APPROVE_PURCHASE_REQUEST", "PurchaseRequest", updated.getId(), "PENDING", "APPROVED (" + comments + ")");

        if (updated.getRequestedBy() != null) {
            notificationService.sendNotification(
                    updated.getRequestedBy().getId(),
                    "Purchase request " + updated.getRequestNumber() + " (" + updated.getTitle() + ") has been APPROVED.",
                    "REQUEST_APPROVED"
            );
        }

        return convertToDto(updated);
    }

    @Override
    @Transactional
    public PurchaseRequestDto rejectRequest(Long id, Long approverId, String comments) {
        PurchaseRequest pr = prRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase request not found"));

        User approver = userRepository.findById(approverId)
                .orElseThrow(() -> new ResourceNotFoundException("Approver user not found"));

        pr.setStatus(PurchaseRequest.RequestStatus.REJECTED);
        pr.setApprovalRemarks(comments);
        PurchaseRequest updated = prRepository.save(pr);

        Approval approval = new Approval();
        approval.setPurchaseRequest(updated);
        approval.setApprover(approver);
        approval.setStatus(Approval.ApprovalStatus.REJECTED);
        approval.setComments(comments);
        approvalRepository.save(approval);

        auditLogService.log(approver.getUsername(), "REJECT_PURCHASE_REQUEST", "PurchaseRequest", updated.getId(), "PENDING", "REJECTED (" + comments + ")");

        if (updated.getRequestedBy() != null) {
            notificationService.sendNotification(
                    updated.getRequestedBy().getId(),
                    "Purchase request " + updated.getRequestNumber() + " (" + updated.getTitle() + ") was REJECTED. Remarks: " + comments,
                    "REQUEST_REJECTED"
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

        pr.setStatus(PurchaseRequest.RequestStatus.REJECTED);
        PurchaseRequest updated = prRepository.save(pr);

        auditLogService.log(pr.getRequestedBy().getUsername(), "CANCEL_PURCHASE_REQUEST", "PurchaseRequest", updated.getId(), "PENDING", "CANCELLED");

        return convertToDto(updated);
    }

    private void notifyManagers(PurchaseRequest pr) {
        List<User> managers = userRepository.findAll().stream()
                .filter(u -> u.getRoles().stream().anyMatch(r -> 
                        r.getName() == com.procurea.procurementsystem.entity.Role.ERole.ROLE_PROCUREMENT_MANAGER ||
                        r.getName() == com.procurea.procurementsystem.entity.Role.ERole.ROLE_ADMIN))
                .collect(Collectors.toList());
        for (User manager : managers) {
            notificationService.sendNotification(
                    manager.getId(),
                    "New purchase request pending approval: " + pr.getRequestNumber() + " - " + pr.getTitle() + " (₹" + pr.getTotalAmount() + ")",
                    "REQUEST"
            );
        }
    }

    private PurchaseRequestDto convertToDto(PurchaseRequest pr) {
        PurchaseRequestDto dto = new PurchaseRequestDto();
        dto.setId(pr.getId());
        dto.setRequestNumber(pr.getRequestNumber());
        dto.setTitle(pr.getTitle());
        dto.setDescription(pr.getDescription());
        dto.setEstimatedBudget(pr.getEstimatedBudget());
        dto.setTotalAmount(pr.getTotalAmount());
        dto.setDepartment(pr.getDepartment());
        if (pr.getRequestedBy() != null) {
            dto.setRequestedById(pr.getRequestedBy().getId());
            dto.setRequestedByUsername(pr.getRequestedBy().getUsername());
        }
        dto.setStatus(pr.getStatus().name());
        dto.setApprovalRemarks(pr.getApprovalRemarks());
        dto.setCreatedAt(pr.getCreatedAt());
        dto.setUpdatedAt(pr.getUpdatedAt());

        if (pr.getItems() != null) {
            List<PurchaseRequestItemDto> itemDtos = pr.getItems().stream().map(i -> {
                PurchaseRequestItemDto idto = new PurchaseRequestItemDto();
                idto.setId(i.getId());
                if (i.getProduct() != null) idto.setProductId(i.getProduct().getId());
                idto.setProductName(i.getProductName());
                idto.setQuantity(i.getQuantity());
                idto.setUnitPrice(i.getUnitPrice());
                idto.setEstimatedTotal(i.getEstimatedTotal());
                return idto;
            }).collect(Collectors.toList());
            dto.setItems(itemDtos);
        }

        return dto;
    }
}
