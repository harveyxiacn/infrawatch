# InfraWatch — Database Schema Design

**Version:** 1.0  
**Database:** PostgreSQL 15 (production) / H2 (development)  
**Migration:** Flyway

---

## Entity Relationship Overview

```
┌──────────┐     ┌──────────────┐     ┌──────────────┐
│  Server   │────<│ HealthMetric │     │  Hypervisor  │
│           │     └──────────────┘     │              │
│           │────<┌──────────────┐     │              │────<┌────────────────┐
│           │     │ Installation │     └──────────────┘     │ VirtualMachine │
│           │     └──────────────┘                          │                │────<┌──────────┐
│           │────<┌──────────────┐                          └────────────────┘     │ Snapshot │
│           │     │   TestCase   │────<┌────────────────┐                          └──────────┘
└──────────┘     └──────────────┘     │ TestExecution  │
                                       └────────────────┘

┌───────────┐     ┌──────────────────┐
│ BackupJob │────<│ BackupExecution  │
└───────────┘     └──────────────────┘

┌──────────┐     ┌───────────┐
│  DRPlan  │────<│ DrillLog  │
└──────────┘     └───────────┘

┌───────────────────┐     ┌─────────────────┐     ┌──────────────────────┐
│ MigrationProject  │────<│ MigrationTask   │────<│ MigrationValidation  │
└───────────────────┘     └─────────────────┘     └──────────────────────┘

┌─────────────────┐     ┌─────────────────┐
│ ReportTemplate  │────<│ ReportSchedule  │
│                 │────<│ ReportArchive   │
└─────────────────┘     └─────────────────┘

┌──────────┐     ┌───────────┐
│   User   │────<│ AuditLog  │
└──────────┘     └───────────┘
```

---

## Table Definitions

### Authentication & Audit

#### users
```sql
CREATE TABLE users (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username        VARCHAR(50) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    email           VARCHAR(100),
    role            VARCHAR(20) NOT NULL DEFAULT 'VIEWER',  -- ADMIN, OPERATOR, VIEWER
    enabled         BOOLEAN NOT NULL DEFAULT true,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Default admin user (password: admin123, BCrypt hashed)
INSERT INTO users (username, password_hash, email, role)
VALUES ('admin', '$2a$10$...', 'admin@infrawatch.local', 'ADMIN');
```

#### audit_log
```sql
CREATE TABLE audit_log (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID REFERENCES users(id),
    username        VARCHAR(50) NOT NULL,
    action          VARCHAR(20) NOT NULL,  -- CREATE, UPDATE, DELETE, LOGIN, LOGOUT
    entity_type     VARCHAR(50) NOT NULL,  -- Server, VirtualMachine, BackupJob, etc.
    entity_id       UUID,
    details         TEXT,
    ip_address      VARCHAR(45),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_log_entity ON audit_log(entity_type, entity_id);
CREATE INDEX idx_audit_log_user ON audit_log(user_id);
CREATE INDEX idx_audit_log_created ON audit_log(created_at);
```

---

### Server Module

#### servers
```sql
CREATE TABLE servers (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    hostname        VARCHAR(255) NOT NULL UNIQUE,
    ip_address      VARCHAR(45) NOT NULL,
    os              VARCHAR(100),
    cpu_cores       INT NOT NULL DEFAULT 1,
    ram_gb          INT NOT NULL DEFAULT 1,
    disk_gb         INT NOT NULL DEFAULT 10,
    location        VARCHAR(255),           -- e.g. "DC1-Rack-A3-U12"
    status          VARCHAR(20) NOT NULL DEFAULT 'ONLINE',  -- ONLINE, OFFLINE, MAINTENANCE
    environment     VARCHAR(20) NOT NULL DEFAULT 'DEV',     -- PRODUCTION, UAT, DEV, DR
    description     TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by      VARCHAR(50)
);

CREATE INDEX idx_servers_status ON servers(status);
CREATE INDEX idx_servers_environment ON servers(environment);
CREATE INDEX idx_servers_hostname ON servers(hostname);
```

