# InfraWatch — Technical Design Document

**Version:** 1.0  
**Author:** Harvey Xia  
**Date:** 2026-03-30

---

## 1. Technology Stack & Justification

| Component | Choice | Version | Rationale |
|-----------|--------|---------|-----------|
| Language | Java | 17 (LTS) | Enterprise standard, strong typing, mature ecosystem |
| Framework | Spring Boot | 3.2.x | Convention over configuration, production-ready features |
| ORM | Spring Data JPA + Hibernate | 6.x | Reduces boilerplate, powerful query methods |
| Database | PostgreSQL | 15 | Robust, supports JSONB for flexible fields, open-source |
| Dev Database | H2 | 2.x | In-memory for fast dev/test cycles |
| DB Migration | Flyway | 9.x | Version-controlled schema changes |
| Report PDF | JasperReports | 6.20 | Industry-standard Java PDF reporting |
| Report Excel | Apache POI | 5.2 | Native Excel generation without external dependencies |
| Scheduler | Quartz | 2.3 | Persistent job scheduling with cron support |
| Frontend | Thymeleaf + HTMX | 3.1 / 1.9 | Server-side rendering, minimal JS complexity |
| Charts | Chart.js | 4.x | Lightweight, responsive charts via CDN |
| Security | Spring Security | 6.x | Role-based access, JWT for API |
| API Docs | SpringDoc OpenAPI | 2.3 | Auto-generated Swagger UI |
| Testing | JUnit 5 + Mockito + Testcontainers | 5.10 / 5.x / 1.19 | Comprehensive test stack |
| Build | Maven | 3.9 | Standard Java build tool |
| Container | Docker + Docker Compose | 24 / 2.x | Consistent deployment |

---

## 2. Package Structure

