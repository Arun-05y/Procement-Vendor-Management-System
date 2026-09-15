# ProcurEA - Enterprise Procurement System API Documentation

Comprehensive REST API reference for ProcurEA Procurement & Vendor Management System.

---

## Base URL
- **Local Development**: `http://localhost:8080/api`
- **Docker Compose**: `http://localhost:8080/api`

---

## Authentication Header
All secured endpoints require a standard Bearer JWT token in the `Authorization` header:
```http
Authorization: Bearer <your_jwt_token>
```

---

## 1. Authentication Endpoints (`/api/auth`)

| Method | Endpoint | Description | Access |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/auth/signin` | Authenticates user & returns JWT token | Public |
| `POST` | `/api/auth/signup` | Registers new user account (Admin, Procurement Officer, Vendor) | Public |

### Request Body (Sign In)
```json
{
  "username": "admin",
  "password": "password"
}
```

### Response (200 OK)
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "id": 1,
  "username": "admin",
  "email": "admin@procurea.com",
  "roles": ["ROLE_ADMIN"]
}
```

---

## 2. Vendor Management (`/api/vendors`)

| Method | Endpoint | Description | Access |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/vendors` | List all registered vendors | Admin / Officer |
| `GET` | `/api/vendors/{id}` | Get vendor details by ID | Authenticated |
| `POST` | `/api/vendors` | Register a new vendor profile | Authenticated |
| `PUT` | `/api/vendors/{id}` | Update vendor profile information | Admin / Vendor |
| `PUT` | `/api/vendors/{id}/status` | Update vendor status (`PENDING`, `APPROVED`, `REJECTED`, `SUSPENDED`) | Admin |
| `POST` | `/api/vendors/{id}/documents` | Upload vendor verification compliance document | Authenticated |

---

## 3. Purchase Requests (`/api/purchase-requests`)

| Method | Endpoint | Description | Access |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/purchase-requests` | List all purchase/procurement requests | Authenticated |
| `GET` | `/api/purchase-requests/{id}` | Get purchase request by ID | Authenticated |
| `POST` | `/api/purchase-requests` | Submit a new purchase requisition with line items | Employee / Officer |
| `PUT` | `/api/purchase-requests/{id}/approve` | Approve a requisition | Admin / Manager |
| `PUT` | `/api/purchase-requests/{id}/reject` | Reject a requisition with remarks | Admin / Manager |

---

## 4. Request for Quotation - RFQ (`/api/rfqs`)

| Method | Endpoint | Description | Access |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/rfqs` | List all published RFQs | Authenticated |
| `GET` | `/api/rfqs/{id}` | Get RFQ details and invited vendors | Authenticated |
| `POST` | `/api/rfqs` | Create and publish RFQ from approved request | Procurement Officer |
| `PUT` | `/api/rfqs/{id}/close` | Close RFQ bidding window | Procurement Officer |

---

## 5. Quotations & Bidding (`/api/quotations`)

| Method | Endpoint | Description | Access |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/quotations/rfq/{rfqId}` | Get all vendor bids submitted for an RFQ | Officer / Admin |
| `POST` | `/api/quotations` | Submit vendor quotation for an RFQ | Vendor |
| `PUT` | `/api/quotations/{id}/select` | Select winning quotation and mark accepted | Procurement Officer |

---

## 6. Quotation Comparison & Analysis (`/api/quotations/compare`)

| Method | Endpoint | Description | Access |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/quotations/compare/{rfqId}` | Side-by-side pricing and terms comparison | Officer / Admin |

---

## 7. Purchase Orders (`/api/purchase-orders`)

| Method | Endpoint | Description | Access |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/purchase-orders` | List all purchase orders | Authenticated |
| `GET` | `/api/purchase-orders/{id}` | Get purchase order details | Authenticated |
| `POST` | `/api/purchase-orders/generate` | Generate official PO from accepted quotation | Procurement Officer |
| `PUT` | `/api/purchase-orders/{id}/status` | Update PO status (`ISSUED`, `ACKNOWLEDGED`, `SHIPPED`, `DELIVERED`, `CANCELLED`) | Authenticated |

---

## 8. Delivery & Shipment Tracking (`/api/deliveries`)

| Method | Endpoint | Description | Access |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/deliveries` | List active deliveries and tracking logs | Authenticated |
| `GET` | `/api/deliveries/{id}` | Get delivery record by ID | Authenticated |
| `GET` | `/api/deliveries/po/{poId}` | Get delivery info for a specific PO | Authenticated |
| `POST` | `/api/deliveries` | Dispatch and initialize shipment tracking | Vendor / Officer |
| `PUT` | `/api/deliveries/{id}/status` | Update delivery tracking status (`IN_TRANSIT`, `DELIVERED`) | Authenticated |

---

## 9. Invoices & Billing (`/api/invoices`)

| Method | Endpoint | Description | Access |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/invoices` | List all invoices | Authenticated |
| `GET` | `/api/invoices/{id}` | Get invoice details | Authenticated |
| `POST` | `/api/invoices` | Submit invoice for delivered PO | Vendor |
| `PUT` | `/api/invoices/{id}/status` | Update invoice verification status (`PENDING`, `VERIFIED`, `PAID`) | Finance / Officer |

---

## 10. Payments (`/api/payments`)

| Method | Endpoint | Description | Access |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/payments` | List all payment transactions | Finance / Admin |
| `GET` | `/api/payments/{id}` | Get payment receipt by ID | Authenticated |
| `POST` | `/api/payments` | Record disbursement payment against invoice | Finance / Admin |

---

## 11. Vendor Evaluation & Performance (`/api/vendor-evaluations`)

| Method | Endpoint | Description | Access |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/vendor-evaluations` | List all vendor rating evaluations | Authenticated |
| `GET` | `/api/vendor-evaluations/vendor/{vendorId}` | Get scorecard evaluations for specific vendor | Authenticated |
| `POST` | `/api/vendor-evaluations` | Submit score rating (Quality, Delivery, Price, Communication) | Procurement Officer |

---

## 12. Analytics & Reporting (`/api/analytics`, `/api/reports`)

| Method | Endpoint | Description | Access |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/analytics/summary` | Aggregated dashboard KPIs (spend, savings, lead time, counts) | Admin / Officer |
| `GET` | `/api/analytics/cost-trends` | Monthly expenditure analytics | Admin |
| `GET` | `/api/reports/purchase-orders/pdf` | Export Purchase Orders report in PDF format | Admin / Officer |
| `GET` | `/api/reports/vendors/pdf` | Export Vendor directory scorecard in PDF format | Admin / Officer |

---

## 13. AI Assistant & Decision Support (`/api/ai`)

| Method | Endpoint | Description | Access |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/ai/recommend-vendor` | Smart weighted recommendation for best vendor quote | Officer / Admin |
| `POST` | `/api/ai/analyze-quotations` | Comparative bid analysis and savings breakdown | Officer / Admin |

---

## 14. Audit Logs & Notifications (`/api/audit-logs`, `/api/notifications`)

| Method | Endpoint | Description | Access |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/audit-logs` | Query system audit trail for compliance | Admin |
| `GET` | `/api/notifications` | Get user notifications and activity updates | Authenticated |
| `PUT` | `/api/notifications/{id}/read` | Mark notification as read | Authenticated |
