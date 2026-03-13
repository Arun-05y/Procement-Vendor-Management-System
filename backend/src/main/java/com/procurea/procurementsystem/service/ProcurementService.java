package com.procurea.procurementsystem.service;

import com.procurea.procurementsystem.model.ProcurementRequest;
import com.procurea.procurementsystem.model.RFQ;
import com.procurea.procurementsystem.repository.ProcurementRequestRepository;
import com.procurea.procurementsystem.repository.RFQRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
public class ProcurementService {
    @Autowired
    private ProcurementRequestRepository requestRepository;

    @Autowired
    private RFQRepository rfqRepository;

    public ProcurementRequest createRequest(ProcurementRequest request) {
        return requestRepository.save(request);
    }

    public List<ProcurementRequest> getAllRequests() {
        return requestRepository.findAll();
    }

    public ProcurementRequest approveRequest(Long id) {
        ProcurementRequest request = requestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Request not found"));
        
        // Mock Budget Validation
        if (request.getEstimatedBudget() > 50000) {
            throw new RuntimeException("Request exceeds department budget limit for auto-approval");
        }

        request.setStatus(ProcurementRequest.RequestStatus.APPROVED);
        return requestRepository.save(request);
    }

    public RFQ createRFQ(Long requestId, LocalDateTime deadline, Set<Long> vendorIds) {
        ProcurementRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));
        
        if (request.getStatus() != ProcurementRequest.RequestStatus.APPROVED) {
            throw new RuntimeException("Cannot generate RFQ for unapproved request");
        }

        request.setStatus(ProcurementRequest.RequestStatus.RFQ_GENERATED);
        requestRepository.save(request);

        RFQ rfq = new RFQ();
        rfq.setRequest(request);
        rfq.setDeadline(deadline);
        
        // Add invited vendors if provided
        return rfqRepository.save(rfq);
    }
}