```
com.infrawatch
├── InfraWatchApplication.java              # Main entry point
├── config/
│   ├── SecurityConfig.java                 # Spring Security configuration
│   ├── JwtConfig.java                      # JWT token provider & filter
│   ├── QuartzConfig.java                   # Quartz scheduler setup
│   ├── JasperConfig.java                   # JasperReports configuration
│   ├── WebConfig.java                      # CORS, interceptors
│   └── AuditConfig.java                    # JPA auditing (createdAt, updatedAt)
│
├── model/                                  # JPA Entities
│   ├── base/
│   │   └── BaseEntity.java                 # id, createdAt, updatedAt, createdBy
│   ├── server/
│   │   ├── Server.java
│   │   ├── HealthMetric.java
│   │   ├── Installation.java
│   │   └── enums/
│   │       ├── ServerStatus.java           # ONLINE, OFFLINE, MAINTENANCE
│   │       ├── Environment.java            # PRODUCTION, UAT, DEV, DR
│   │       └── ChangeType.java             # INSTALL, UPGRADE, PATCH, CONFIG
│   ├── virtualization/
│   │   ├── Hypervisor.java
│   │   ├── VirtualMachine.java
│   │   ├── Snapshot.java
│   │   └── enums/
│   │       ├── HypervisorType.java         # VMWARE, KVM, HYPERV
│   │       └── VmStatus.java               # RUNNING, STOPPED, SUSPENDED
│   ├── backup/
│   │   ├── BackupJob.java
│   │   ├── BackupExecution.java
│   │   ├── DRPlan.java
│   │   ├── DrillLog.java
│   │   └── enums/
│   │       ├── BackupType.java             # FULL, INCREMENTAL, DIFFERENTIAL
│   │       └── ExecutionStatus.java        # SUCCESS, FAILED, PARTIAL, RUNNING
│   ├── migration/
│   │   ├── MigrationProject.java
│   │   ├── MigrationTask.java
│   │   ├── MigrationValidation.java
│   │   └── enums/
│   │       ├── MigrationStatus.java        # PLANNING, IN_PROGRESS, COMPLETED, FAILED, ROLLED_BACK
│   │       └── ValidationType.java         # ROW_COUNT, CHECKSUM, SAMPLE_CHECK
│   ├── report/
│   │   ├── ReportTemplate.java
│   │   ├── ReportSchedule.java
│   │   └── ReportArchive.java
│   ├── testing/
│   │   ├── TestCase.java
│   │   └── TestExecution.java
│   └── auth/
│       ├── User.java
│       └── AuditLog.java
│
├── repository/                             # Spring Data JPA Repositories
│   ├── server/
│   │   ├── ServerRepository.java
│   │   ├── HealthMetricRepository.java
│   │   └── InstallationRepository.java
│   ├── virtualization/
│   │   ├── HypervisorRepository.java
│   │   ├── VirtualMachineRepository.java
│   │   └── SnapshotRepository.java
│   ├── backup/
│   │   ├── BackupJobRepository.java
│   │   ├── BackupExecutionRepository.java
│   │   ├── DRPlanRepository.java
│   │   └── DrillLogRepository.java
│   ├── migration/
│   │   ├── MigrationProjectRepository.java
│   │   ├── MigrationTaskRepository.java
│   │   └── MigrationValidationRepository.java
│   ├── report/
│   │   ├── ReportTemplateRepository.java
│   │   ├── ReportScheduleRepository.java
│   │   └── ReportArchiveRepository.java
│   ├── testing/
│   │   ├── TestCaseRepository.java
│   │   └── TestExecutionRepository.java
│   └── auth/
│       ├── UserRepository.java
│       └── AuditLogRepository.java
│
├── dto/                                    # Data Transfer Objects
│   ├── request/
│   │   ├── ServerCreateRequest.java
│   │   ├── VmCreateRequest.java
│   │   ├── BackupJobCreateRequest.java
│   │   ├── MigrationProjectCreateRequest.java
│   │   ├── DRPlanCreateRequest.java
│   │   ├── ReportGenerateRequest.java
│   │   └── LoginRequest.java
│   ├── response/
│   │   ├── ServerResponse.java
│   │   ├── DashboardSummary.java
│   │   ├── HealthMetricSummary.java
│   │   ├── BackupTrendData.java
│   │   ├── MigrationProgressData.java
│   │   ├── PageResponse.java              # Generic paginated response
│   │   └── ApiResponse.java               # Standard { success, message, data }
│   └── mapper/
│       └── EntityMapper.java              # MapStruct or manual mapping
│
├── service/
│   ├── server/
│   │   ├── ServerService.java
│   │   ├── HealthMetricService.java
│   │   └── SystemSpecService.java         # Generate system spec PDF
│   ├── virtualization/
│   │   ├── VirtualizationService.java
│   │   └── CapacityPlanningService.java
│   ├── backup/
│   │   ├── BackupService.java
│   │   ├── DRPlanService.java
│   │   └── BackupAnalyticsService.java
│   ├── migration/
│   │   ├── MigrationService.java
│   │   └── DataValidationService.java
│   ├── report/
│   │   ├── ReportGenerationService.java   # JasperReports + POI
│   │   ├── ReportScheduleService.java
│   │   └── ReportArchiveService.java
│   ├── testing/
│   │   ├── TestCaseService.java
│   │   └── HealthCheckService.java        # Automated checks
│   ├── dashboard/
│   │   └── DashboardService.java          # Aggregated stats
│   └── auth/
│       ├── AuthService.java
│       ├── JwtService.java
│       └── AuditService.java
│
├── controller/
│   ├── api/                               # REST API controllers (JSON)
│   │   ├── ServerApiController.java       # /api/servers
│   │   ├── VirtualizationApiController.java  # /api/virtualization
│   │   ├── BackupApiController.java       # /api/backup
│   │   ├── DRApiController.java           # /api/dr
│   │   ├── MigrationApiController.java    # /api/migrations
│   │   ├── ReportApiController.java       # /api/reports
│   │   ├── TestApiController.java         # /api/tests
│   │   ├── DashboardApiController.java    # /api/dashboard
│   │   └── AuthApiController.java         # /api/auth
│   └── web/                               # Thymeleaf page controllers
│       ├── DashboardController.java       # /
│       ├── ServerWebController.java       # /servers
│       ├── VmWebController.java           # /virtualization
│       ├── BackupWebController.java       # /backup
│       ├── MigrationWebController.java    # /migrations
│       ├── ReportWebController.java       # /reports
│       ├── TestWebController.java         # /tests
│       └── LoginController.java           # /login
│
├── scheduler/                             # Quartz Jobs
│   ├── ScheduledReportJob.java            # Generate & email scheduled reports
│   ├── HealthCheckJob.java                # Run automated health checks
│   ├── MetricCollectionJob.java           # Collect server metrics (simulated)
│   └── SnapshotAlertJob.java              # Alert on old VM snapshots
│
├── report/                                # JasperReports processors
│   ├── JasperReportProcessor.java         # Core PDF generation logic
│   ├── ExcelReportProcessor.java          # Apache POI Excel generation
│   └── ReportDataSourceBuilder.java       # Build JRDataSource from entities
│
└── exception/
    ├── GlobalExceptionHandler.java        # @ControllerAdvice
    ├── ResourceNotFoundException.java
    ├── BusinessValidationException.java
    └── ReportGenerationException.java
```

