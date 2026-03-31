# InfraWatch — Java Infrastructure Monitoring & Reporting Platform

[![Java](https://img.shields.io/badge/Java-17-orange)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-green)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue)](LICENSE)

**InfraWatch** is an enterprise-grade infrastructure monitoring and reporting platform built with Java 17 / Spring Boot 3.2. It provides real-time visibility into server health, virtualization resources, backup/DR status, and data migration progress — with automated PDF/Excel report generation and a modern dark-themed web dashboard.

Designed for **System Administrators** and **Infrastructure Engineers** managing hybrid on-premises and cloud environments.

---

## Quick Start

```bash
# Clone the repository
git clone https://github.com/harveyxiacn/InfraWatch.git
cd InfraWatch

# Run with Maven (dev mode — includes demo data)
# Windows PowerShell:
./mvnw spring-boot:run "-Dspring-boot.run.profiles=dev"
# macOS/Linux:
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Or run with Docker Compose (production mode)
docker-compose up -d

# Access the dashboard
open http://localhost:8080

# API documentation (Swagger UI)
open http://localhost:8080/swagger-ui/index.html
```

### Default Credentials

| Username | Password | Role | Access |
|----------|----------|------|--------|
| `admin` | `admin123` | ADMIN | Full access |
| `operator` | `operator123` | OPERATOR | Create & edit, no delete |
| `viewer` | `viewer123` | VIEWER | View only |

### Demo Data

In dev mode, the app auto-seeds **1,900+ realistic records** on startup:
- 20 servers across prod/uat/dev/dr environments
- 1,500+ health metrics (7 days, with business-hour CPU spikes)
- 4 hypervisors (VMware/KVM/Hyper-V) with 20 VMs
- 8 backup jobs with 240 executions (30 days, ~95% success rate)
- 5 DR plans with drill logs (3 pass, 1 fail)
- 2 migration projects with task tracking and validations
- 8 test cases with 40 execution records
- 6 report templates (PDF + Excel generation working)

---

## Key Features

### Server & System Monitoring
- Server inventory CRUD with search, filter by status/environment
- Real-time health metrics (CPU, memory, disk, network) with 7-day Chart.js graphs
- Installation/change tracking with audit trail and change ticket references
- Server create/edit/delete from web UI

### Virtualization Management
- Hypervisor inventory (VMware ESXi, KVM, Hyper-V)
- VM lifecycle management with host-to-VM mapping
- Snapshot tracking with age alerts
- Resource allocation visibility

### Backup & Disaster Recovery
- Backup job management with cron scheduling
- Execution history per job with success/failure/partial tracking
- 30-day backup trend analysis with stacked bar chart
- DR plan documentation with RTO/RPO targets
- DR drill logs with pass/fail results and issue tracking

### Data Migration Tracker
- Migration project management with progress bars
- Task-level tracking (expected vs actual row counts)
- Data validation: row count comparison + checksum verification
- Multi-status workflow: PLANNING → IN_PROGRESS → COMPLETED

### Report Engine
- **Real PDF generation** using OpenPDF with styled tables and headers
- **Real Excel generation** using Apache POI with formatted worksheets
- 6 report templates: Server Health, VM Capacity, Backup Compliance, Migration Progress, DR Readiness, System Inventory
- Report archive with download functionality
- Quartz Scheduler integration for scheduled reports

### System Testing
- Test case management (connectivity, performance, security, backup/restore)
- One-click test execution with simulated results
- Execution history with pass/fail badges and duration metrics

### Dashboard
- Summary cards: servers, VMs, backup jobs, active migrations
- Server status distribution (doughnut chart)
- System health metrics: test pass rate, backup success rate
- Quick navigation to all modules

### Security
- Spring Security with dual filter chains (web + API)
- Form login for web UI, JWT Bearer for REST API
- Role-based access: ADMIN, OPERATOR, VIEWER with `@PreAuthorize`
- BCrypt password hashing
- Audit log for all write operations
- API returns 401 (not redirect) for unauthenticated requests

### UI/UX
- Dark-themed command center UI (UI UX Pro Max design system)
- 4 selectable themes: IoT Dashboard, Mission Control, Cyberpunk, Arctic Light
- Theme switcher with localStorage persistence
- Fira Code + Fira Sans typography (data-optimized)
- Chart.js visualizations with dark-mode defaults
- Responsive design, error pages (403, 404, 500)

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 17 (LTS) |
| Framework | Spring Boot 3.2.5, Spring Data JPA, Spring Security 6 |
| Database | PostgreSQL 15 (prod) / H2 (dev) |
| PDF Reports | OpenPDF (iText fork via JasperReports) |
| Excel Reports | Apache POI 5.2.5 |
| Scheduler | Quartz Scheduler |
| Frontend | Thymeleaf + HTMX + Chart.js 4 + Bootstrap 5.3 |
| Build | Maven 3.9+ (wrapper included) |
| Testing | JUnit 5 + Mockito + Testcontainers |
| API Docs | SpringDoc OpenAPI 2.3 (Swagger UI) |
| Container | Docker + Docker Compose |

---

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│              Web Dashboard (Thymeleaf + HTMX + Chart.js)     │
├─────────────────────────────────────────────────────────────┤
│              REST API Layer (Spring MVC + Swagger)            │
├──────────┬──────────┬──────────┬──────────┬─────────────────┤
│  Server  │  Virtual │   DR &   │Migration │  Report Engine  │
│  Monitor │  ization │  Backup  │ Tracker  │  (PDF + Excel)  │
├──────────┴──────────┴──────────┴──────────┴─────────────────┤
│              Service Layer (Business Logic + Audit)           │
├─────────────────────────────────────────────────────────────┤
│              Data Access (Spring Data JPA + Repositories)    │
├─────────────────────────────────────────────────────────────┤
│              PostgreSQL 15 / H2 (dev)                        │
└─────────────────────────────────────────────────────────────┘
```

---

## Project Structure

```
InfraWatch/
├── src/main/java/com/infrawatch/
│   ├── InfraWatchApplication.java          # Main entry point
│   ├── config/                             # Security, JWT, Audit, Web config
│   │   ├── SecurityConfig.java             # Dual filter chain (web + API)
│   │   ├── JwtAuthenticationFilter.java    # JWT Bearer token filter
│   │   ├── AuditConfig.java                # JPA auditing
│   │   └── DemoDataInitializer.java        # Realistic demo data seeder
│   ├── controller/
│   │   ├── api/                            # REST API controllers (JSON)
│   │   └── web/                            # Thymeleaf page controllers
│   ├── service/                            # Business logic layer
│   ├── repository/                         # Spring Data JPA repositories
│   ├── model/                              # 18 JPA entities across 6 modules
│   ├── dto/                                # Request/Response DTOs
│   └── exception/                          # Global error handling
├── src/main/resources/
│   ├── application.yml                     # Base config
│   ├── application-dev.yml                 # Dev profile (H2, auto-DDL)
│   ├── application-prod.yml                # Prod profile (PostgreSQL)
│   ├── static/css/                         # Design system + themes
│   ├── static/js/                          # Theme switcher
│   └── templates/                          # 20+ Thymeleaf templates
├── docs/                                   # PRD, Tech Design, API Spec, DB Schema
├── Dockerfile                              # Multi-stage build
├── docker-compose.yml                      # App + PostgreSQL
└── CLAUDE.md                               # Build instructions
```

---

## API Endpoints

All REST endpoints are documented via Swagger UI at `/swagger-ui/index.html`.

| Module | Base URL | Endpoints |
|--------|----------|-----------|
| Auth | `/api/auth` | POST `/login` |
| Servers | `/api/servers` | GET, POST, PUT, DELETE + metrics |
| Virtualization | `/api/virtualization` | Hypervisors + VMs CRUD |
| Backup | `/api/backup` | Jobs CRUD + execution history |
| DR | `/api/dr` | Plans CRUD + drill logs |
| Migrations | `/api/migrations` | Projects + tasks + validations |
| Reports | `/api/reports` | Templates + generate + archives |
| Testing | `/api/tests` | Cases + execution |
| Dashboard | `/api/dashboard` | Summary aggregation |

### API Authentication

```bash
# Get JWT token
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# Use token
curl -H "Authorization: Bearer <token>" http://localhost:8080/api/servers
```

---

## Themes

InfraWatch includes 4 selectable themes (click the theme icon in the top bar):

| Theme | Typography | Background | Style |
|-------|-----------|------------|-------|
| **IoT Dashboard** (default) | Fira Code + Fira Sans | Dark navy | UI UX Pro Max recommended |
| **Mission Control** | JetBrains Mono + DM Sans | Deep slate | Electric blue accents |
| **Cyberpunk** | Space Mono + Rajdhani | True black OLED | Neon green + glow effects |
| **Arctic Light** | IBM Plex Mono + Plus Jakarta Sans | Light grey | Clean enterprise |

---

## Documentation

| Document | Description |
|----------|-------------|
| [PRD](docs/PRD.md) | Product Requirements Document |
| [Technical Design](docs/TECHNICAL_DESIGN.md) | Architecture & design patterns |
| [API Specification](docs/API_SPEC.md) | REST API endpoint definitions |
| [Database Schema](docs/DATABASE_SCHEMA.md) | All table definitions & relationships |
| [Test Plan](docs/TEST_PLAN.md) | Test strategy & case listings |

---

## License

This project is licensed under the Apache License 2.0 — see [LICENSE](LICENSE) for details.

---

## Author

**Harvey Xia** — Infrastructure & Software Engineer
[GitHub](https://github.com/harveyxiacn) | [LinkedIn](https://linkedin.com/in/harveyxiacn)
