package com.procurea.procurementsystem.service;

import com.procurea.procurementsystem.model.PurchaseOrder;
import com.procurea.procurementsystem.model.Quotation;
import com.procurea.procurementsystem.repository.PurchaseOrderRepository;
import com.procurea.procurementsystem.repository.QuotationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PurchaseOrderService {
    @Autowired
    private PurchaseOrderRepository poRepository;

    @Autowired
    private QuotationRepository quotationRepository;

    public PurchaseOrder generatePO(Long quotationId) {
        Quotation quotation = quotationRepository.findById(quotationId)
                .orElseThrow(() -> new RuntimeException("Quotation not found"));

        quotation.setStatus(Quotation.QuotationStatus.ACCEPTED);
        quotationRepository.save(quotation);

        PurchaseOrder po = new PurchaseOrder();
        po.setQuotation(quotation);
        po.setPoNumber("PO-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        return poRepository.save(po);
    }

    public PurchaseOrder updatePOStatus(Long id, PurchaseOrder.POStatus status) {
        PurchaseOrder po = poRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("PO not found"));
        po.setStatus(status);
        return poRepository.save(po);
    }
}