---

## 3. Key Design Patterns

### 3.1 Entity Inheritance — BaseEntity
All entities extend `BaseEntity` providing `id` (UUID), `createdAt`, `updatedAt`, `createdBy` with JPA auditing.

```java
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @CreatedBy
    private String createdBy;
}
```

### 3.2 Repository Query Methods
Use Spring Data JPA derived queries and `@Query` for complex aggregations:

```java
public interface HealthMetricRepository extends JpaRepository<HealthMetric, UUID> {
    List<HealthMetric> findByServerIdAndTimestampBetween(UUID serverId, LocalDateTime from, LocalDateTime to);

    @Query("SELECT new com.infrawatch.dto.response.HealthMetricSummary(h.server.id, AVG(h.cpuPercent), AVG(h.memPercent), AVG(h.diskPercent)) " +
           "FROM HealthMetric h WHERE h.timestamp >= :since GROUP BY h.server.id")
    List<HealthMetricSummary> getAverageMetricsSince(@Param("since") LocalDateTime since);
}
```

### 3.3 Service Layer Pattern
Each service encapsulates business logic, validation, and cross-cutting concerns:

```java
@Service
@Transactional
public class ServerService {
    private final ServerRepository serverRepository;
    private final AuditService auditService;

    public Server createServer(ServerCreateRequest request) {
        // Validate uniqueness
        if (serverRepository.existsByHostname(request.getHostname())) {
            throw new BusinessValidationException("Server hostname already exists");
        }
        Server server = mapToEntity(request);
        server = serverRepository.save(server);
        auditService.log("CREATE", "Server", server.getId(), "Created server: " + server.getHostname());
        return server;
    }
}
```

### 3.4 Report Generation Flow
```
ReportGenerateRequest
    → ReportGenerationService.generate()
        → Fetch data from relevant repository
        → Build JRDataSource / Excel data
        → If PDF: JasperReportProcessor.generatePdf(templatePath, dataSource, params)
        → If Excel: ExcelReportProcessor.generateExcel(data, headers, sheetName)
        → Save to ReportArchive
        → Return byte[] or file path
```

### 3.5 Scheduled Job Architecture
Quartz jobs are persistent (stored in DB) and survive restarts:

```java
@Component
public class ScheduledReportJob implements Job {
    @Autowired
    private ReportGenerationService reportService;
    @Autowired
    private EmailService emailService;

    @Override
    public void execute(JobExecutionContext context) {
        UUID scheduleId = UUID.fromString(context.getMergedJobDataMap().getString("scheduleId"));
        ReportSchedule schedule = reportScheduleService.findById(scheduleId);
        byte[] report = reportService.generateByTemplate(schedule.getTemplate(), schedule.getParameters());
        emailService.sendWithAttachment(schedule.getRecipients(), "Scheduled Report", report);
    }
}
```

---

## 4. Security Architecture

### 4.1 Authentication Flow
```
Browser → /login (POST username/password)
    → AuthService.authenticate()
        → BCrypt password verification
        → Generate JWT token (24h expiry)
        → Set HTTP-only cookie + return token
    → Redirect to dashboard

API Client → /api/auth/login (POST JSON)
    → Returns { token: "eyJ..." }
    → Subsequent requests: Authorization: Bearer <token>
```

### 4.2 Role-Based Access Control

| Role | Dashboard | View Data | Create/Edit | Delete | Reports | Admin |
|------|:---------:|:---------:|:-----------:|:------:|:-------:|:-----:|
| VIEWER | ✅ | ✅ | ❌ | ❌ | ✅ (view only) | ❌ |
| OPERATOR | ✅ | ✅ | ✅ | ❌ | ✅ | ❌ |
| ADMIN | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |

