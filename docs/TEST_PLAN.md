# InfraWatch — Test Plan & Strategy

**Version:** 1.0  
**Author:** Harvey Xia  
**Date:** 2026-03-30

---

## 1. Test Strategy Overview

### 1.1 Test Pyramid
```
         ┌──────────┐
         │   E2E    │  ← Minimal: key user flows via browser
        ┌┴──────────┴┐
        │ Integration │  ← API tests with Testcontainers (PostgreSQL)
       ┌┴────────────┴┐
       │  Unit Tests    │  ← Service + utility logic, mocked dependencies
      └────────────────┘
```

### 1.2 Coverage Target
- Unit tests: > 70% line coverage on service layer
- Integration tests: All REST API endpoints (happy path + error cases)
- Report generation: Verified output for each template

### 1.3 Tools
| Tool | Purpose |
|------|---------|
| JUnit 5 | Test framework |
| Mockito | Mocking dependencies in unit tests |
| Testcontainers | Real PostgreSQL for integration tests |
| Spring Boot Test | @WebMvcTest, @DataJpaTest, @SpringBootTest |
| MockMvc | Controller endpoint testing |
| AssertJ | Fluent assertions |
| Jacoco | Code coverage reporting |

---

## 2. Unit Tests

### 2.1 Service Layer Tests

Each service class gets a corresponding test class with mocked repository:

#### ServerServiceTest
```
- createServer_success
- createServer_duplicateHostname_throwsException
- getServerById_found
- getServerById_notFound_throwsResourceNotFoundException
- updateServer_success
- updateServer_notFound_throwsException
- deleteServer_success
- listServers_withPagination
- listServers_filterByStatus
- listServers_filterByEnvironment
```

#### VirtualizationServiceTest
```
- createHypervisor_success
- createVm_success
- createVm_hypervisorNotFound_throwsException
- getCapacityReport_calculatesCorrectly
- getCapacityReport_detectsOvercommittedHosts
- getOvercommittedHosts_returnsCorrectList
```

#### BackupServiceTest
```
- createBackupJob_success
- logExecution_success
- logExecution_jobNotFound_throwsException
- getBackupTrend_calculatesSuccessRate
- getBackupTrend_30days_correctAggregation
```

#### MigrationServiceTest
```
- createProject_success
- addTask_success
- addTask_projectNotFound_throwsException
- updateTaskStatus_validTransition
- updateTaskStatus_invalidTransition_throwsException
- calculateProgress_allCompleted_returns100
- calculateProgress_mixed_returnsCorrectPercentage
```

#### DRPlanServiceTest
```
- createPlan_success
- updatePlan_incrementsVersion
- logDrill_success
- getComplianceReport_calculatesCorrectly
- getOverdueSystems_returnsCorrectList
```

#### ReportGenerationServiceTest
```
- generatePdfReport_success
- generateExcelReport_success
- generateReport_templateNotFound_throwsException
- generateReport_invalidParameters_throwsException
```

#### DashboardServiceTest
```
- getSummary_aggregatesAllModules
- getSummary_emptyDatabase_returnsZeros
- getRecentAlerts_returnsTopN
```

#### DataValidationServiceTest
```
- validateRowCount_matching_passes
- validateRowCount_mismatch_fails
- validateChecksum_matching_passes
- validateChecksum_mismatch_fails
```

### 2.2 Utility / Helper Tests

#### JwtServiceTest
```
- generateToken_containsCorrectClaims
- validateToken_validToken_returnsTrue
- validateToken_expiredToken_returnsFalse
- validateToken_tamperedToken_returnsFalse
- extractUsername_returnsCorrectValue
```

---

## 3. Integration Tests (API Layer)

Using `@SpringBootTest` with Testcontainers for real PostgreSQL:

### 3.1 Server API Tests (`ServerApiControllerIT`)
```
- POST /api/servers — 201 Created
- POST /api/servers — 400 Bad Request (missing required fields)
- POST /api/servers — 409 Conflict (duplicate hostname)
- GET /api/servers — 200 with paginated results
- GET /api/servers?status=ONLINE — 200 filtered results
- GET /api/servers/{id} — 200
- GET /api/servers/{id} — 404 Not Found
- PUT /api/servers/{id} — 200 Updated
- DELETE /api/servers/{id} — 204 No Content (ADMIN role)
- DELETE /api/servers/{id} — 403 Forbidden (VIEWER role)
- POST /api/servers/{id}/metrics — 201
- GET /api/servers/{id}/metrics?from=...&to=... — 200
- POST /api/servers/{id}/installations — 201
- GET /api/servers/{id}/spec — 200 returns PDF
```