#### health_metrics
```sql
CREATE TABLE health_metrics (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    server_id       UUID NOT NULL REFERENCES servers(id) ON DELETE CASCADE,
    timestamp       TIMESTAMP NOT NULL DEFAULT NOW(),
    cpu_percent     DECIMAL(5,2),
    mem_percent     DECIMAL(5,2),
    disk_percent    DECIMAL(5,2),
    network_in_mbps DECIMAL(10,2),
    network_out_mbps DECIMAL(10,2),
    uptime_seconds  BIGINT
);

CREATE INDEX idx_health_metrics_server_time ON health_metrics(server_id, timestamp);
-- Partitioning by month recommended for large deployments
```

#### installations
```sql
CREATE TABLE installations (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    server_id           UUID NOT NULL REFERENCES servers(id) ON DELETE CASCADE,
    change_type         VARCHAR(20) NOT NULL,  -- INSTALL, UPGRADE, PATCH, CONFIG
    description         TEXT NOT NULL,
    performed_by        VARCHAR(100) NOT NULL,
    approval_reference  VARCHAR(100),           -- Change ticket reference
    created_at          TIMESTAMP NOT NULL DEFAULT NOW()
    -- No updated_at — audit log entries are immutable
);

CREATE INDEX idx_installations_server ON installations(server_id);
CREATE INDEX idx_installations_created ON installations(created_at);
```

---

### Virtualization Module

#### hypervisors
```sql
CREATE TABLE hypervisors (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    hostname        VARCHAR(255) NOT NULL UNIQUE,
    type            VARCHAR(20) NOT NULL,   -- VMWARE, KVM, HYPERV
    version         VARCHAR(50),
    total_cpu_cores INT NOT NULL,
    total_ram_gb    INT NOT NULL,
    total_storage_gb INT NOT NULL,
    cluster         VARCHAR(100),
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',  -- ACTIVE, MAINTENANCE, DECOMMISSIONED
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by      VARCHAR(50)
);
```

#### virtual_machines
```sql
CREATE TABLE virtual_machines (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(255) NOT NULL,
    guest_os        VARCHAR(100),
    vcpu            INT NOT NULL DEFAULT 1,
    vram_gb         INT NOT NULL DEFAULT 1,
    vdisk_gb        INT NOT NULL DEFAULT 10,
    hypervisor_id   UUID NOT NULL REFERENCES hypervisors(id),
    status          VARCHAR(20) NOT NULL DEFAULT 'RUNNING',  -- RUNNING, STOPPED, SUSPENDED
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by      VARCHAR(50)
);

CREATE INDEX idx_vms_hypervisor ON virtual_machines(hypervisor_id);
CREATE INDEX idx_vms_status ON virtual_machines(status);
```

#### snapshots
```sql
CREATE TABLE snapshots (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    vm_id           UUID NOT NULL REFERENCES virtual_machines(id) ON DELETE CASCADE,
    name            VARCHAR(255) NOT NULL,
    size_gb         DECIMAL(10,2),
    description     TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_snapshots_vm ON snapshots(vm_id);
CREATE INDEX idx_snapshots_created ON snapshots(created_at);
```

---

### Backup & DR Module

#### backup_jobs
```sql
CREATE TABLE backup_jobs (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(255) NOT NULL,
    type            VARCHAR(20) NOT NULL,    -- FULL, INCREMENTAL, DIFFERENTIAL
    source_system   VARCHAR(255) NOT NULL,
    target_location VARCHAR(255) NOT NULL,
    schedule_cron   VARCHAR(100),
    retention_days  INT NOT NULL DEFAULT 30,
    enabled         BOOLEAN NOT NULL DEFAULT true,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by      VARCHAR(50)
);
```

#### backup_executions
```sql
CREATE TABLE backup_executions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    job_id          UUID NOT NULL REFERENCES backup_jobs(id) ON DELETE CASCADE,
    start_time      TIMESTAMP NOT NULL,
    end_time        TIMESTAMP,
    status          VARCHAR(20) NOT NULL,   -- SUCCESS, FAILED, PARTIAL, RUNNING
    data_size_gb    DECIMAL(10,2),
    duration_seconds INT,
    notes           TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_backup_exec_job ON backup_executions(job_id);
CREATE INDEX idx_backup_exec_status ON backup_executions(status);
CREATE INDEX idx_backup_exec_start ON backup_executions(start_time);
```

