package com.procurea.procurementsystem.controller;

import com.procurea.procurementsystem.entity.Payment;
import com.procurea.procurementsystem.entity.PurchaseOrder;
import com.procurea.procurementsystem.entity.Vendor;
import com.procurea.procurementsystem.repository.PaymentRepository;
import com.procurea.procurementsystem.repository.PurchaseOrderRepository;
import com.procurea.procurementsystem.repository.VendorRepository;
import com.procurea.procurementsystem.util.ReportExporter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/reports")
public class ReportsController {

    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private PurchaseOrderRepository poRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @GetMapping("/vendors/csv")
    @PreAuthorize("hasRole('PROCUREMENT_MANAGER') or hasRole('ADMIN')")
    public void exportVendorsCSV(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=vendors_report.csv");
        List<Vendor> vendors = vendorRepository.findAll();
        ReportExporter.exportVendorsToCSV(vendors, response.getWriter());
    }

    @GetMapping("/vendors/pdf")
    @PreAuthorize("hasRole('PROCUREMENT_MANAGER') or hasRole('ADMIN')")
    public void exportVendorsPDF(HttpServletResponse response) throws IOException {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=vendors_report.pdf");
        List<Vendor> vendors = vendorRepository.findAll();
        ReportExporter.exportVendorsToPDF(vendors, response.getOutputStream());
    }

    @GetMapping("/purchase-orders/csv")
    @PreAuthorize("hasRole('PROCUREMENT_MANAGER') or hasRole('ADMIN')")
    public void exportPOsCSV(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=purchase_orders_report.csv");
        List<PurchaseOrder> pos = poRepository.findAll();
        ReportExporter.exportPOsToCSV(pos, response.getWriter());
    }

    @GetMapping("/purchase-orders/pdf")
    @PreAuthorize("hasRole('PROCUREMENT_MANAGER') or hasRole('ADMIN')")
    public void exportPOsPDF(HttpServletResponse response) throws IOException {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=purchase_orders_report.pdf");
        List<PurchaseOrder> pos = poRepository.findAll();
        ReportExporter.exportPOsToPDF(pos, response.getOutputStream());
    }

    @GetMapping("/payments/csv")
    @PreAuthorize("hasRole('PROCUREMENT_MANAGER') or hasRole('ADMIN')")
    public void exportPaymentsCSV(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=payments_report.csv");
        List<Payment> payments = paymentRepository.findAll();
        ReportExporter.exportPaymentsToCSV(payments, response.getWriter());
    }

    @GetMapping("/payments/pdf")
    @PreAuthorize("hasRole('PROCUREMENT_MANAGER') or hasRole('ADMIN')")
    public void exportPaymentsPDF(HttpServletResponse response) throws IOException {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=payments_report.pdf");
        List<Payment> payments = paymentRepository.findAll();
        ReportExporter.exportPaymentsToPDF(payments, response.getOutputStream());
    }
}