### 4.3 Audit Trail
Every write operation is logged to `audit_log` table:
```json
{
  "id": "uuid",
  "userId": "uuid",
  "action": "CREATE",
  "entityType": "Server",
  "entityId": "uuid",
  "timestamp": "2026-03-30T10:30:00",
  "details": "Created server: web-prod-01",
  "ipAddress": "192.168.1.100"
}
```

---

## 5. Frontend Architecture

### 5.1 Thymeleaf + HTMX Approach
- Server-side rendered HTML (no SPA complexity)
- HTMX for partial page updates without full reload
- Chart.js for data visualization (loaded via CDN)
- Bootstrap 5 for responsive layout

### 5.2 Page Layout
```html
<!-- layout/main.html -->
<html>
<head>
    <link href="/css/bootstrap.min.css" rel="stylesheet">
    <link href="/css/infrawatch.css" rel="stylesheet">
</head>
<body>
    <nav th:replace="~{fragments/sidebar :: sidebar}"></nav>
    <main class="content">
        <div th:replace="~{fragments/breadcrumb :: breadcrumb}"></div>
        <div layout:fragment="content"></div>
    </main>
    <script src="/js/htmx.min.js"></script>
    <script src="/js/chart.min.js"></script>
</body>
</html>
```

### 5.3 HTMX Interaction Example
```html
<!-- Server list with inline edit -->
<table id="server-table">
    <tr th:each="server : ${servers}">
        <td th:text="${server.hostname}"></td>
        <td>
            <span th:class="${server.status.cssClass}" th:text="${server.status}"></span>
        </td>
        <td>
            <button hx-get="/servers/{id}/edit" hx-target="closest tr" hx-swap="outerHTML">
                Edit
            </button>
        </td>
    </tr>
</table>
```

---

## 6. Database Migration Strategy (Flyway)

Migration files follow naming convention: `V{version}__{description}.sql`

```
db/migration/
├── V1__create_auth_tables.sql              # users, audit_log
├── V2__create_server_tables.sql            # servers, health_metrics, installations
├── V3__create_virtualization_tables.sql    # hypervisors, virtual_machines, snapshots
├── V4__create_backup_tables.sql            # backup_jobs, backup_executions, dr_plans, drill_logs
├── V5__create_migration_tables.sql         # migration_projects, migration_tasks, migration_validations
├── V6__create_report_tables.sql            # report_templates, report_schedules, report_archives
├── V7__create_testing_tables.sql           # test_cases, test_executions
├── V8__insert_default_data.sql             # Default admin user, report templates
└── V9__insert_demo_data.sql                # Demo data for showcase
```

---

## 7. Docker Configuration

### Dockerfile (multi-stage build)
```dockerfile
# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/infrawatch-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### docker-compose.yml
```yaml
version: '3.8'
services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/infrawatch
      - SPRING_DATASOURCE_USERNAME=infrawatch
      - SPRING_DATASOURCE_PASSWORD=infrawatch123
    depends_on:
      db:
        condition: service_healthy

  db:
    image: postgres:15
    environment:
      - POSTGRES_DB=infrawatch
      - POSTGRES_USER=infrawatch
      - POSTGRES_PASSWORD=infrawatch123
    ports:
      - "5432:5432"
    volumes:
      - pgdata:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U infrawatch"]
      interval: 5s
      timeout: 5s
      retries: 5

volumes:
  pgdata:
```

---

## 8. Configuration Files

### application.yml
```yaml
spring:
  application:
    name: infrawatch
  jpa:
    hibernate:
      ddl-auto: validate  # Flyway manages schema
    show-sql: false
  flyway:
    enabled: true
    locations: classpath:db/migration

server:
  port: 8080

infrawatch:
  jwt:
    secret: ${JWT_SECRET:your-256-bit-secret-key-here-change-in-production}
    expiration-ms: 86400000  # 24 hours
  reports:
    output-dir: ./reports
    templates-dir: classpath:reports/
  metrics:
    retention-days: 90
  snapshots:
    alert-age-days: 7
```

### application-dev.yml
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:infrawatch
    driver-class-name: org.h2.Driver
  h2:
    console:
      enabled: true
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
  flyway:
    enabled: false  # Use JPA auto-DDL in dev
```

### application-prod.yml
```yaml
spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL}
    username: ${SPRING_DATASOURCE_USERNAME}
    password: ${SPRING_DATASOURCE_PASSWORD}
  jpa:
    show-sql: false
```
