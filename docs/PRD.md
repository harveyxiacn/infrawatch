# InfraWatch — Product Requirements Document (PRD)

**Version:** 1.0  
**Author:** Harvey Xia  
**Date:** 2026-03-30  
**Status:** Approved for Development

---

## 1. Overview

### 1.1 Problem Statement
Infrastructure teams in large public-sector and enterprise organizations manage hundreds of servers, virtual machines, backup jobs, and data migration projects. Without a centralized reporting platform, teams rely on scattered spreadsheets, manual checks, and ad-hoc scripts — resulting in delayed incident response, missed backup failures, and poor audit readiness.

### 1.2 Solution
InfraWatch is a Java-based web application that consolidates infrastructure monitoring data into a single dashboard with automated report generation. It covers the five pillars of infrastructure management: server health, virtualization, backup/DR, data migration, and compliance reporting.

### 1.3 Target Users
- System Administrators (SA)
- Infrastructure Engineers
- IT Operations Managers
- Compliance/Audit teams

---

## 2. Functional Requirements

### 2.1 Server & System Management Module

#### FR-SVR-001: Server Inventory
- CRUD operations for server records
- Fields: hostname, IP, OS, CPU cores, RAM (GB), disk (GB), location (datacenter/rack), status (online/offline/maintenance), environment (prod/uat/dev)
- Bulk import from CSV/Excel

#### FR-SVR-002: System Specification Generator
- Auto-generate system specification documents in PDF format
- Template includes: hardware config, OS version, installed software, network config, security settings
- Version history for each spec document
- Diff comparison between spec versions

#### FR-SVR-003: Health Metrics Collection
- Scheduled collection of server health metrics via agent or API
- Metrics: CPU usage %, memory usage %, disk usage %, network I/O, uptime
- Store time-series data with configurable retention (default: 90 days)
- Alert thresholds: warning (80%) and critical (95%) — configurable per server

#### FR-SVR-004: Installation Tracking
- Log system installation and configuration changes
- Fields: timestamp, server, change type (install/upgrade/patch/config), description, performed by, approval reference
- Audit trail — immutable log entries

### 2.2 Virtualization Module

#### FR-VM-001: Hypervisor Inventory
- Register hypervisor hosts (VMware ESXi, KVM, Hyper-V)
- Fields: hostname, type, version, total CPU/RAM/storage, cluster membership

#### FR-VM-002: Virtual Machine Inventory
- CRUD for VM records
- Fields: VM name, guest OS, vCPU, vRAM, vDisk, host assignment, status, created date
- Link VMs to their parent hypervisor

#### FR-VM-003: Resource Utilization Reports
- VM resource utilization over time (CPU, memory, disk I/O)
- Host resource allocation ratio (allocated vs total capacity)
- Over-committed resource alerts
- Capacity planning: projected resource exhaustion date

#### FR-VM-004: Snapshot Management
- Track VM snapshots: name, creation date, size, description
- Alert on snapshots older than X days (default: 7)
- Snapshot cleanup recommendations

### 2.3 Backup & Disaster Recovery Module

#### FR-DR-001: Backup Job Management
- Register backup jobs: name, type (full/incremental/differential), source, target, schedule (cron expression), retention policy
- Track execution history: start time, end time, status (success/failed/partial), data size, duration

#### FR-DR-002: DR Plan Documentation
- Create and version DR plans per system/application
- Fields: system name, RTO, RPO, recovery steps, dependencies, responsible team, last review date
- Export DR plan as PDF

#### FR-DR-003: DR Drill Management
- Schedule and log DR drill exercises
- Fields: drill date, scope, participants, steps executed, issues found, resolution, result (pass/fail)
- Drill compliance report: % of systems with completed drills in last 12 months

#### FR-DR-004: Backup Trend Analysis
- Dashboard showing backup success/failure rate over time
- Storage consumption trend
- Failed backup alert with auto-retry recommendation

### 2.4 Data Migration Module

#### FR-MIG-001: Migration Project Management
- Create migration projects: name, description, source system, target system, type (database/file/application), planned start/end date, status
- Assign team members and stakeholders

#### FR-MIG-002: Migration Task Tracking
- Break projects into tasks: table/dataset name, source, target, row count (expected), transformation rules, status (pending/in-progress/completed/failed)
- Dependency mapping between tasks

#### FR-MIG-003: Data Validation
- Post-migration validation checks:
  - Row count comparison (source vs target)
  - Checksum/hash verification
  - Sample data spot check
- Validation result dashboard with pass/fail indicators

#### FR-MIG-004: Rollback Planning
- Document rollback procedures per migration task
- Pre-migration backup verification
- Rollback execution log

### 2.5 Report Engine Module

#### FR-RPT-001: Report Templates
- Pre-built templates:
  - Daily Server Health Summary
  - Weekly Virtualization Capacity Report
  - Monthly Backup Compliance Report
  - Data Migration Progress Report
  - DR Readiness Report
  - System Inventory Report

#### FR-RPT-002: Report Generation
- Generate reports in PDF (JasperReports) and Excel (Apache POI)
- Parameters: date range, server group, environment filter
- Include charts (bar, line, pie) rendered server-side