#### dr_plans
```sql
CREATE TABLE dr_plans (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    system_name         VARCHAR(255) NOT NULL,
    rto_minutes         INT NOT NULL,
    rpo_minutes         INT NOT NULL,
    recovery_steps      JSONB NOT NULL,          -- Array of step strings
    dependencies        JSONB,                    -- Array of dependency strings
    responsible_team    VARCHAR(100),
    contact_email       VARCHAR(100),
    version             INT NOT NULL DEFAULT 1,
    last_review_date    DATE,
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by          VARCHAR(50)
);
```

#### drill_logs
```sql
CREATE TABLE drill_logs (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    plan_id         UUID NOT NULL REFERENCES dr_plans(id) ON DELETE CASCADE,
    drill_date      DATE NOT NULL,
    scope           TEXT NOT NULL,
    participants    JSONB,                    -- Array of participant names
    steps_executed  TEXT,
    issues_found    TEXT,
    resolution      TEXT,
    result          VARCHAR(10) NOT NULL,     -- PASS, FAIL
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by      VARCHAR(50)
);

CREATE INDEX idx_drill_logs_plan ON drill_logs(plan_id);
CREATE INDEX idx_drill_logs_date ON drill_logs(drill_date);
```

---

### Migration Module

#### migration_projects
```sql
CREATE TABLE migration_projects (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name                VARCHAR(255) NOT NULL,
    description         TEXT,
    source_system       VARCHAR(255) NOT NULL,
    target_system       VARCHAR(255) NOT NULL,
    migration_type      VARCHAR(20) NOT NULL,    -- DATABASE, FILE, APPLICATION
    status              VARCHAR(20) NOT NULL DEFAULT 'PLANNING',  -- PLANNING, IN_PROGRESS, COMPLETED, FAILED, ROLLED_BACK
    planned_start_date  DATE,
    planned_end_date    DATE,
    actual_start_date   DATE,
    actual_end_date     DATE,
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by          VARCHAR(50)
);

CREATE INDEX idx_migration_status ON migration_projects(status);
```

#### migration_tasks
```sql
CREATE TABLE migration_tasks (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id              UUID NOT NULL REFERENCES migration_projects(id) ON DELETE CASCADE,
    dataset_name            VARCHAR(255) NOT NULL,
    source_table            VARCHAR(255),
    target_table            VARCHAR(255),
    expected_row_count      BIGINT,
    actual_row_count        BIGINT,
    transformation_rules    TEXT,
    status                  VARCHAR(20) NOT NULL DEFAULT 'PENDING',  -- PENDING, IN_PROGRESS, COMPLETED, FAILED
    sort_order              INT NOT NULL DEFAULT 0,
    notes                   TEXT,
    created_at              TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_migration_tasks_project ON migration_tasks(project_id);
```

#### migration_task_dependencies
```sql
CREATE TABLE migration_task_dependencies (
    task_id         UUID NOT NULL REFERENCES migration_tasks(id) ON DELETE CASCADE,
    depends_on_id   UUID NOT NULL REFERENCES migration_tasks(id) ON DELETE CASCADE,
    PRIMARY KEY (task_id, depends_on_id)
);
```

#### migration_validations
```sql
CREATE TABLE migration_validations (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    task_id         UUID NOT NULL REFERENCES migration_tasks(id) ON DELETE CASCADE,
    validation_type VARCHAR(20) NOT NULL,    -- ROW_COUNT, CHECKSUM, SAMPLE_CHECK
    source_value    VARCHAR(255),
    target_value    VARCHAR(255),
    passed          BOOLEAN NOT NULL,
    notes           TEXT,
    executed_at     TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_migration_validations_task ON migration_validations(task_id);
```

---

### Report Module

