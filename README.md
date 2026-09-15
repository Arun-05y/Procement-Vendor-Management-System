# ProcurEA - Enterprise Procurement & Vendor Management System

A full-stack, enterprise-grade **Procurement & Vendor Management System** built with **Spring Boot 3, Java 17, and MySQL 8.0** on the backend and **React, Vite, and Lucide Icons** on the frontend. The application delivers end-to-end procurement lifecycle automation—from purchase requisitions, RFQs, vendor quotation comparisons, AI-driven bid recommendations, and purchase orders to delivery tracking, invoicing, payments, and PDF reporting.

---

## 📌 Features

* **Vendor Management**: Onboarding, profile management, status verification, document uploads, and evaluation scorecards.
* **Purchase Requisitions**: Multi-item purchase requests with multi-tier approval workflows.
* **RFQ & Quotation Bidding**: Automated RFQ generation, vendor invitations, bid submission, and side-by-side comparison matrices.
* **AI-Assisted Decision Making**: Smart weighted algorithmic vendor recommendations based on price, delivery lead time, and vendor ratings.
* **Purchase Orders & Deliveries**: Automated PO issuance, shipment dispatch, tracking updates, and delivery confirmations.
* **Invoicing & Payments**: Three-way matching verification, invoice settlement, and payment transaction logging.
* **Analytics & PDF Reporting**: Real-time spending dashboards, cost trends, savings analytics, and downloadable OpenPDF reports.
* **Security & Auditing**: Role-Based Access Control (Admin, Procurement Officer, Vendor, Employee), JWT authentication, and comprehensive audit trails.
* **Resilient Frontend**: React/Vite UI with centralized Axios interceptors and offline failover simulation modes.

---

## 🛠️ Tech Stack

### Backend
* **Language & Framework**: Java 17, Spring Boot 3.2.3
* **Security**: Spring Security 6, JWT (JSON Web Tokens)
* **Data Access**: Spring Data JPA, Hibernate, MySQL Connector/J
* **Documentation & Reporting**: SpringDoc OpenAPI / Swagger 3, OpenPDF (LibrePDF)
* **Build Tool**: Maven

### Frontend
* **Core**: React 18, Vite
* **UI Components & Icons**: Modern Glassmorphism CSS, Lucide React
* **Routing & HTTP**: React Router DOM, Axios with JWT interceptor

### Infrastructure & Database
* **Database**: MySQL 8.0
* **Containerization**: Docker, Docker Compose

---

## 📂 Project Structure

```text
Procement-Vendor-Management-System/
├── backend/
│   ├── src/main/java/com/procurea/procurementsystem/
│   │   ├── controller/      # REST Endpoints (14 Controllers)
│   │   ├── dto/             # Data Transfer Objects
│   │   ├── entity/          # JPA Domain Entities
│   │   ├── exception/       # Global Exception Handlers
│   │   ├── repository/      # Spring Data JPA Repositories
│   │   ├── security/        # JWT & Web Security Config
│   │   ├── service/         # Service Interfaces & Implementations
│   │   └── util/            # PDF Exporter & Helpers
│   ├── pom.xml              # Maven Dependencies
│   └── Dockerfile
├── frontend/
│   ├── src/
│   │   ├── components/      # Navbar, Sidebar, Modals
│   │   ├── context/         # AuthContext
│   │   ├── pages/           # Dashboard, RFQ, PO, Deliveries, Analytics, etc.
│   │   └── services/        # Axios API Client with JWT Interceptors
│   ├── package.json
│   └── vite.config.js
├── API_DOCUMENTATION.md     # Detailed REST API Reference
├── docker-compose.yml       # Multi-container orchestration (MySQL + Backend)
└── README.md
```

---

## 🚀 Getting Started

### Prerequisites
* Java 17+ (or Docker Desktop)
* Node.js 18+ and npm
* MySQL 8.0 (if running locally without Docker)

### Option 1: Quickstart with Docker Compose

```bash
docker-compose up --build
```
* Backend starts at `http://localhost:8080/api`
* MySQL database starts at port `3306`

### Option 2: Running Locally

#### 1. Start the Backend
```bash
cd backend
mvn spring-boot:run
```
Configure your database credentials in `backend/src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/procurea?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=password
```

#### 2. Start the Frontend
```bash
cd frontend
npm install
npm run dev
```
Access the application at [http://localhost:3000](http://localhost:3000).

---

## 📖 API Documentation

A comprehensive guide to all REST API endpoints (request/response schemas, parameters, and access permissions) is available in [API_DOCUMENTATION.md](API_DOCUMENTATION.md).

---

## 👨‍💻 Author

**Arun Y**  
* GitHub: [https://github.com/Arun-05y](https://github.com/Arun-05y)

---

## 📄 License

This project is developed for educational and enterprise learning purposes under the MIT License.
