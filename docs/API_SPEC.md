# InfraWatch — REST API Specification

**Version:** 1.0  
**Base URL:** `http://localhost:8080/api`  
**Auth:** Bearer JWT token (obtain via `/api/auth/login`)

---

## Authentication

### POST /api/auth/login
Login and obtain JWT token.

**Request:**
```json
{
  "username": "admin",
  "password": "admin123"
}
```

**Response (200):**
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "expiresIn": 86400000,
    "role": "ADMIN"
  }
}
```

---

## 1. Server Module — `/api/servers`

### GET /api/servers
List all servers (paginated).

**Query Params:** `page` (default 0), `size` (default 20), `status` (optional: ONLINE/OFFLINE/MAINTENANCE), `environment` (optional: PRODUCTION/UAT/DEV/DR), `search` (optional: hostname keyword)

**Response (200):**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": "uuid",
        "hostname": "web-prod-01",
        "ipAddress": "192.168.1.10",
        "os": "Ubuntu 22.04 LTS",
        "cpuCores": 8,
        "ramGb": 32,
        "diskGb": 500,
        "location": "DC1-Rack-A3",
        "status": "ONLINE",
        "environment": "PRODUCTION",
        "createdAt": "2026-01-15T10:00:00"
      }
    ],
    "totalElements": 150,
    "totalPages": 8,
    "currentPage": 0
  }
}
```

### POST /api/servers
Create a new server. **Roles: OPERATOR, ADMIN**

**Request:**
```json
{
  "hostname": "web-prod-02",
  "ipAddress": "192.168.1.11",
  "os": "Ubuntu 22.04 LTS",
  "cpuCores": 16,
  "ramGb": 64,
  "diskGb": 1000,
  "location": "DC1-Rack-A3",
  "environment": "PRODUCTION",
  "description": "Web application server for production"
}
```

### GET /api/servers/{id}
Get server details including latest health metrics.

### PUT /api/servers/{id}
Update server details. **Roles: OPERATOR, ADMIN**

### DELETE /api/servers/{id}
Delete a server. **Roles: ADMIN only**

### GET /api/servers/{id}/metrics
Get health metrics for a server.

**Query Params:** `from` (ISO datetime), `to` (ISO datetime), `interval` (HOURLY/DAILY)

**Response (200):**
```json
{
  "success": true,
  "data": [
    {
      "timestamp": "2026-03-30T10:00:00",
      "cpuPercent": 45.2,
      "memPercent": 67.8,
      "diskPercent": 52.1,
      "networkInMbps": 120.5,
      "networkOutMbps": 85.3
    }
  ]
}
```

### POST /api/servers/{id}/metrics
Submit health metric data point (used by agents or simulated data).

### GET /api/servers/{id}/installations
Get installation/change history for a server.

### POST /api/servers/{id}/installations
Log a new installation/change event. **Roles: OPERATOR, ADMIN**

**Request:**
```json
{
  "changeType": "PATCH",
  "description": "Applied security patch KB2026-0330",
  "performedBy": "Harvey Xia",
  "approvalReference": "CHG-2026-0452"
}
```

### GET /api/servers/{id}/spec
Generate system specification document (returns PDF).

**Response:** `Content-Type: application/pdf`

### POST /api/servers/import
Bulk import servers from CSV. **Roles: ADMIN**

**Request:** `Content-Type: multipart/form-data`, field: `file`

---

## 2. Virtualization Module — `/api/virtualization`

### GET /api/virtualization/hypervisors
List all hypervisors.

### POST /api/virtualization/hypervisors
Register a hypervisor. **Roles: OPERATOR, ADMIN**

**Request:**
```json
{
  "hostname": "esxi-host-01",
  "type": "VMWARE",
  "version": "8.0 Update 2",
  "totalCpuCores": 64,
  "totalRamGb": 512,
  "totalStorageGb": 10000,
  "cluster": "Production-Cluster-A"
}
```

### GET /api/virtualization/hypervisors/{id}
Get hypervisor details with VM list.

### GET /api/virtualization/vms
List all VMs (paginated).

**Query Params:** `page`, `size`, `hostId` (optional), `status` (optional: RUNNING/STOPPED/SUSPENDED)

### POST /api/virtualization/vms
Create a VM record. **Roles: OPERATOR, ADMIN**

**Request:**
```json
{
  "name": "app-server-01",
  "guestOs": "CentOS 8",
  "vcpu": 4,
  "vramGb": 16,
  "vdiskGb": 200,
  "hypervisorId": "uuid",
  "status": "RUNNING"
}
```

### GET /api/virtualization/vms/{id}/snapshots
List snapshots for a VM.

### POST /api/virtualization/vms/{id}/snapshots
Record a snapshot.

### GET /api/virtualization/capacity
Get capacity planning report data.