### 3.2 Virtualization API Tests (`VirtualizationApiControllerIT`)
```
- POST /api/virtualization/hypervisors — 201
- GET /api/virtualization/hypervisors — 200
- POST /api/virtualization/vms — 201
- GET /api/virtualization/vms — 200 paginated
- GET /api/virtualization/vms?hostId=... — 200 filtered
- POST /api/virtualization/vms/{id}/snapshots — 201
- GET /api/virtualization/capacity — 200 with correct calculations
```

### 3.3 Backup API Tests (`BackupApiControllerIT`)
```
- POST /api/backup/jobs — 201
- GET /api/backup/jobs — 200
- POST /api/backup/jobs/{id}/executions — 201
- GET /api/backup/jobs/{id}/executions — 200
- GET /api/backup/trend?days=30 — 200 with aggregated data
```

### 3.4 DR API Tests (`DRApiControllerIT`)
```
- POST /api/dr/plans — 201
- GET /api/dr/plans — 200
- GET /api/dr/plans/{id}/pdf — 200 returns PDF
- POST /api/dr/drills — 201
- GET /api/dr/compliance — 200 with correct percentages
```

### 3.5 Migration API Tests (`MigrationApiControllerIT`)
```
- POST /api/migrations — 201
- GET /api/migrations — 200
- POST /api/migrations/{id}/tasks — 201
- PUT /api/migrations/{projectId}/tasks/{taskId}/status — 200
- POST /api/migrations/{projectId}/tasks/{taskId}/validate — 200
- GET /api/migrations/{id}/progress — 200 correct percentage
```

### 3.6 Report API Tests (`ReportApiControllerIT`)
```
- GET /api/reports/templates — 200 lists all templates
- POST /api/reports/generate — 200 returns PDF
- POST /api/reports/generate — 200 returns Excel
- POST /api/reports/generate — 400 invalid template
- GET /api/reports/archive — 200
```

### 3.7 Auth & Security Tests (`AuthApiControllerIT`)
```
- POST /api/auth/login — 200 returns token
- POST /api/auth/login — 401 wrong password
- GET /api/servers — 401 no token
- GET /api/servers — 200 valid token
- DELETE /api/servers/{id} — 403 VIEWER role
- DELETE /api/servers/{id} — 204 ADMIN role
```

### 3.8 Dashboard API Tests (`DashboardApiControllerIT`)
```
- GET /api/dashboard/summary — 200 aggregated data
```

---

## 4. Report Output Verification

For each report template, verify:

| Template | Verify |
|----------|--------|
| Server Health Summary | Contains server table, CPU/MEM/DISK charts, alert section |
| VM Capacity Report | Shows host-to-VM mapping, allocation ratios, projections |
| Backup Compliance | Success rate chart, failed job list, trend graph |
| Migration Progress | Project status, task list with percentages, validation results |
| DR Readiness | Plan summary table, drill compliance %, overdue list |
| System Inventory | Complete server and VM listing with specs |

---

## 5. Test Data Management

### 5.1 Unit Tests
- Use `@BeforeEach` to create test fixtures
- Mockito `when(...).thenReturn(...)` for repository responses

### 5.2 Integration Tests
- Testcontainers PostgreSQL — fresh database per test class
- `@Sql` annotation to load test data sets
- `@DirtiesContext` only when necessary (prefer test isolation)

### 5.3 Test Data Files
```
src/test/resources/
├── test-data/
│   ├── servers.json           # 5 test servers
│   ├── hypervisors.json       # 2 test hypervisors
│   ├── backup-jobs.json       # 3 test backup jobs
│   └── migration-project.json # 1 test project with tasks
└── sql/
    ├── insert-servers.sql
    ├── insert-vms.sql
    └── insert-backups.sql
```

---

## 6. CI/CD Integration

### Maven Configuration (pom.xml)
```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.11</version>
    <executions>
        <execution>
            <goals><goal>prepare-agent</goal></goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals><goal>report</goal></goals>
        </execution>
    </executions>
</plugin>
```

### GitHub Actions Workflow
```yaml
name: CI
on: [push, pull_request]
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
      - run: mvn test
      - uses: actions/upload-artifact@v4
        with:
          name: coverage-report
          path: target/site/jacoco/
```

---

## 7. Running Tests

```bash
# Run all tests
mvn test

# Run unit tests only
mvn test -Dgroups=unit

# Run integration tests only
mvn test -Dgroups=integration

# Generate coverage report
mvn test jacoco:report
# View at: target/site/jacoco/index.html

# Run specific test class
mvn test -Dtest=ServerServiceTest

# Run with verbose output
mvn test -X
```