#### report_templates
```sql
CREATE TABLE report_templates (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(255) NOT NULL UNIQUE,
    description     TEXT,
    type            VARCHAR(50) NOT NULL,    -- SERVER_HEALTH, VM_CAPACITY, BACKUP_COMPLIANCE, MIGRATION_PROGRESS, DR_READINESS, SYSTEM_INVENTORY
    jrxml_path      VARCHAR(255),            -- Path to JasperReports template file
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Seed default templates
INSERT INTO report_templates (name, type, jrxml_path, description) VALUES
('Daily Server Health Summary', 'SERVER_HEALTH', 'reports/server_health.jrxml', 'Daily overview of all server health metrics'),
('Weekly VM Capacity Report', 'VM_CAPACITY', 'reports/vm_capacity.jrxml', 'Weekly virtualization resource utilization and capacity planning'),
('Monthly Backup Compliance', 'BACKUP_COMPLIANCE', 'reports/backup_compliance.jrxml', 'Monthly backup job success rates and compliance status'),
('Data Migration Progress', 'MIGRATION_PROGRESS', 'reports/migration_progress.jrxml', 'Current status of all active data migration projects'),
('DR Readiness Report', 'DR_READINESS', 'reports/dr_readiness.jrxml', 'DR plan review status and drill compliance'),
('System Inventory Report', 'SYSTEM_INVENTORY', 'reports/system_inventory.jrxml', 'Complete inventory of servers, VMs, and infrastructure');
```

#### report_schedules
```sql
CREATE TABLE report_schedules (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    template_id     UUID NOT NULL REFERENCES report_templates(id),
    cron_expression VARCHAR(100) NOT NULL,
    format          VARCHAR(10) NOT NULL DEFAULT 'PDF',  -- PDF, EXCEL
    recipients_email VARCHAR(500),
    parameters_json JSONB,
    enabled         BOOLEAN NOT NULL DEFAULT true,
    last_run_at     TIMESTAMP,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);
```

#### report_archives
```sql
CREATE TABLE report_archives (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    template_id     UUID REFERENCES report_templates(id),
    report_name     VARCHAR(255) NOT NULL,
    format          VARCHAR(10) NOT NULL,
    file_path       VARCHAR(500) NOT NULL,
    file_size_bytes BIGINT,
    parameters_json JSONB,
    generated_at    TIMESTAMP NOT NULL DEFAULT NOW(),
    generated_by    VARCHAR(50)
);

CREATE INDEX idx_report_archives_template ON report_archives(template_id);
CREATE INDEX idx_report_archives_generated ON report_archives(generated_at);
```

---

### Testing Module

#### test_cases
```sql
CREATE TABLE test_cases (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(255) NOT NULL,
    category        VARCHAR(30) NOT NULL,    -- CONNECTIVITY, PERFORMANCE, SECURITY, BACKUP_RESTORE
    server_id       UUID REFERENCES servers(id),
    steps           TEXT NOT NULL,
    expected_result TEXT NOT NULL,
    enabled         BOOLEAN NOT NULL DEFAULT true,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by      VARCHAR(50)
);

CREATE INDEX idx_test_cases_server ON test_cases(server_id);
CREATE INDEX idx_test_cases_category ON test_cases(category);
```

#### test_executions
```sql
CREATE TABLE test_executions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    test_case_id    UUID NOT NULL REFERENCES test_cases(id) ON DELETE CASCADE,
    result          VARCHAR(10) NOT NULL,    -- PASS, FAIL, ERROR
    actual_output   TEXT,
    duration_ms     INT,
    executed_by     VARCHAR(50),
    executed_at     TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_test_exec_case ON test_executions(test_case_id);
CREATE INDEX idx_test_exec_date ON test_executions(executed_at);
```

---

## Demo Data Summary

The `V9__insert_demo_data.sql` migration inserts realistic demo data:

| Entity | Count | Notes |
|--------|-------|-------|
| Users | 3 | admin, operator, viewer |
| Servers | 20 | Mix of web/app/db servers across prod/uat/dev |
| Health Metrics | 2000+ | 7 days of hourly data for all servers |
| Installations | 30 | Various patches and config changes |
| Hypervisors | 3 | VMware, KVM, Hyper-V |
| Virtual Machines | 40 | Distributed across hypervisors |
| Snapshots | 15 | Some intentionally old for alert testing |
| Backup Jobs | 8 | Mix of full/incremental/differential |
| Backup Executions | 200+ | 30 days of history, ~95% success rate |
| DR Plans | 5 | Various systems with different RTO/RPO |
| Drill Logs | 8 | Mix of pass/fail results |
| Migration Projects | 2 | 1 completed, 1 in progress |
| Migration Tasks | 15 | With validation results |
| Test Cases | 10 | Connectivity and performance checks |
| Test Executions | 50+ | Historical results |
