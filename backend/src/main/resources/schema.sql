-- ==========================================================
-- ProcurEA Database Schema (MySQL 8.0)
-- ==========================================================

CREATE DATABASE IF NOT EXISTS procurea;
USE procurea;

-- 1. Roles
CREATE TABLE IF NOT EXISTS roles (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(35) NOT NULL UNIQUE
);

-- 2. Users
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(120) NOT NULL
);

-- 3. User Roles Mapping
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id INT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- 4. Vendors
CREATE TABLE IF NOT EXISTS vendors (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNIQUE,
    vendor_code VARCHAR(30) UNIQUE,
    company_name VARCHAR(255) NOT NULL,
    contact_person VARCHAR(255),
    email VARCHAR(255) NOT NULL,
    phone_number VARCHAR(50),
    address TEXT,
    city VARCHAR(100),
    state VARCHAR(100),
    country VARCHAR(100),
    gst_number VARCHAR(20),
    pan_number VARCHAR(15),
    category VARCHAR(100),
    payment_terms VARCHAR(50) DEFAULT 'Net 30',
    bank_account_number VARCHAR(50),
    bank_name VARCHAR(100),
    bank_ifsc_code VARCHAR(30),
    contract_start_date DATE,
    contract_end_date DATE,
    status VARCHAR(25) DEFAULT 'ACTIVE',
    rating DOUBLE DEFAULT 4.5,
    on_time_delivery_rate DOUBLE DEFAULT 95.0,
    fulfillment_rate DOUBLE DEFAULT 98.0,
    quality_rating DOUBLE DEFAULT 4.5,
    response_time_hours DOUBLE DEFAULT 12.0,
    performance_score DOUBLE DEFAULT 92.0,
    completed_orders_count INT DEFAULT 0,
    delayed_orders_count INT DEFAULT 0,
    cancelled_orders_count INT DEFAULT 0,
    performance_category VARCHAR(20) DEFAULT 'EXCELLENT',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_vendor_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

-- 5. Vendor Compliance Documents
CREATE TABLE IF NOT EXISTS vendor_documents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vendor_id BIGINT NOT NULL,
    document_name VARCHAR(255) NOT NULL,
    document_type VARCHAR(50),
    file_path VARCHAR(500),
    upload_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_vendor_docs_vendor FOREIGN KEY (vendor_id) REFERENCES vendors(id) ON DELETE CASCADE
);

-- 6. Products Catalog
CREATE TABLE IF NOT EXISTS products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_code VARCHAR(50) UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(100),
    unit_of_measure VARCHAR(50) DEFAULT 'PCS',
    unit_price DOUBLE NOT NULL,
    current_stock INT DEFAULT 0,
    reorder_level INT DEFAULT 10,
    vendor_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_product_vendor FOREIGN KEY (vendor_id) REFERENCES vendors(id) ON DELETE SET NULL
);

-- 7. Purchase Requests (Requisitions)
CREATE TABLE IF NOT EXISTS purchase_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    request_number VARCHAR(50) UNIQUE,
    department VARCHAR(100) NOT NULL,
    requester_name VARCHAR(100) NOT NULL,
    requester_email VARCHAR(100),
    title VARCHAR(255) NOT NULL,
    justification TEXT,
    priority VARCHAR(20) DEFAULT 'MEDIUM',
    estimated_budget DOUBLE NOT NULL,
    status VARCHAR(30) DEFAULT 'PENDING_APPROVAL',
    approved_by VARCHAR(100),
    approval_date TIMESTAMP NULL,
    rejection_reason TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 8. Purchase Request Line Items
CREATE TABLE IF NOT EXISTS purchase_request_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    purchase_request_id BIGINT NOT NULL,
    item_name VARCHAR(255) NOT NULL,
    description TEXT,
    quantity INT NOT NULL,
    estimated_unit_price DOUBLE NOT NULL,
    total_price DOUBLE NOT NULL,
    CONSTRAINT fk_pr_items_pr FOREIGN KEY (purchase_request_id) REFERENCES purchase_requests(id) ON DELETE CASCADE
);

-- 9. RFQs (Request For Quotations)
CREATE TABLE IF NOT EXISTS rfqs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rfq_number VARCHAR(50) UNIQUE,
    request_id BIGINT,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    submission_deadline TIMESTAMP NOT NULL,
    status VARCHAR(30) DEFAULT 'OPEN',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_rfq_pr FOREIGN KEY (request_id) REFERENCES purchase_requests(id) ON DELETE SET NULL
);

-- 10. RFQ Invited Vendors Join Table
CREATE TABLE IF NOT EXISTS rfq_invited_vendors (
    rfq_id BIGINT NOT NULL,
    vendor_id BIGINT NOT NULL,
    PRIMARY KEY (rfq_id, vendor_id),
    CONSTRAINT fk_rfq_inv_rfq FOREIGN KEY (rfq_id) REFERENCES rfqs(id) ON DELETE CASCADE,
    CONSTRAINT fk_rfq_inv_vendor FOREIGN KEY (vendor_id) REFERENCES vendors(id) ON DELETE CASCADE
);

