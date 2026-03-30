# CLAUDE.md — Implementation Instructions for Claude Code

## Project Overview
Build **InfraWatch**, a Java 17 / Spring Boot 3.2 infrastructure monitoring and reporting platform. Read ALL docs in the `docs/` folder before starting implementation.

## Priority Order
Implement in this exact order. Each phase should compile and run before moving to next:

---

### Phase 1: Project Skeleton (do this FIRST)
1. Initialize Maven project with `pom.xml` — include ALL dependencies:
   - spring-boot-starter-web
   - spring-boot-starter-data-jpa
   - spring-boot-starter-security
   - spring-boot-starter-thymeleaf
   - spring-boot-starter-validation
   - springdoc-openapi-starter-webmvc-ui (2.3.0)
   - postgresql driver
   - h2 database (test/dev scope)
   - flyway-core
   - jasperreports (6.20.6)
   - poi-ooxml (5.2.5)
   - quartz-scheduler
   - lombok
   - mapstruct (if desired, or manual mapping)
   - jjwt (0.12.5) for JWT
   - junit-jupiter, mockito, spring-boot-starter-test, testcontainers (test scope)
   - jacoco-maven-plugin
   - htmx (webjars)
   - bootstrap (webjars)

2. Create `InfraWatchApplication.java` main class
3. Create `application.yml`, `application-dev.yml`, `application-prod.yml` as specified in TECHNICAL_DESIGN.md
4. Create `BaseEntity.java` with JPA auditing
5. Verify: `mvn spring-boot:run -Dspring-boot.run.profiles=dev` starts without errors

---

### Phase 2: Authentication & Security
1. Create `User` entity and `UserRepository`
2. Create `AuditLog` entity and `AuditLogRepository`
3. Create `SecurityConfig.java` — configure Spring Security with:
   - Form login for web UI (Thymeleaf)
   - JWT Bearer token for API endpoints
   - Role-based access: ADMIN, OPERATOR, VIEWER
   - Permit: `/login`, `/css/**`, `/js/**`, `/actuator/health`, `/swagger-ui/**`, `/v3/api-docs/**`
4. Create `JwtService.java` — generate/validate JWT tokens using jjwt
5. Create `AuthService.java` — authenticate, create default admin on first run
6. Create `AuthApiController.java` — POST `/api/auth/login`
7. Create `AuditService.java` — log write operations
8. Create `LoginController.java` + `templates/login.html`
9. Flyway migration `V1__create_auth_tables.sql`

---

### Phase 3: Server Module
1. Create entities: `Server`, `HealthMetric`, `Installation` with enums
2. Create repositories with custom query methods (see DATABASE_SCHEMA.md)
3. Create `ServerService`, `HealthMetricService`, `SystemSpecService`
4. Create `ServerApiController` — full CRUD + metrics + installations + spec PDF
5. Create `ServerWebController` + Thymeleaf templates:
   - `servers/list.html` — table with status badges, search, filter
   - `servers/detail.html` — server info + metrics chart (Chart.js) + installation history
   - `servers/form.html` — create/edit form
6. Flyway migration `V2__create_server_tables.sql`
7. JasperReports template: `reports/server_health.jrxml`
8. System spec PDF generation in `SystemSpecService`

---

### Phase 4: Virtualization Module
1. Create entities: `Hypervisor`, `VirtualMachine`, `Snapshot`
2. Create repositories
3. Create `VirtualizationService`, `CapacityPlanningService`
4. Create `VirtualizationApiController` — CRUD + capacity endpoint
5. Create `VmWebController` + Thymeleaf templates:
   - `virtualization/hosts.html` — hypervisor list with VM counts
   - `virtualization/vms.html` — VM table with host filter
   - `virtualization/capacity.html` — allocation ratios, charts
6. Flyway migration `V3__create_virtualization_tables.sql`
7. JasperReports template: `reports/vm_capacity.jrxml`

---

### Phase 5: Backup & DR Module
1. Create entities: `BackupJob`, `BackupExecution`, `DRPlan`, `DrillLog`
2. Create repositories
3. Create `BackupService`, `DRPlanService`, `BackupAnalyticsService`
4. Create `BackupApiController`, `DRApiController`
5. Create web controllers + templates:
   - `backup/jobs.html` — backup job list + execution history
   - `backup/trend.html` — success/failure trend chart
   - `dr/plans.html` — DR plan list
   - `dr/drills.html` — drill log history
   - `dr/compliance.html` — compliance dashboard
6. Flyway migrations `V4__create_backup_tables.sql`
7. DR Plan PDF export
8. JasperReports templates: `reports/backup_compliance.jrxml`, `reports/dr_readiness.jrxml`

---

### Phase 6: Data Migration Module
1. Create entities: `MigrationProject`, `MigrationTask`, `MigrationValidation`
2. Create repositories
3. Create `MigrationService`, `DataValidationService`
4. Create `MigrationApiController`
5. Create web controllers + templates:
   - `migrations/list.html` — project list with progress bars
   - `migrations/detail.html` — task list, validation results, progress