**Response (200):**
```json
{
  "success": true,
  "data": {
    "totalHosts": 10,
    "totalCpuCores": 640,
    "allocatedCpuCores": 480,
    "cpuAllocationRatio": 0.75,
    "totalRamGb": 5120,
    "allocatedRamGb": 3840,
    "ramAllocationRatio": 0.75,
    "overcommittedHosts": [
      {
        "hostId": "uuid",
        "hostname": "esxi-host-03",
        "cpuRatio": 1.2,
        "ramRatio": 0.95
      }
    ],
    "projectedExhaustionDate": "2026-09-15"
  }
}
```

---

## 3. Backup & DR Module — `/api/backup`, `/api/dr`

### GET /api/backup/jobs
List backup jobs.

### POST /api/backup/jobs
Create a backup job. **Roles: OPERATOR, ADMIN**

**Request:**
```json
{
  "name": "DB-Daily-Full",
  "type": "FULL",
  "sourceSystem": "PostgreSQL Production",
  "targetLocation": "NAS-Backup-01:/backups/db",
  "scheduleCron": "0 2 * * *",
  "retentionDays": 30,
  "enabled": true
}
```

### GET /api/backup/jobs/{id}/executions
List execution history for a backup job.

### POST /api/backup/jobs/{id}/executions
Log a backup execution result.

**Request:**
```json
{
  "startTime": "2026-03-30T02:00:00",
  "endTime": "2026-03-30T02:45:00",
  "status": "SUCCESS",
  "dataSizeGb": 125.4,
  "notes": "Full backup completed successfully"
}
```

### GET /api/backup/trend
Get backup success/failure trend data.

**Query Params:** `days` (default 30)

**Response (200):**
```json
{
  "success": true,
  "data": {
    "period": "30 days",
    "totalExecutions": 450,
    "successCount": 435,
    "failedCount": 12,
    "partialCount": 3,
    "successRate": 96.7,
    "dailyTrend": [
      { "date": "2026-03-01", "success": 15, "failed": 0, "partial": 0 },
      { "date": "2026-03-02", "success": 14, "failed": 1, "partial": 0 }
    ]
  }
}
```

### GET /api/dr/plans
List all DR plans.

### POST /api/dr/plans
Create a DR plan. **Roles: OPERATOR, ADMIN**

**Request:**
```json
{
  "systemName": "Core Banking Application",
  "rtoMinutes": 60,
  "rpoMinutes": 15,
  "recoverySteps": [
    "1. Activate DR site network routing",
    "2. Restore database from latest backup",
    "3. Start application servers in sequence",
    "4. Verify data integrity with checksum",
    "5. Redirect traffic to DR site",
    "6. Notify stakeholders"
  ],
  "dependencies": ["PostgreSQL DR replica", "Load Balancer failover", "DNS update"],
  "responsibleTeam": "Infrastructure Operations",
  "contactEmail": "infra-ops@company.com"
}
```

### GET /api/dr/plans/{id}
Get DR plan details.

### GET /api/dr/plans/{id}/pdf
Export DR plan as PDF.

### GET /api/dr/drills
List DR drill logs.

### POST /api/dr/drills
Log a DR drill. **Roles: OPERATOR, ADMIN**

**Request:**
```json
{
  "planId": "uuid",
  "drillDate": "2026-03-15",
  "scope": "Full failover simulation for Core Banking",
  "participants": ["Harvey Xia", "John Smith", "Lisa Wong"],
  "stepsExecuted": "All 6 steps completed",
  "issuesFound": "Step 3 took 15 minutes longer than expected due to slow VM boot",
  "resolution": "Pre-warmed standby VMs added to DR site",
  "result": "PASS"
}
```

### GET /api/dr/compliance
Get DR drill compliance summary.

**Response (200):**
```json
{
  "success": true,
  "data": {
    "totalSystems": 25,
    "systemsWithDrillInLast12Months": 20,
    "complianceRate": 80.0,
    "overdueSystems": [
      { "systemName": "Legacy ERP", "lastDrillDate": "2025-01-20", "daysSinceDrill": 434 }
    ]
  }
}
```

---

## 4. Migration Module — `/api/migrations`

### GET /api/migrations
List migration projects.

### POST /api/migrations
Create a migration project. **Roles: OPERATOR, ADMIN**

**Request:**
```json
{
  "name": "Legacy DB to Cloud PostgreSQL",
  "description": "Migrate on-prem Oracle database to AWS RDS PostgreSQL",
  "sourceSystem": "Oracle 12c (on-prem DC1)",
  "targetSystem": "AWS RDS PostgreSQL 15",
  "migrationType": "DATABASE",
  "plannedStartDate": "2026-04-01",
  "plannedEndDate": "2026-04-30"
}
```

