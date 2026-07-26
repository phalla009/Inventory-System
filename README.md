# 📦 Inventory Management System

A robust, full-featured Inventory Management System built with **Java Spring Boot**, designed to track stock movements, manage products, suppliers, and streamline retail/warehouse operations effectively.

---

## 🚀 Key Features

- **Product Management:** CRUD operations for products, categories, units of measurement, and dual pricing (Cost vs. Selling price).
- **Stock Tracking & Real-Time Updates:** Automated stock-in/stock-out dynamic updates with audit logging.
- **Low Stock Alerts:** Reorder point notifications when inventory levels drop below thresholds.
- **Stock Adjustment & Audit:** Track damaged, expired, or lost inventory with reason codes.
- **Supplier & Purchase Orders:** Supplier directory, PO generation, and Goods Received Notes (GRN).
- **Role-Based Access Control (RBAC):** Granular permissions for Admin, Warehouse Staff, and Cashiers powered by Spring Security.
- **Reporting & Analytics:** Export inventory valuation, daily transaction logs, and stock movement reports to Excel/PDF.

---

## 🛠 Tech Stack

- **Backend:** Java 17+ / Spring Boot 3.x (Spring Data JPA, Spring Security)
- **Frontend:** Thymeleaf / Bootstrap 5 / HTML5 & CSS3
- **Database:** PostgreSQL / MySQL
- **Build Tool:** Apache Maven
- **Reporting & Tools:** Apache POI (Excel Export), JasperReports (PDF Export), Lombok, MapStruct

---

## 📂 Project Structure

```text
inventory-system/
├── src/
│   ├── main/
│   │   ├── java/com/inventory/
│   │   │   ├── config/          # Security, Swagger, & App Configurations
│   │   │   ├── controller/      # REST API & Web Controllers
│   │   │   ├── dto/             # Data Transfer Objects (Requests/Responses)
│   │   │   ├── entity/          # JPA Entities / Database Models
│   │   │   ├── exception/       # Global Exception Handlers & Custom Errors
│   │   │   ├── repository/     # Spring Data JPA Repositories
│   │   │   ├── service/         # Business Logic & Service Interfaces
│   │   │   │   └── impl/        # Service Implementations
│   │   │   └── InventoryApp.java# Spring Boot Main Class
│   │   └── resources/
│   │       ├── static/          # CSS, JS, Images, & Vendor Libraries
│   │       ├── templates/       # Thymeleaf HTML Views
│   │       ├── db/migration/    # Flyway / Liquibase SQL Migration Scripts
│   │       └── application.yml  # Application Configurations
│   └── test/                    # Unit & Integration Tests (JUnit 5, Mockito)
├── docs/                        # ER Diagrams, DB Schemas & API Documentation
├── .gitignore
├── pom.xml                      # Maven Dependencies Configuration
└── README.md