-- 11. Quotations (Bids)
CREATE TABLE IF NOT EXISTS quotations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rfq_id BIGINT NOT NULL,
    vendor_id BIGINT NOT NULL,
    quotation_number VARCHAR(50) UNIQUE,
    total_amount DOUBLE NOT NULL,
    delivery_lead_time_days INT DEFAULT 7,
    payment_terms VARCHAR(100),
    warranty_months INT DEFAULT 12,
    valid_until DATE,
    status VARCHAR(30) DEFAULT 'SUBMITTED',
    notes TEXT,
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_quotation_rfq FOREIGN KEY (rfq_id) REFERENCES rfqs(id) ON DELETE CASCADE,
    CONSTRAINT fk_quotation_vendor FOREIGN KEY (vendor_id) REFERENCES vendors(id) ON DELETE CASCADE
);

-- 12. Purchase Orders
CREATE TABLE IF NOT EXISTS purchase_orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    po_number VARCHAR(50) NOT NULL UNIQUE,
    quotation_id BIGINT UNIQUE,
    vendor_id BIGINT NOT NULL,
    issued_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expected_delivery_date DATE,
    total_amount DOUBLE NOT NULL,
    payment_terms VARCHAR(100),
    shipping_address TEXT,
    billing_address TEXT,
    status VARCHAR(30) DEFAULT 'ISSUED',
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_po_quotation FOREIGN KEY (quotation_id) REFERENCES quotations(id) ON DELETE SET NULL,
    CONSTRAINT fk_po_vendor FOREIGN KEY (vendor_id) REFERENCES vendors(id) ON DELETE CASCADE
);

-- 13. Purchase Order Items
CREATE TABLE IF NOT EXISTS purchase_order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    purchase_order_id BIGINT NOT NULL,
    item_name VARCHAR(255) NOT NULL,
    description TEXT,
    quantity INT NOT NULL,
    unit_price DOUBLE NOT NULL,
    total_price DOUBLE NOT NULL,
    CONSTRAINT fk_po_items_po FOREIGN KEY (purchase_order_id) REFERENCES purchase_orders(id) ON DELETE CASCADE
);

-- 14. Deliveries
CREATE TABLE IF NOT EXISTS deliveries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    purchase_order_id BIGINT NOT NULL,
    tracking_number VARCHAR(100) UNIQUE,
    carrier VARCHAR(100),
    estimated_arrival DATE,
    delivery_date TIMESTAMP NULL,
    status VARCHAR(30) DEFAULT 'PENDING',
    recipient_name VARCHAR(100),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_delivery_po FOREIGN KEY (purchase_order_id) REFERENCES purchase_orders(id) ON DELETE CASCADE
);

-- 15. Invoices
CREATE TABLE IF NOT EXISTS invoices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    invoice_number VARCHAR(50) UNIQUE NOT NULL,
    purchase_order_id BIGINT NOT NULL,
    vendor_id BIGINT NOT NULL,
    invoice_date DATE NOT NULL,
    due_date DATE NOT NULL,
    amount DOUBLE NOT NULL,
    tax_amount DOUBLE DEFAULT 0.0,
    total_amount DOUBLE NOT NULL,
    status VARCHAR(30) DEFAULT 'PENDING_VERIFICATION',
    payment_terms VARCHAR(50),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_invoice_po FOREIGN KEY (purchase_order_id) REFERENCES purchase_orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_invoice_vendor FOREIGN KEY (vendor_id) REFERENCES vendors(id) ON DELETE CASCADE
);

-- 16. Payments
CREATE TABLE IF NOT EXISTS payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    payment_reference VARCHAR(100) UNIQUE NOT NULL,
    invoice_id BIGINT NOT NULL,
    amount DOUBLE NOT NULL,
    payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    payment_method VARCHAR(50) DEFAULT 'BANK_TRANSFER',
    transaction_status VARCHAR(30) DEFAULT 'COMPLETED',
    notes TEXT,
    CONSTRAINT fk_payment_invoice FOREIGN KEY (invoice_id) REFERENCES invoices(id) ON DELETE CASCADE
);

-- 17. Vendor Evaluations
CREATE TABLE IF NOT EXISTS vendor_evaluations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vendor_id BIGINT NOT NULL,
    evaluator_name VARCHAR(100) NOT NULL,
    quality_score DOUBLE NOT NULL,
    delivery_score DOUBLE NOT NULL,
    pricing_score DOUBLE NOT NULL,
    service_score DOUBLE NOT NULL,
    overall_score DOUBLE NOT NULL,
    feedback TEXT,
    evaluation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_eval_vendor FOREIGN KEY (vendor_id) REFERENCES vendors(id) ON DELETE CASCADE
);

-- 18. Approvals Audit
CREATE TABLE IF NOT EXISTS approvals (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT NOT NULL,
    approver_username VARCHAR(100) NOT NULL,
    decision VARCHAR(30) NOT NULL,
    comments TEXT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 19. Audit Logs
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    action VARCHAR(100) NOT NULL,
    performed_by VARCHAR(100) NOT NULL,
    details TEXT,
    ip_address VARCHAR(50),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 20. Notifications
CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    recipient_username VARCHAR(100) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    read_status BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
