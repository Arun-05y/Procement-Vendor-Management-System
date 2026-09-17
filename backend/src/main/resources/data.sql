-- ==========================================================
-- ProcurEA Initial Data Seed (MySQL 8.0)
-- ==========================================================

USE procurea;

-- 1. Insert Default Roles
INSERT IGNORE INTO roles (id, name) VALUES 
(1, 'ROLE_ADMIN'),
(2, 'ROLE_PROCUREMENT_MANAGER'),
(3, 'ROLE_PROCUREMENT_EXECUTIVE'),
(4, 'ROLE_FINANCE'),
(5, 'ROLE_EMPLOYEE'),
(6, 'ROLE_VENDOR');

-- 2. Insert Default Admin User (Password: password -> BCrypt hash)
INSERT IGNORE INTO users (id, username, email, password) VALUES 
(1, 'admin', 'admin@procurea.com', '$2a$10$eACCYoNOHEqgkZ9Z9FjU/eY8r8aTq8uGvS1x3T0Fm8T.9Qx9kXkye'),
(2, 'officer', 'officer@procurea.com', '$2a$10$eACCYoNOHEqgkZ9Z9FjU/eY8r8aTq8uGvS1x3T0Fm8T.9Qx9kXkye'),
(3, 'vendor1', 'vendor@globalsupplies.com', '$2a$10$eACCYoNOHEqgkZ9Z9FjU/eY8r8aTq8uGvS1x3T0Fm8T.9Qx9kXkye');

-- 3. Assign Roles
INSERT IGNORE INTO user_roles (user_id, role_id) VALUES 
(1, 1), -- admin -> ROLE_ADMIN
(2, 2), -- officer -> ROLE_PROCUREMENT_MANAGER
(3, 6); -- vendor1 -> ROLE_VENDOR

-- 4. Sample Vendors
INSERT IGNORE INTO vendors (id, user_id, vendor_code, company_name, contact_person, email, phone_number, address, city, state, country, category, status, rating, performance_category) VALUES
(1, 3, 'VND-001', 'Global Supplies Inc', 'John Doe', 'vendor@globalsupplies.com', '+1-555-0199', '100 Industrial Parkway', 'Chicago', 'IL', 'USA', 'IT Hardware', 'ACTIVE', 4.8, 'EXCELLENT'),
(2, NULL, 'VND-002', 'Apex Technologies', 'Sarah Jenkins', 'contact@apextech.com', '+1-555-0244', '450 Innovation Way', 'Austin', 'TX', 'USA', 'Electronics', 'ACTIVE', 4.6, 'EXCELLENT'),
(3, NULL, 'VND-003', 'Metro Office Solutions', 'Robert Vance', 'sales@metrooffice.com', '+1-555-0311', '800 Commerce Blvd', 'Scranton', 'PA', 'USA', 'Office Furniture', 'ACTIVE', 4.2, 'GOOD');

-- 5. Sample Products
INSERT IGNORE INTO products (id, product_code, name, description, category, unit_of_measure, unit_price, current_stock, reorder_level, vendor_id) VALUES
(1, 'PRD-LT01', 'Dell Latitude 5440 Laptop', 'Intel i7 13th Gen, 16GB RAM, 512GB SSD', 'IT Hardware', 'PCS', 950.00, 45, 10, 1),
(2, 'PRD-MN02', 'Dell 27-inch 4K Monitor', 'UltraSharp USB-C Hub Monitor', 'IT Hardware', 'PCS', 380.00, 60, 15, 1),
(3, 'PRD-CH03', 'Ergonomic Mesh Office Chair', 'Adjustable lumbar support and 3D armrests', 'Office Furniture', 'PCS', 220.00, 30, 8, 3),
(4, 'PRD-DK04', 'Motorized Standing Desk', 'Dual-motor 60x30 inch height adjustable desk', 'Office Furniture', 'PCS', 450.00, 20, 5, 3);
