package com.procurea.procurementsystem;

import com.procurea.procurementsystem.entity.*;
import com.procurea.procurementsystem.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PurchaseRequestRepository prRepository;

    @Autowired
    private PurchaseOrderRepository poRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Autowired
    private VendorEvaluationRepository evaluationRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Override
    public void run(String... args) throws Exception {
        // 1. Seed Roles
        for (Role.ERole roleEnum : Role.ERole.values()) {
            if (roleRepository.findByName(roleEnum).isEmpty()) {
                Role role = new Role();
                role.setName(roleEnum);
                roleRepository.save(role);
            }
        }

        // 2. Seed Users
        Role adminRole = roleRepository.findByName(Role.ERole.ROLE_ADMIN).get();
        Role managerRole = roleRepository.findByName(Role.ERole.ROLE_PROCUREMENT_MANAGER).get();
        Role execRole = roleRepository.findByName(Role.ERole.ROLE_PROCUREMENT_EXECUTIVE).get();
        Role financeRole = roleRepository.findByName(Role.ERole.ROLE_FINANCE).get();
        Role employeeRole = roleRepository.findByName(Role.ERole.ROLE_EMPLOYEE).get();
        Role vendorRole = roleRepository.findByName(Role.ERole.ROLE_VENDOR).get();

        User adminUser = createUserIfNotFound("admin", "admin@procurea.com", "admin123", adminRole);
        User managerUser = createUserIfNotFound("manager", "manager@procurea.com", "manager123", managerRole);
        User execUser = createUserIfNotFound("executive", "executive@procurea.com", "executive123", execRole);
        User financeUser = createUserIfNotFound("finance", "finance@procurea.com", "finance123", financeRole);
        User employeeUser = createUserIfNotFound("employee", "employee@procurea.com", "employee123", employeeRole);

        User vendorUser1 = createUserIfNotFound("techvendor", "contact@techcorp.com", "vendor123", vendorRole);
        User vendorUser2 = createUserIfNotFound("globalvendor", "sales@globalsupplies.com", "vendor123", vendorRole);

        // 3. Seed Vendors
        Vendor v1 = createVendorIfNotFound(vendorUser1, "VND-1001", "TechCorp Solutions Pvt Ltd", "Alex Morgan", "contact@techcorp.com", "+91 9876543210", "Bengaluru Tech Park", "Electronics & IT", "29ABCDE1234F1Z5", "Net 30", 4.8, 97.5, 98.0, 95.0, "EXCELLENT", Vendor.VendorStatus.ACTIVE);
        Vendor v2 = createVendorIfNotFound(vendorUser2, "VND-1002", "Global Industrial Supplies", "Rajesh Sharma", "sales@globalsupplies.com", "+91 9823456789", "Mumbai Industrial Estate", "Raw Materials", "27ABCDE5678F2Z1", "Net 45", 4.2, 89.0, 92.0, 88.0, "GOOD", Vendor.VendorStatus.ACTIVE);
        Vendor v3 = createVendorIfNotFound(null, "VND-1003", "Apex Office Essentials", "Samantha Reed", "orders@apexoffice.com", "+91 9811223344", "Delhi Commercial Hub", "Office Supplies", "07ABCDE9012F3Z8", "Net 15", 4.6, 95.0, 96.0, 93.5, "EXCELLENT", Vendor.VendorStatus.ACTIVE);
        Vendor v4 = createVendorIfNotFound(null, "VND-1004", "Prime Logistics & Cargo", "Vikram Patel", "ops@primelogistics.com", "+91 9833445566", "Chennai Port Zone", "Logistics", "33ABCDE3456F4Z3", "Immediate", 3.7, 78.0, 82.0, 75.0, "AVERAGE", Vendor.VendorStatus.PENDING_APPROVAL);

        // 4. Seed Products (including Low-Stock products)
        Product p1 = createProductIfNotFound("PRD-001", "Dell Latitude 5530 Laptop", "Electronics", "15.6-inch Intel Core i7 16GB RAM 512GB SSD", 62000.0, 24, 10, "Units", v1);
        Product p2 = createProductIfNotFound("PRD-002", "Ergonomic Mesh Chair", "Furniture", "Adjustable lumbar support high-back desk chair", 8500.0, 6, 15, "Units", v3); // Low stock!
        Product p3 = createProductIfNotFound("PRD-003", "Cold Rolled Steel Coil (Grade A)", "Raw Materials", "High-durability manufacturing steel", 4800.0, 180, 50, "Tons", v2);
        Product p4 = createProductIfNotFound("PRD-004", "CAT6 Shielded Ethernet Cable 305m", "Networking", "1Gbps high-speed networking cable spool", 4500.0, 4, 12, "Boxes", v1); // Low stock!
        Product p5 = createProductIfNotFound("PRD-005", "A4 Premium Copier Paper (75 GSM)", "Office Supplies", "500 sheets ream multi-purpose paper", 280.0, 450, 100, "Reams", v3);

        // 5. Seed Purchase Requests
        if (prRepository.count() == 0) {
            PurchaseRequest pr1 = new PurchaseRequest();
            pr1.setRequestNumber("PR-2026-0001");
            pr1.setTitle("Workstations and IT Peripherals for Engineering");
            pr1.setDescription("Upgrade equipment for 5 new incoming software engineers.");
            pr1.setDepartment("Engineering");
            pr1.setRequestedBy(employeeUser);
            pr1.setEstimatedBudget(320000.0);
            pr1.setStatus(PurchaseRequest.RequestStatus.APPROVED);
            pr1.setApprovalRemarks("Approved as per annual IT expansion budget.");

            PurchaseRequestItem pri1 = new PurchaseRequestItem(null, pr1, p1, p1.getName(), 5, p1.getUnitPrice(), 5 * p1.getUnitPrice());
            pr1.getItems().add(pri1);
            pr1.setTotalAmount(pri1.getEstimatedTotal());
            prRepository.save(pr1);

            PurchaseRequest pr2 = new PurchaseRequest();
            pr2.setRequestNumber("PR-2026-0002");
            pr2.setTitle("Office Ergonomics & Replacement Chairs");
            pr2.setDescription("Replace damaged seating in 3rd floor marketing bay.");
            pr2.setDepartment("Marketing");
            pr2.setRequestedBy(employeeUser);
            pr2.setEstimatedBudget(60000.0);
            pr2.setStatus(PurchaseRequest.RequestStatus.PENDING);
            pr2.setApprovalRemarks(null);

            PurchaseRequestItem pri2 = new PurchaseRequestItem(null, pr2, p2, p2.getName(), 6, p2.getUnitPrice(), 6 * p2.getUnitPrice());
            pr2.getItems().add(pri2);
            pr2.setTotalAmount(pri2.getEstimatedTotal());
            prRepository.save(pr2);
        }

        // 6. Seed Purchase Orders & Invoices
        if (poRepository.count() == 0) {
            PurchaseOrder po1 = new PurchaseOrder();
            po1.setPoNumber("PO-2026-0001");
            po1.setVendor(v1);
            po1.setDeliveryAddress("ProcureAI Tower, Floor 4, Bengaluru");
            po1.setExpectedDeliveryDate(LocalDateTime.now().plusDays(5));
            po1.setStatus(PurchaseOrder.POStatus.ORDERED);
            po1.setPaymentStatus(PurchaseOrder.PaymentStatus.PENDING);
            po1.setTermsAndConditions("Payment Net 30 Days upon quality sign-off");

            PurchaseOrderItem poi1 = new PurchaseOrderItem(null, po1, p1, p1.getName(), 3, p1.getUnitPrice(), 3 * p1.getUnitPrice());
            po1.getItems().add(poi1);
            po1.calculateTotals();
            PurchaseOrder savedPo1 = poRepository.save(po1);

            // Invoice 1 (Pending)
            Invoice inv1 = new Invoice();
            inv1.setInvoiceNumber("INV-2026-0001");
            inv1.setPurchaseOrder(savedPo1);
            inv1.setVendor(v1);
            inv1.setInvoiceAmount(savedPo1.getGrandTotal());
            inv1.setPaidAmount(0.0);
            inv1.setPaymentDueDate(LocalDate.now().plusDays(25));
            inv1.setStatus(Invoice.PaymentStatus.PENDING);
            inv1.setPaymentMethod("BANK_TRANSFER");
            inv1.setNotes("Payment due on NET 30 schedule.");
            invoiceRepository.save(inv1);

            // Delivery for PO1 (In Transit)
            Delivery del1 = new Delivery();
            del1.setPurchaseOrder(savedPo1);
            del1.setTrackingNumber("TRK-BLR-8921");
            del1.setCarrier("BlueDart Express");
            del1.setStatus(Delivery.DeliveryStatus.IN_TRANSIT);
            del1.setExpectedDeliveryDate(LocalDateTime.now().plusDays(5));
            del1.setNotes("Dispatched from Bengaluru warehouse on route to campus.");
            deliveryRepository.save(del1);

            // PO 2 (Delivered & Paid)
            PurchaseOrder po2 = new PurchaseOrder();
            po2.setPoNumber("PO-2026-0002");
            po2.setVendor(v3);
            po2.setDeliveryAddress("ProcureAI Regional Hub, Mumbai");
            po2.setExpectedDeliveryDate(LocalDateTime.now().minusDays(3));
            po2.setStatus(PurchaseOrder.POStatus.DELIVERED);
            po2.setPaymentStatus(PurchaseOrder.PaymentStatus.PAID);
            po2.setTermsAndConditions("Standard Commercial Delivery Terms");

            PurchaseOrderItem poi2 = new PurchaseOrderItem(null, po2, p5, p5.getName(), 50, p5.getUnitPrice(), 50 * p5.getUnitPrice());
            po2.getItems().add(poi2);
            po2.calculateTotals();
            PurchaseOrder savedPo2 = poRepository.save(po2);

            // Invoice 2 (Paid)
            Invoice inv2 = new Invoice();
            inv2.setInvoiceNumber("INV-2026-0002");
            inv2.setPurchaseOrder(savedPo2);
            inv2.setVendor(v3);
            inv2.setInvoiceAmount(savedPo2.getGrandTotal());
            inv2.setPaidAmount(savedPo2.getGrandTotal());
            inv2.setPaymentDueDate(LocalDate.now().minusDays(2));
            inv2.setPaymentDate(LocalDateTime.now().minusDays(1));
            inv2.setStatus(Invoice.PaymentStatus.PAID);
            inv2.setPaymentMethod("BANK_TRANSFER");
            inv2.setNotes("Paid in full via NEFT.");
            invoiceRepository.save(inv2);

            // Delivery for PO2 (Delivered)
            Delivery del2 = new Delivery();
            del2.setPurchaseOrder(savedPo2);
            del2.setTrackingNumber("TRK-MUM-4412");
            del2.setCarrier("DTDC Air");
            del2.setStatus(Delivery.DeliveryStatus.DELIVERED);
            del2.setDeliveryDate(LocalDateTime.now().minusDays(2));
            del2.setQualityScore(4.9);
            del2.setRejectionRate(0.0);
            del2.setQualityComments("All 50 reams received in crisp, undamaged packaging.");
            deliveryRepository.save(del2);

            // Evaluation for PO2
            VendorEvaluation eval1 = new VendorEvaluation();
            eval1.setVendor(v3);
            eval1.setPurchaseOrder(savedPo2);
            eval1.setDeliveryRating(5.0);
            eval1.setQualityRating(4.9);
            eval1.setPriceCompetitiveness(4.8);
            eval1.setOverallRating(4.9);
            eval1.setComments("Prompt delivery ahead of schedule and flawless quality.");
            evaluationRepository.save(eval1);

            // Invoice 3 (Overdue sample)
            Invoice inv3 = new Invoice();
            inv3.setInvoiceNumber("INV-2026-0003");
            inv3.setPurchaseOrder(savedPo1);
            inv3.setVendor(v2);
            inv3.setInvoiceAmount(45000.0);
            inv3.setPaidAmount(0.0);
            inv3.setPaymentDueDate(LocalDate.now().minusDays(5)); // Past due!
            inv3.setStatus(Invoice.PaymentStatus.OVERDUE);
            inv3.setPaymentMethod("BANK_TRANSFER");
            inv3.setNotes("Payment delayed pending reconciliation.");
            invoiceRepository.save(inv3);
        }

        // 7. Seed Initial Audit Logs
        if (auditLogRepository.count() == 0) {
            AuditLog l1 = new AuditLog(null, "admin", "LOGIN", "SECURITY", "User", adminUser.getId(), "Administrator successfully logged into dashboard", LocalDateTime.now().minusHours(2), "127.0.0.1", null, null);
            AuditLog l2 = new AuditLog(null, "manager", "APPROVE", "REQUESTS", "PurchaseRequest", 1L, "Approved request PR-2026-0001 for IT upgrade", LocalDateTime.now().minusHours(1), "127.0.0.1", "PENDING", "APPROVED");
            AuditLog l3 = new AuditLog(null, "executive", "CREATE", "ORDERS", "PurchaseOrder", 1L, "Generated purchase order PO-2026-0001 for TechCorp", LocalDateTime.now().minusMinutes(30), "127.0.0.1", null, "PO-2026-0001");
            AuditLog l4 = new AuditLog(null, "finance", "PAYMENT", "INVOICES", "Invoice", 2L, "Recorded payment for Invoice INV-2026-0002", LocalDateTime.now().minusMinutes(10), "127.0.0.1", "PENDING", "PAID");
            auditLogRepository.saveAll(Arrays.asList(l1, l2, l3, l4));
        }

        // 8. Seed Initial Notifications
        if (notificationRepository.count() == 0) {
            Notification n1 = new Notification();
            n1.setUser(adminUser);
            n1.setMessage("Low Stock Alert: Product 'Ergonomic Mesh Chair' has only 6 units remaining (Min: 15).");
            n1.setType("LOW_STOCK");
            n1.setIsRead(false);

            Notification n2 = new Notification();
            n2.setUser(financeUser);
            n2.setMessage("Overdue Payment Alert: Invoice INV-2026-0003 is overdue by 5 days.");
            n2.setType("PAYMENT_OVERDUE");
            n2.setIsRead(false);

            notificationRepository.saveAll(Arrays.asList(n1, n2));
        }
    }

    private User createUserIfNotFound(String username, String email, String password, Role role) {
        return userRepository.findByUsername(username).orElseGet(() -> {
            User user = new User(username, email, encoder.encode(password));
            Set<Role> roles = new HashSet<>();
            roles.add(role);
            user.setRoles(roles);
            return userRepository.save(user);
        });
    }

    private Vendor createVendorIfNotFound(User user, String code, String company, String contact, String email, String phone, String address, String category, String gst, String terms, Double rating, Double onTime, Double fulfillment, Double score, String perfCat, Vendor.VendorStatus status) {
        return vendorRepository.findByVendorCode(code).orElseGet(() -> {
            Vendor v = new Vendor();
            v.setUser(user);
            v.setVendorCode(code);
            v.setCompanyName(company);
            v.setContactPerson(contact);
            v.setEmail(email);
            v.setPhoneNumber(phone);
            v.setAddress(address);
            v.setCategory(category);
            v.setGstNumber(gst);
            v.setPaymentTerms(terms);
            v.setRating(rating);
            v.setOnTimeDeliveryRate(onTime);
            v.setFulfillmentRate(fulfillment);
            v.setPerformanceScore(score);
            v.setPerformanceCategory(perfCat);
            v.setStatus(status);
            v.setContractStartDate(LocalDate.now().minusMonths(6));
            v.setContractEndDate(LocalDate.now().plusMonths(6));
            return vendorRepository.save(v);
        });
    }

    private Product createProductIfNotFound(String code, String name, String cat, String desc, Double price, Integer stock, Integer minStock, String unit, Vendor supplier) {
        return productRepository.findByProductCode(code).orElseGet(() -> {
            Product p = new Product();
            p.setProductCode(code);
            p.setName(name);
            p.setCategory(cat);
            p.setDescription(desc);
            p.setUnitPrice(price);
            p.setCurrentStock(stock);
            p.setMinimumStockLevel(minStock);
            p.setUnit(unit);
            p.setSupplier(supplier);
            p.updateStockStatus();
            return productRepository.save(p);
        });
    }
}
