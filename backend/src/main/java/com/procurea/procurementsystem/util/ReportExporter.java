package com.procurea.procurementsystem.util;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.procurea.procurementsystem.entity.Payment;
import com.procurea.procurementsystem.entity.PurchaseOrder;
import com.procurea.procurementsystem.entity.Vendor;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.List;

public class ReportExporter {

    // ─── CSV EXPORTERS ────────────────────────────────────────────────────────

    public static void exportVendorsToCSV(List<Vendor> vendors, Writer writer) throws IOException {
        PrintWriter pw = new PrintWriter(writer);
        pw.println("Vendor Code,Company Name,Contact Person,Email,Phone,Category,Status,Rating,Performance Score");
        for (Vendor v : vendors) {
            pw.printf("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%.1f,%.1f\n",
                    v.getVendorCode(),
                    v.getCompanyName().replace("\"", "\"\""),
                    v.getContactPerson() == null ? "" : v.getContactPerson().replace("\"", "\"\""),
                    v.getEmail(),
                    v.getPhoneNumber() == null ? "" : v.getPhoneNumber(),
                    v.getCategory() == null ? "" : v.getCategory(),
                    v.getStatus().name(),
                    v.getRating(),
                    v.getPerformanceScore()
            );
        }
        pw.flush();
    }

    public static void exportPOsToCSV(List<PurchaseOrder> pos, Writer writer) throws IOException {
        PrintWriter pw = new PrintWriter(writer);
        pw.println("PO Number,Vendor,Total Amount,Status,Issued Date,Expected Delivery");
        for (PurchaseOrder po : pos) {
            pw.printf("\"%s\",\"%s\",%.2f,\"%s\",\"%s\",\"%s\"\n",
                    po.getPoNumber(),
                    po.getQuotation().getVendor().getCompanyName().replace("\"", "\"\""),
                    po.getQuotation().getTotalAmount(),
                    po.getStatus().name(),
                    po.getIssuedDate(),
                    po.getExpectedDeliveryDate() == null ? "" : po.getExpectedDeliveryDate()
            );
        }
        pw.flush();
    }

    public static void exportPaymentsToCSV(List<Payment> payments, Writer writer) throws IOException {
        PrintWriter pw = new PrintWriter(writer);
        pw.println("Payment ID,PO Number,Vendor,Amount,Status,Method,Txn Ref,Date");
        for (Payment p : payments) {
            pw.printf("%d,\"%s\",\"%s\",%.2f,\"%s\",\"%s\",\"%s\",\"%s\"\n",
                    p.getId(),
                    p.getPurchaseOrder().getPoNumber(),
                    p.getPurchaseOrder().getQuotation().getVendor().getCompanyName().replace("\"", "\"\""),
                    p.getAmount(),
                    p.getStatus().name(),
                    p.getPaymentMethod(),
                    p.getTransactionReference() == null ? "" : p.getTransactionReference(),
                    p.getPaymentDate()
            );
        }
        pw.flush();
    }

    // ─── PDF EXPORTERS ────────────────────────────────────────────────────────

    public static void exportVendorsToPDF(List<Vendor> vendors, OutputStream out) {
        Document document = new Document(PageSize.A4);
        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.DARK_GRAY);
            Paragraph title = new Paragraph("ProcureAI — Vendor Directory Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1.5f, 2.5f, 2.0f, 1.5f, 1.0f, 1.5f});

            addCell(table, "Code", true);
            addCell(table, "Company", true);
            addCell(table, "Email", true);
            addCell(table, "Category", true);
            addCell(table, "Rating", true);
            addCell(table, "Status", true);

            for (Vendor v : vendors) {
                addCell(table, v.getVendorCode(), false);
                addCell(table, v.getCompanyName(), false);
                addCell(table, v.getEmail(), false);
                addCell(table, v.getCategory() == null ? "General" : v.getCategory(), false);
                addCell(table, String.format("%.1f ★", v.getRating()), false);
                addCell(table, v.getStatus().name(), false);
            }

            document.add(table);
        } catch (DocumentException e) {
            e.printStackTrace();
        } finally {
            document.close();
        }
    }

    public static void exportPOsToPDF(List<PurchaseOrder> pos, OutputStream out) {
        Document document = new Document(PageSize.A4);
        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.DARK_GRAY);
            Paragraph title = new Paragraph("ProcureAI — Purchase Orders Summary", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2.0f, 3.0f, 2.0f, 2.0f, 2.0f});

            addCell(table, "PO Number", true);
            addCell(table, "Vendor", true);
            addCell(table, "Amount", true);
            addCell(table, "Status", true);
            addCell(table, "Issued Date", true);

            for (PurchaseOrder po : pos) {
                addCell(table, po.getPoNumber(), false);
                addCell(table, po.getQuotation().getVendor().getCompanyName(), false);
                addCell(table, String.format("₹%,.2f", po.getQuotation().getTotalAmount()), false);
                addCell(table, po.getStatus().name(), false);
                addCell(table, po.getIssuedDate().toString().substring(0, 10), false);
            }

            document.add(table);
        } catch (DocumentException e) {
            e.printStackTrace();
        } finally {
            document.close();
        }
    }

    public static void exportPaymentsToPDF(List<Payment> payments, OutputStream out) {
        Document document = new Document(PageSize.A4);
        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.DARK_GRAY);
            Paragraph title = new Paragraph("ProcureAI — Payments Transaction Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1.0f, 2.0f, 2.5f, 2.0f, 1.5f, 2.0f});

            addCell(table, "ID", true);
            addCell(table, "PO Number", true);
            addCell(table, "Vendor", true);
            addCell(table, "Amount", true);
            addCell(table, "Status", true);
            addCell(table, "Txn Ref", true);

            for (Payment p : payments) {
                addCell(table, p.getId().toString(), false);
                addCell(table, p.getPurchaseOrder().getPoNumber(), false);
                addCell(table, p.getPurchaseOrder().getQuotation().getVendor().getCompanyName(), false);
                addCell(table, String.format("₹%,.2f", p.getAmount()), false);
                addCell(table, p.getStatus().name(), false);
                addCell(table, p.getTransactionReference() == null ? "" : p.getTransactionReference(), false);
            }

            document.add(table);
        } catch (DocumentException e) {
            e.printStackTrace();
        } finally {
            document.close();
        }
    }

    private static void addCell(PdfPTable table, String text, boolean isHeader) {
        Font font = isHeader ? FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE) : FontFactory.getFont(FontFactory.HELVETICA, 9, Color.BLACK);
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(6);
        if (isHeader) {
            cell.setBackgroundColor(new Color(99, 102, 241)); // Primary color #6366f1
        }
        table.addCell(cell);
    }
}