6. Flyway migration `V5__create_migration_tables.sql`
7. JasperReports template: `reports/migration_progress.jrxml`

---

### Phase 7: Report Engine
1. Create entities: `ReportTemplate`, `ReportSchedule`, `ReportArchive`
2. Create `JasperReportProcessor` — core PDF generation from .jrxml + JRDataSource
3. Create `ExcelReportProcessor` — Apache POI Excel generation
4. Create `ReportGenerationService` — orchestrates PDF/Excel generation per template
5. Create `ReportScheduleService` — manages Quartz jobs for scheduled reports
6. Create `ReportApiController`
7. Create web controllers + templates:
   - `reports/templates.html` — list available templates
   - `reports/generate.html` — form to generate on-demand report
   - `reports/archive.html` — list generated reports with download links
   - `reports/schedules.html` — manage scheduled reports
8. Flyway migration `V6__create_report_tables.sql`
9. Create ALL .jrxml templates with embedded Chart.js or JFreeChart for charts in PDFs
10. System Inventory report template: `reports/system_inventory.jrxml`

---

### Phase 8: Testing Module
1. Create entities: `TestCase`, `TestExecution`
2. Create `TestCaseService`, `HealthCheckService` (ping, TCP, disk checks)
3. Create `TestApiController`
4. Create web controllers + templates
5. Flyway migration `V7__create_testing_tables.sql`

---

### Phase 9: Dashboard
1. Create `DashboardService` — aggregates data from all modules
2. Create `DashboardApiController` — `/api/dashboard/summary`
3. Create `DashboardController` + `templates/dashboard.html`:
   - Summary cards (server count, VM count, backup rate, active migrations)
   - Server health heatmap or status distribution chart
   - Recent alerts section
   - Quick navigation links
4. This is the landing page after login — make it the default route `/`

---

### Phase 10: Demo Data & Polish
1. Flyway `V8__insert_default_data.sql` — default admin user + report templates
2. Flyway `V9__insert_demo_data.sql` — realistic demo data (see DATABASE_SCHEMA.md for counts)
3. Sidebar navigation fragment (`fragments/sidebar.html`)
4. Common layout template (`layout/main.html`)
5. Error pages (403, 404, 500)
6. Global exception handler `GlobalExceptionHandler.java`
7. Swagger/OpenAPI annotations on all API controllers

---

### Phase 11: Docker & Deployment
1. Create `Dockerfile` (multi-stage build as in TECHNICAL_DESIGN.md)
2. Create `docker-compose.yml`
3. Create `.github/workflows/ci.yml` for GitHub Actions
4. Verify: `docker-compose up -d` works end-to-end

---

### Phase 12: Tests
1. Unit tests for all service classes (see TEST_PLAN.md for test case list)
2. Integration tests for all API controllers using Testcontainers
3. Jacoco coverage report
4. Target: > 70% service layer coverage

---

## Key Implementation Notes

### JasperReports Integration
- Store .jrxml templates in `src/main/resources/reports/`
- Use `JasperCompileManager.compileReport()` → `JasperFillManager.fillReport()` → `JasperExportManager.exportReportToPdf()`
- For data, use `JRBeanCollectionDataSource` from entity lists
- Pass parameters as `Map<String, Object>` (date range, filters)

### Apache POI Excel Generation
```java
// Pattern for Excel report
XSSFWorkbook workbook = new XSSFWorkbook();
XSSFSheet sheet = workbook.createSheet("Server Health");
// Create header row with bold style
// Iterate data rows
// Auto-size columns
// Return as byte[]
```

### Thymeleaf + HTMX Pattern
- Use `th:fragment` for reusable components
- Use `hx-get`, `hx-post` for partial updates
- Use `hx-target` and `hx-swap` for in-place content replacement
- Chart.js charts: render in `<canvas>` elements, populate via inline `<script>` with Thymeleaf data

### Standard API Response
Create `ApiResponse<T>` wrapper:
```java
public class ApiResponse<T> {
    private boolean success;
    private T data;
    private String message;
    private ErrorDetail error;
}
```

### Pagination
Use Spring Data's `Pageable` and return `Page<T>` wrapped in `PageResponse`.

### Validation
Use Jakarta Bean Validation annotations (`@NotBlank`, `@Min`, `@Max`, `@Email`) on request DTOs.

---

## File Structure Reminder
Refer to TECHNICAL_DESIGN.md Section 2 for the complete package structure. Follow it precisely.

## API Contract
Refer to API_SPEC.md for all endpoint definitions, request/response formats.

## Database
Refer to DATABASE_SCHEMA.md for all table definitions. Create Flyway migrations matching these schemas.

## Testing
Refer to TEST_PLAN.md for complete test case listings.
