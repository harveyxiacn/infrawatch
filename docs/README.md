# InfraWatch — Java Infrastructure Monitoring & Reporting Platform

[![Java](https://img.shields.io/badge/Java-17-orange)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-green)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue)](LICENSE)

**InfraWatch** is an open-source, enterprise-grade infrastructure monitoring and reporting platform built with Java / Spring Boot. It provides real-time visibility into server health, virtualization resources, backup/DR status, and data migration progress — with automated PDF/Excel report generation and a modern web dashboard.

Designed for **System Administrators** and **Infrastructure Engineers** managing hybrid on-premises and cloud environments.

---

## Key Features

### 🖥️ Server & System Monitoring
- Real-time server health metrics (CPU, memory, disk, network)
- System installation tracking and configuration audit trail
- Server inventory management with hardware/software specifications
- Automated system specification document generation

### 🔄 Virtualization Management
- VM inventory and resource utilization monitoring (VMware/KVM/Hyper-V compatible via API abstraction)
- Host-to-VM mapping and resource allocation reports
- VM snapshot tracking and lifecycle management
- Virtualization capacity planning analytics

### 💾 Data Migration Tracker
- End-to-end data migration project management
- Source-to-target mapping with validation checkpoints
- Migration progress dashboard with real-time status
- Data integrity verification reports (row count, checksum)
- Rollback planning documentation

### 🛡️ Disaster Recovery & Backup
- Backup job scheduling and status monitoring
- DR plan documentation and version control
- RTO/RPO tracking per system/application
- DR drill execution log and compliance reports
- Backup success/failure trend analysis

### 📊 Report Engine
- Automated PDF report generation (JasperReports)
- Excel export with pivot-ready data (Apache POI)
- Scheduled report delivery via email (Quartz Scheduler)
- Custom report templates with drag-and-drop builder
- Historical report archive and comparison

### 🧪 System Testing
- Infrastructure test case management
- Automated server connectivity and health checks
- Test execution history and results dashboard
- Integration with CI/CD pipelines via REST API

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 17 |
| Framework | Spring Boot 3.2, Spring Data JPA, Spring Security |
| Database | PostgreSQL 15 (production) / H2 (dev/test) |
| Report Engine | JasperReports 6.x + Apache POI 5.x |
| Scheduler | Quartz Scheduler |
| Frontend | Thymeleaf + HTMX + Chart.js (server-side rendered) |
| Build | Maven |
| Testing | JUnit 5 + Mockito + Testcontainers |
| API Docs | SpringDoc OpenAPI (Swagger UI) |
| Containerization | Docker + Docker Compose |

---

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    Web Dashboard (Thymeleaf + HTMX)      │
├─────────────────────────────────────────────────────────┤
│                    REST API Layer (Spring MVC)            │
├──────────┬──────────┬──────────┬──────────┬─────────────┤
│  Server  │  Virtual │   DR &   │Migration │   Report    │
│  Monitor │  ization │  Backup  │ Tracker  │   Engine    │
│  Module  │  Module  │  Module  │  Module  │   Module    │
├──────────┴──────────┴──────────┴──────────┴─────────────┤
│              Service Layer (Business Logic)               │
├─────────────────────────────────────────────────────────┤
│              Data Access Layer (Spring Data JPA)          │
├─────────────────────────────────────────────────────────┤
│              PostgreSQL / H2 Database                     │
└─────────────────────────────────────────────────────────┘
```

---

## Quick Start

```bash
# Clone the repository
git clone https://github.com/harveyxiacn/infrawatch.git
cd infrawatch

# Run with Docker Compose (recommended)
docker-compose up -d

# Or run locally with Maven
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Access the dashboard
open http://localhost:8080

# API documentation
open http://localhost:8080/swagger-ui.html
```

### Default Credentials
- Username: `admin`
- Password: `admin123`

---

## Project Structure

```
infrawatch/
├── src/main/java/com/infrawatch/
│   ├── InfraWatchApplication.java
│   ├── config/                    # Security, Quartz, JasperReports config
│   ├── controller/                # REST + Thymeleaf controllers
│   ├── service/                   # Business logic
│   ├── repository/                # Spring Data JPA repositories
│   ├── model/                     # JPA entities
│   │   ├── server/                # Server, SystemSpec, Installation
│   │   ├── virtualization/        # VirtualMachine, Hypervisor, Snapshot
│   │   ├── backup/                # BackupJob, DRPlan, DrillLog
│   │   ├── migration/             # MigrationProject, MigrationTask, Validation
│   │   └── report/                # ReportTemplate, ReportSchedule, ReportArchive
│   ├── dto/                       # Request/Response DTOs
│   ├── scheduler/                 # Quartz jobs for scheduled reports & health checks
│   ├── report/                    # JasperReports template processors
│   └── exception/                 # Global exception handling
├── src/main/resources/
│   ├── application.yml
│   ├── application-dev.yml
│   ├── application-prod.yml
│   ├── db/migration/              # Flyway database migrations
│   ├── reports/                   # JasperReports .jrxml templates
│   ├── templates/                 # Thymeleaf HTML templates
│   └── static/                    # CSS, JS, Chart.js
├── src/test/java/                 # Unit & integration tests
├── docker-compose.yml
├── Dockerfile
└── docs/
    ├── PRD.md                     # Product Requirements Document
    ├── TECHNICAL_DESIGN.md        # Technical Design Document
    ├── API_SPEC.md                # REST API Specification
    ├── DATABASE_SCHEMA.md         # Database Schema Design
    ├── SYSTEM_SPEC_TEMPLATE.md    # System Specification Template (meta!)
    └── TEST_PLAN.md               # Test Plan & Strategy
```

---

## Modules Overview

### 1. Server Monitor (`/api/servers`)
Manage server inventory, track health metrics, generate system specification documents.

### 2. Virtualization (`/api/virtualization`)
Monitor VMs, hypervisors, resource allocation; capacity planning reports.

### 3. DR & Backup (`/api/dr`)
Track backup jobs, manage DR plans, log DR drills, RTO/RPO compliance.

### 4. Migration Tracker (`/api/migrations`)
Manage data migration projects, track tasks, validate data integrity.

### 5. Report Engine (`/api/reports`)
Generate, schedule, and archive PDF/Excel infrastructure reports.

---

## Screenshots

> Screenshots will be added after initial implementation.

---

## Contributing

Contributions are welcome! Please read [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

---

## License

This project is licensed under the Apache License 2.0 — see [LICENSE](LICENSE) for details.

---

## Author

**Harvey Xia** — Infrastructure & Software Engineer  
[GitHub](https://github.com/harveyxiacn) | [LinkedIn](https://linkedin.com/in/harveyxiacn)