### GET /api/migrations/{id}
Get migration project with task list and overall progress.

### GET /api/migrations/{id}/tasks
List tasks for a migration project.

### POST /api/migrations/{id}/tasks
Create a migration task.

**Request:**
```json
{
  "datasetName": "customer_accounts",
  "sourceTable": "CUST_ACCT (Oracle)",
  "targetTable": "customer_accounts (PostgreSQL)",
  "expectedRowCount": 1500000,
  "transformationRules": "Convert DATE to TIMESTAMP, uppercase COUNTRY_CODE",
  "dependsOnTaskIds": []
}
```

### PUT /api/migrations/{projectId}/tasks/{taskId}/status
Update task status.

**Request:**
```json
{
  "status": "COMPLETED",
  "actualRowCount": 1500000,
  "notes": "Migration completed. All rows transferred."
}
```

### POST /api/migrations/{projectId}/tasks/{taskId}/validate
Run data validation for a completed task.

**Request:**
```json
{
  "validationType": "ROW_COUNT",
  "sourceValue": "1500000",
  "targetValue": "1500000"
}
```

**Response (200):**
```json
{
  "success": true,
  "data": {
    "validationType": "ROW_COUNT",
    "sourceValue": "1500000",
    "targetValue": "1500000",
    "passed": true,
    "executedAt": "2026-03-30T14:00:00"
  }
}
```

### GET /api/migrations/{id}/progress
Get migration progress summary.

---

## 5. Report Module — `/api/reports`

### GET /api/reports/templates
List available report templates.

### POST /api/reports/generate
Generate a report on demand.

**Request:**
```json
{
  "templateId": "uuid",
  "format": "PDF",
  "parameters": {
    "dateFrom": "2026-03-01",
    "dateTo": "2026-03-31",
    "environment": "PRODUCTION"
  }
}
```

**Response:** Returns binary file with appropriate Content-Type header.

### GET /api/reports/archive
List generated report history.

### GET /api/reports/archive/{id}/download
Download a previously generated report.

### GET /api/reports/schedules
List report schedules.

### POST /api/reports/schedules
Create a report schedule. **Roles: ADMIN**

**Request:**
```json
{
  "templateId": "uuid",
  "cronExpression": "0 8 * * 1",
  "format": "PDF",
  "recipientsEmail": "team@company.com",
  "parameters": {
    "environment": "PRODUCTION"
  },
  "enabled": true
}
```

---

## 6. Testing Module — `/api/tests`

### GET /api/tests/cases
List test cases.

### POST /api/tests/cases
Create a test case. **Roles: OPERATOR, ADMIN**

**Request:**
```json
{
  "name": "Web Server Connectivity Check",
  "category": "CONNECTIVITY",
  "serverId": "uuid",
  "steps": "1. Ping server IP\n2. Check TCP port 80\n3. Check TCP port 443\n4. Verify HTTP 200 response",
  "expectedResult": "All checks return success"
}
```

### POST /api/tests/cases/{id}/execute
Execute a test case (automated health check).

### GET /api/tests/executions
List test execution history.

### GET /api/tests/summary
Get test execution summary dashboard data.

---

## 7. Dashboard — `/api/dashboard`

### GET /api/dashboard/summary
Get aggregated dashboard data.

**Response (200):**
```json
{
  "success": true,
  "data": {
    "serverCount": { "total": 150, "online": 142, "offline": 5, "maintenance": 3 },
    "vmCount": { "total": 420, "running": 380, "stopped": 35, "suspended": 5 },
    "backupStatus24h": { "total": 45, "success": 43, "failed": 2, "successRate": 95.6 },
    "activeMigrations": 3,
    "drComplianceRate": 80.0,
    "recentAlerts": [
      { "type": "BACKUP_FAILED", "message": "Backup job DB-Daily-Full failed", "timestamp": "2026-03-30T02:45:00", "severity": "HIGH" },
      { "type": "DISK_WARNING", "message": "web-prod-01 disk usage at 85%", "timestamp": "2026-03-30T09:00:00", "severity": "MEDIUM" }
    ],
    "serverHealthDistribution": {
      "healthy": 130,
      "warning": 15,
      "critical": 5
    }
  }
}
```

---

## Standard Response Format

All API responses follow this structure:

**Success:**
```json
{
  "success": true,
  "data": { ... },
  "message": "Operation completed successfully"
}
```

**Error:**
```json
{
  "success": false,
  "error": {
    "code": "RESOURCE_NOT_FOUND",
    "message": "Server with id 'uuid' not found"
  }
}
```

**Validation Error (400):**
```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Validation failed",
    "details": [
      { "field": "hostname", "message": "must not be blank" },
      { "field": "cpuCores", "message": "must be greater than 0" }
    ]
  }
}
```