#### FR-RPT-003: Scheduled Reports
- Configure scheduled report generation via Quartz Scheduler
- Delivery via email with attached PDF/Excel
- Cron expression for flexible scheduling

#### FR-RPT-004: Report Archive
- Store generated reports with metadata
- Search and download historical reports
- Retention policy (configurable, default: 1 year)

### 2.6 System Testing Module

#### FR-TST-001: Test Case Management
- Define infrastructure test cases: name, category (connectivity/performance/security/backup-restore), steps, expected result
- Link test cases to servers/systems

#### FR-TST-002: Automated Health Checks
- Ping/TCP connectivity checks
- Disk space threshold checks
- Service status checks (e.g., is PostgreSQL running)
- Results stored in test execution history

#### FR-TST-003: Test Execution Dashboard
- View test run history with pass/fail/error counts
- Trend chart of test results over time
- Export test results as report

---

## 3. Non-Functional Requirements

### NFR-001: Performance
- Dashboard page load < 2 seconds for up to 500 servers
- Report generation < 30 seconds for 1000-row dataset
- API response time < 500ms for CRUD operations

### NFR-002: Security
- Spring Security with role-based access (ADMIN, OPERATOR, VIEWER)
- Password hashing with BCrypt
- CSRF protection enabled
- API authentication via JWT tokens
- Audit log for all write operations

### NFR-003: Scalability
- Stateless application design — horizontally scalable behind load balancer
- Database connection pooling (HikariCP)
- Pagination for all list endpoints (default: 20 items)

### NFR-004: Deployment
- Docker image with multi-stage build
- Docker Compose for local development (app + PostgreSQL)
- Environment-based configuration (dev/prod profiles)
- Health check endpoint at `/actuator/health`

### NFR-005: Testing
- Unit test coverage > 70%
- Integration tests using Testcontainers (PostgreSQL)
- API tests for all endpoints

---

## 4. User Interface Requirements

### 4.1 Dashboard (Home Page)
- Summary cards: total servers, total VMs, backup success rate (24h), active migrations
- Server health heatmap (green/yellow/red)
- Recent alerts list
- Quick links to all modules

### 4.2 Navigation
- Left sidebar with module icons
- Breadcrumb navigation
- Responsive layout (desktop-first, tablet-friendly)

### 4.3 Report Viewer
- In-browser PDF preview
- Download buttons for PDF and Excel
- Schedule button to set up recurring delivery

---

## 5. Data Model Summary

| Entity | Key Fields | Relationships |
|--------|-----------|---------------|
| Server | hostname, ip, os, cpu, ram, disk, status | has many HealthMetrics, Installations, TestCases |
| HealthMetric | server_id, timestamp, cpu_pct, mem_pct, disk_pct | belongs to Server |
| Installation | server_id, change_type, description, performed_by, timestamp | belongs to Server |
| Hypervisor | hostname, type, version, total_cpu, total_ram | has many VirtualMachines |
| VirtualMachine | name, guest_os, vcpu, vram, host_id, status | belongs to Hypervisor, has many Snapshots |
| Snapshot | vm_id, name, size_gb, created_at | belongs to VirtualMachine |
| BackupJob | name, type, source, target, schedule_cron | has many BackupExecutions |
| BackupExecution | job_id, start_time, end_time, status, data_size_gb | belongs to BackupJob |
| DRPlan | system_name, rto_minutes, rpo_minutes, steps_json, version | has many DrillLogs |
| DrillLog | plan_id, drill_date, result, issues_found | belongs to DRPlan |
| MigrationProject | name, source_system, target_system, status, planned_start | has many MigrationTasks |
| MigrationTask | project_id, dataset_name, source, target, expected_rows, status | belongs to MigrationProject, has many Validations |
| MigrationValidation | task_id, check_type, source_value, target_value, passed | belongs to MigrationTask |
| ReportTemplate | name, type, jrxml_path, description | has many ReportArchives |
| ReportArchive | template_id, generated_at, file_path, parameters_json | belongs to ReportTemplate |
| ReportSchedule | template_id, cron_expression, recipients_email, enabled | belongs to ReportTemplate |
| TestCase | name, category, server_id, steps, expected_result | belongs to Server, has many TestExecutions |
| TestExecution | test_case_id, executed_at, result, actual_output | belongs to TestCase |
| User | username, password_hash, role, email | system user |
| AuditLog | user_id, action, entity_type, entity_id, timestamp, details | immutable log |

---

## 6. MVP Scope (Phase 1)

For the initial release, implement:
1. ✅ Server inventory CRUD + health metrics display
2. ✅ VM inventory CRUD + host mapping
3. ✅ Backup job tracking + execution history
4. ✅ Migration project + task tracking + validation
5. ✅ DR plan CRUD + drill logging
6. ✅ Report generation (PDF + Excel) for all modules
7. ✅ Dashboard with summary cards and charts
8. ✅ Role-based access control
9. ✅ REST API with Swagger documentation
10. ✅ Docker Compose deployment

### Phase 2 (Future)
- Real-time metrics collection via agent
- Email notification for alerts
- LDAP/AD integration
- Custom report builder UI
- Multi-tenant support
