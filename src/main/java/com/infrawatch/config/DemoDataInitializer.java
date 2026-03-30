package com.infrawatch.config;

import com.infrawatch.model.auth.User;
import com.infrawatch.model.backup.BackupExecution;
import com.infrawatch.model.backup.BackupJob;
import com.infrawatch.model.backup.DRPlan;
import com.infrawatch.model.backup.DrillLog;
import com.infrawatch.model.backup.enums.BackupType;
import com.infrawatch.model.backup.enums.ExecutionStatus;
import com.infrawatch.model.migration.MigrationProject;
import com.infrawatch.model.migration.MigrationTask;
import com.infrawatch.model.migration.MigrationValidation;
import com.infrawatch.model.migration.enums.MigrationStatus;
import com.infrawatch.model.migration.enums.ValidationType;
import com.infrawatch.model.report.ReportTemplate;
import com.infrawatch.model.server.HealthMetric;
import com.infrawatch.model.server.Installation;
import com.infrawatch.model.server.Server;
import com.infrawatch.model.server.enums.ChangeType;
import com.infrawatch.model.server.enums.Environment;
import com.infrawatch.model.server.enums.ServerStatus;
import com.infrawatch.model.testing.TestCase;
import com.infrawatch.model.testing.TestExecution;
import com.infrawatch.model.virtualization.Hypervisor;
import com.infrawatch.model.virtualization.Snapshot;
import com.infrawatch.model.virtualization.VirtualMachine;
import com.infrawatch.model.virtualization.enums.HypervisorType;
import com.infrawatch.model.virtualization.enums.VmStatus;
import com.infrawatch.repository.auth.UserRepository;
import com.infrawatch.repository.backup.BackupExecutionRepository;
import com.infrawatch.repository.backup.BackupJobRepository;
import com.infrawatch.repository.backup.DRPlanRepository;
import com.infrawatch.repository.backup.DrillLogRepository;
import com.infrawatch.repository.migration.MigrationProjectRepository;
import com.infrawatch.repository.migration.MigrationTaskRepository;
import com.infrawatch.repository.migration.MigrationValidationRepository;
import com.infrawatch.repository.report.ReportTemplateRepository;
import com.infrawatch.repository.server.HealthMetricRepository;
import com.infrawatch.repository.server.InstallationRepository;
import com.infrawatch.repository.server.ServerRepository;
import com.infrawatch.repository.testing.TestCaseRepository;
import com.infrawatch.repository.testing.TestExecutionRepository;
import com.infrawatch.repository.virtualization.HypervisorRepository;
import com.infrawatch.repository.virtualization.SnapshotRepository;
import com.infrawatch.repository.virtualization.VirtualMachineRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Component
@Profile("dev")
@Order(2)
@RequiredArgsConstructor
@Slf4j
public class DemoDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ServerRepository serverRepository;
    private final HealthMetricRepository healthMetricRepository;
    private final InstallationRepository installationRepository;
    private final HypervisorRepository hypervisorRepository;
    private final VirtualMachineRepository virtualMachineRepository;
    private final SnapshotRepository snapshotRepository;
    private final BackupJobRepository backupJobRepository;
    private final BackupExecutionRepository backupExecutionRepository;
    private final DRPlanRepository drPlanRepository;
    private final DrillLogRepository drillLogRepository;
    private final MigrationProjectRepository migrationProjectRepository;
    private final MigrationTaskRepository migrationTaskRepository;
    private final MigrationValidationRepository migrationValidationRepository;
    private final ReportTemplateRepository reportTemplateRepository;
    private final TestCaseRepository testCaseRepository;
    private final TestExecutionRepository testExecutionRepository;

    private final Random random = new Random(42);

    @Override
    public void run(String... args) {
        if (serverRepository.count() > 0) {
            log.info("Demo data already exists, skipping initialization");
            return;
        }

        log.info("Initializing demo data...");

        createUsers();
        List<Server> servers = createServers();
        createHealthMetrics(servers);
        createInstallations(servers);
        List<Hypervisor> hypervisors = createHypervisors();
        List<VirtualMachine> vms = createVirtualMachines(hypervisors);
        createSnapshots(vms);
        List<BackupJob> backupJobs = createBackupJobs();
        createBackupExecutions(backupJobs);
        List<DRPlan> drPlans = createDRPlans();
        createDrillLogs(drPlans);
        createMigrationData();
        createReportTemplates();
        createTestData(servers);

        log.info("Demo data initialization complete!");
    }

    private void createUsers() {
        if (!userRepository.existsByUsername("operator")) {
            userRepository.save(User.builder()
                    .username("operator").passwordHash(passwordEncoder.encode("operator123"))
                    .email("operator@infrawatch.local").role("OPERATOR").enabled(true).build());
        }
        if (!userRepository.existsByUsername("viewer")) {
            userRepository.save(User.builder()
                    .username("viewer").passwordHash(passwordEncoder.encode("viewer123"))
                    .email("viewer@infrawatch.local").role("VIEWER").enabled(true).build());
        }
        log.info("  Created 3 users (admin/operator/viewer)");
    }

    private List<Server> createServers() {
        List<Server> servers = List.of(
            server("web-prod-01", "10.0.1.10", "Ubuntu 22.04 LTS", 8, 16, 200, "DC1-Rack-A1-U10", ServerStatus.ONLINE, Environment.PRODUCTION),
            server("web-prod-02", "10.0.1.11", "Ubuntu 22.04 LTS", 8, 16, 200, "DC1-Rack-A1-U12", ServerStatus.ONLINE, Environment.PRODUCTION),
            server("app-prod-01", "10.0.1.20", "RHEL 9.3", 16, 32, 500, "DC1-Rack-B2-U05", ServerStatus.ONLINE, Environment.PRODUCTION),
            server("app-prod-02", "10.0.1.21", "RHEL 9.3", 16, 32, 500, "DC1-Rack-B2-U07", ServerStatus.ONLINE, Environment.PRODUCTION),
            server("db-prod-01", "10.0.1.30", "RHEL 9.3", 32, 128, 2000, "DC1-Rack-C3-U01", ServerStatus.ONLINE, Environment.PRODUCTION),
            server("db-prod-02", "10.0.1.31", "RHEL 9.3", 32, 128, 2000, "DC1-Rack-C3-U03", ServerStatus.ONLINE, Environment.PRODUCTION),
            server("cache-prod-01", "10.0.1.40", "Ubuntu 22.04 LTS", 8, 64, 100, "DC1-Rack-A2-U08", ServerStatus.ONLINE, Environment.PRODUCTION),
            server("mq-prod-01", "10.0.1.50", "RHEL 9.3", 8, 32, 500, "DC1-Rack-B3-U10", ServerStatus.ONLINE, Environment.PRODUCTION),
            server("monitor-01", "10.0.1.60", "Ubuntu 22.04 LTS", 4, 8, 200, "DC1-Rack-A3-U15", ServerStatus.ONLINE, Environment.PRODUCTION),
            server("backup-srv-01", "10.0.1.70", "Windows Server 2022", 8, 16, 8000, "DC1-Rack-D1-U01", ServerStatus.ONLINE, Environment.PRODUCTION),
            server("web-uat-01", "10.0.2.10", "Ubuntu 22.04 LTS", 4, 8, 100, "DC2-Rack-A1-U05", ServerStatus.ONLINE, Environment.UAT),
            server("app-uat-01", "10.0.2.20", "RHEL 9.3", 8, 16, 200, "DC2-Rack-A1-U07", ServerStatus.ONLINE, Environment.UAT),
            server("db-uat-01", "10.0.2.30", "RHEL 9.3", 8, 32, 500, "DC2-Rack-A2-U01", ServerStatus.ONLINE, Environment.UAT),
            server("web-dev-01", "10.0.3.10", "Ubuntu 22.04 LTS", 2, 4, 50, "DC2-Rack-B1-U10", ServerStatus.ONLINE, Environment.DEV),
            server("app-dev-01", "10.0.3.20", "RHEL 9.3", 4, 8, 100, "DC2-Rack-B1-U12", ServerStatus.ONLINE, Environment.DEV),
            server("db-dev-01", "10.0.3.30", "RHEL 9.3", 4, 16, 200, "DC2-Rack-B2-U01", ServerStatus.ONLINE, Environment.DEV),
            server("legacy-app-01", "10.0.1.80", "Windows Server 2016", 4, 8, 200, "DC1-Rack-D2-U05", ServerStatus.MAINTENANCE, Environment.PRODUCTION),
            server("test-runner-01", "10.0.3.40", "Ubuntu 22.04 LTS", 4, 8, 100, "DC2-Rack-B3-U01", ServerStatus.ONLINE, Environment.DEV),
            server("dr-web-01", "10.0.4.10", "Ubuntu 22.04 LTS", 8, 16, 200, "DC3-Rack-A1-U01", ServerStatus.OFFLINE, Environment.DR),
            server("dr-db-01", "10.0.4.30", "RHEL 9.3", 32, 128, 2000, "DC3-Rack-A2-U01", ServerStatus.OFFLINE, Environment.DR)
        );
        List<Server> saved = serverRepository.saveAll(servers);
        log.info("  Created {} servers", saved.size());
        return saved;
    }

    private Server server(String hostname, String ip, String os, int cpu, int ram, int disk,
                          String location, ServerStatus status, Environment env) {
        return Server.builder()
                .hostname(hostname).ipAddress(ip).os(os)
                .cpuCores(cpu).ramGb(ram).diskGb(disk)
                .location(location).status(status).environment(env)
                .build();
    }

    private void createHealthMetrics(List<Server> servers) {
        int count = 0;
        for (Server server : servers) {
            if (server.getStatus() == ServerStatus.OFFLINE) continue;
            // 7 days of hourly metrics
            for (int day = 6; day >= 0; day--) {
                for (int hour = 0; hour < 24; hour += 2) { // every 2 hours to keep data manageable
                    LocalDateTime ts = LocalDateTime.now().minusDays(day).withHour(hour).withMinute(0);
                    double baseCpu = server.getHostname().contains("db") ? 45 : 25;
                    double baseMem = server.getHostname().contains("db") ? 70 : 40;
                    double baseDisk = server.getHostname().contains("db") ? 65 : 35;

                    // Add business hour spikes
                    if (hour >= 9 && hour <= 17) {
                        baseCpu += 20;
                        baseMem += 10;
                    }

                    healthMetricRepository.save(HealthMetric.builder()
                            .server(server)
                            .timestamp(ts)
                            .cpuPercent(bd(baseCpu + random.nextDouble() * 15))
                            .memPercent(bd(baseMem + random.nextDouble() * 10))
                            .diskPercent(bd(baseDisk + random.nextDouble() * 3))
                            .networkInMbps(bd(random.nextDouble() * 100))
                            .networkOutMbps(bd(random.nextDouble() * 50))
                            .uptimeSeconds((long) (day * 86400 + hour * 3600 + random.nextInt(3600) + 2592000))
                            .build());
                    count++;
                }
            }
        }
        log.info("  Created {} health metrics", count);
    }

    private void createInstallations(List<Server> servers) {
        List.of(
            install(servers.get(0), ChangeType.PATCH, "Security patch KB5034441 applied", "Harvey Xia", "CHG-2026-001"),
            install(servers.get(0), ChangeType.CONFIG, "Updated nginx config: increased worker_connections to 2048", "Harvey Xia", "CHG-2026-002"),
            install(servers.get(2), ChangeType.UPGRADE, "Upgraded Java from 17.0.9 to 17.0.10", "Harvey Xia", "CHG-2026-003"),
            install(servers.get(4), ChangeType.PATCH, "PostgreSQL 15.5 -> 15.6 security update", "Harvey Xia", "CHG-2026-004"),
            install(servers.get(4), ChangeType.CONFIG, "Increased shared_buffers to 32GB, work_mem to 256MB", "Harvey Xia", "CHG-2026-005"),
            install(servers.get(6), ChangeType.INSTALL, "Installed Redis 7.2 with Sentinel configuration", "Harvey Xia", "CHG-2026-006"),
            install(servers.get(7), ChangeType.UPGRADE, "RabbitMQ upgraded from 3.12 to 3.13", "Harvey Xia", "CHG-2026-007"),
            install(servers.get(9), ChangeType.INSTALL, "Installed Veeam Backup Agent 6.0", "Harvey Xia", "CHG-2026-008"),
            install(servers.get(10), ChangeType.PATCH, "Applied March 2026 OS security patches", "Harvey Xia", "CHG-2026-009"),
            install(servers.get(16), ChangeType.CONFIG, "Entered maintenance mode for legacy .NET migration assessment", "Harvey Xia", "CHG-2026-010")
        ).forEach(installationRepository::save);
        log.info("  Created 10 installation records");
    }

    private Installation install(Server server, ChangeType type, String desc, String by, String ref) {
        return Installation.builder()
                .server(server).changeType(type).description(desc)
                .performedBy(by).approvalReference(ref).build();
    }

    private List<Hypervisor> createHypervisors() {
        List<Hypervisor> hypervisors = hypervisorRepository.saveAll(List.of(
            Hypervisor.builder().hostname("esxi-host-01.dc1.local").type(HypervisorType.VMWARE)
                    .version("8.0 Update 2").totalCpuCores(64).totalRamGb(512).totalStorageGb(10000)
                    .cluster("PROD-Cluster-01").status("ACTIVE").build(),
            Hypervisor.builder().hostname("esxi-host-02.dc1.local").type(HypervisorType.VMWARE)
                    .version("8.0 Update 2").totalCpuCores(64).totalRamGb(512).totalStorageGb(10000)
                    .cluster("PROD-Cluster-01").status("ACTIVE").build(),
            Hypervisor.builder().hostname("kvm-host-01.dc2.local").type(HypervisorType.KVM)
                    .version("QEMU 8.2").totalCpuCores(32).totalRamGb(256).totalStorageGb(5000)
                    .cluster("DEV-Cluster").status("ACTIVE").build(),
            Hypervisor.builder().hostname("hyperv-host-01.dc1.local").type(HypervisorType.HYPERV)
                    .version("Windows Server 2022").totalCpuCores(48).totalRamGb(384).totalStorageGb(8000)
                    .cluster("LEGACY-Cluster").status("ACTIVE").build()
        ));
        log.info("  Created {} hypervisors", hypervisors.size());
        return hypervisors;
    }

    private List<VirtualMachine> createVirtualMachines(List<Hypervisor> hypervisors) {
        Hypervisor esxi1 = hypervisors.get(0), esxi2 = hypervisors.get(1);
        Hypervisor kvm = hypervisors.get(2), hyperv = hypervisors.get(3);

        List<VirtualMachine> vms = virtualMachineRepository.saveAll(List.of(
            vm("vm-web-prod-01", "Ubuntu 22.04", 4, 8, 80, esxi1, VmStatus.RUNNING),
            vm("vm-web-prod-02", "Ubuntu 22.04", 4, 8, 80, esxi1, VmStatus.RUNNING),
            vm("vm-api-prod-01", "RHEL 9", 8, 16, 200, esxi1, VmStatus.RUNNING),
            vm("vm-api-prod-02", "RHEL 9", 8, 16, 200, esxi1, VmStatus.RUNNING),
            vm("vm-worker-prod-01", "Ubuntu 22.04", 4, 8, 100, esxi1, VmStatus.RUNNING),
            vm("vm-db-replica-01", "RHEL 9", 16, 64, 500, esxi2, VmStatus.RUNNING),
            vm("vm-elastic-01", "Ubuntu 22.04", 8, 32, 500, esxi2, VmStatus.RUNNING),
            vm("vm-elastic-02", "Ubuntu 22.04", 8, 32, 500, esxi2, VmStatus.RUNNING),
            vm("vm-grafana-01", "Ubuntu 22.04", 2, 4, 50, esxi2, VmStatus.RUNNING),
            vm("vm-jenkins-01", "Ubuntu 22.04", 4, 8, 100, esxi2, VmStatus.RUNNING),
            vm("vm-dev-web-01", "Ubuntu 22.04", 2, 4, 40, kvm, VmStatus.RUNNING),
            vm("vm-dev-api-01", "RHEL 9", 4, 8, 80, kvm, VmStatus.RUNNING),
            vm("vm-dev-db-01", "RHEL 9", 4, 16, 200, kvm, VmStatus.RUNNING),
            vm("vm-staging-01", "Ubuntu 22.04", 4, 8, 100, kvm, VmStatus.RUNNING),
            vm("vm-test-runner-01", "Ubuntu 22.04", 2, 4, 50, kvm, VmStatus.STOPPED),
            vm("vm-legacy-app-01", "Windows Server 2016", 4, 8, 100, hyperv, VmStatus.RUNNING),
            vm("vm-legacy-app-02", "Windows Server 2016", 4, 8, 100, hyperv, VmStatus.RUNNING),
            vm("vm-legacy-db-01", "Windows Server 2019", 8, 32, 500, hyperv, VmStatus.RUNNING),
            vm("vm-sharepoint-01", "Windows Server 2022", 4, 16, 200, hyperv, VmStatus.RUNNING),
            vm("vm-decommission-01", "Windows Server 2012 R2", 2, 4, 80, hyperv, VmStatus.SUSPENDED)
        ));
        log.info("  Created {} virtual machines", vms.size());
        return vms;
    }

    private VirtualMachine vm(String name, String os, int vcpu, int vram, int vdisk,
                              Hypervisor host, VmStatus status) {
        return VirtualMachine.builder()
                .name(name).guestOs(os).vcpu(vcpu).vramGb(vram).vdiskGb(vdisk)
                .hypervisor(host).status(status).build();
    }

    private void createSnapshots(List<VirtualMachine> vms) {
        snapshotRepository.saveAll(List.of(
            snapshot(vms.get(0), "Pre-patch snapshot", 2.5, "Before nginx update"),
            snapshot(vms.get(2), "Pre-deploy v3.2.1", 5.8, "Before API deployment"),
            snapshot(vms.get(5), "Pre-upgrade snapshot", 15.2, "Before PostgreSQL upgrade"),
            snapshot(vms.get(7), "Config backup", 3.1, "Elasticsearch config change"),
            snapshot(vms.get(15), "Legacy app backup", 8.4, "Before migration assessment"),
            // Old snapshots (for alert testing)
            Snapshot.builder().vm(vms.get(19)).name("Old decom snapshot").sizeGb(BigDecimal.valueOf(12.5))
                    .description("Forgotten snapshot").createdAt(LocalDateTime.now().minusDays(30)).build(),
            Snapshot.builder().vm(vms.get(16)).name("Legacy migration prep").sizeGb(BigDecimal.valueOf(6.3))
                    .description("Pre-migration snapshot").createdAt(LocalDateTime.now().minusDays(14)).build()
        ));
        log.info("  Created 7 snapshots (2 intentionally old for alert demo)");
    }

    private Snapshot snapshot(VirtualMachine vm, String name, double size, String desc) {
        return Snapshot.builder().vm(vm).name(name).sizeGb(BigDecimal.valueOf(size)).description(desc).build();
    }

    private List<BackupJob> createBackupJobs() {
        List<BackupJob> jobs = backupJobRepository.saveAll(List.of(
            backupJob("DB Full Backup - Production", BackupType.FULL, "db-prod-01 (PostgreSQL)", "backup-srv-01:/backups/db/full", "0 2 * * 0", 90),
            backupJob("DB Incremental - Production", BackupType.INCREMENTAL, "db-prod-01 (PostgreSQL)", "backup-srv-01:/backups/db/incr", "0 2 * * 1-6", 30),
            backupJob("App Server Backup", BackupType.FULL, "app-prod-01, app-prod-02", "backup-srv-01:/backups/app", "0 3 * * *", 30),
            backupJob("Web Server Config Backup", BackupType.FULL, "web-prod-01, web-prod-02", "backup-srv-01:/backups/web", "0 4 * * *", 14),
            backupJob("File Share Backup", BackupType.DIFFERENTIAL, "file-share-01 (SMB)", "backup-srv-01:/backups/fileshare", "0 1 * * *", 60),
            backupJob("Exchange Mailbox Backup", BackupType.FULL, "exchange-01 (O365 hybrid)", "Azure Blob Storage", "0 0 * * *", 365),
            backupJob("VM Snapshot Backup - ESXi", BackupType.FULL, "esxi-host-01 (all VMs)", "NAS-01:/vmbackups", "0 5 * * 6", 30),
            backupJob("DR Replication - Database", BackupType.INCREMENTAL, "db-prod-01", "dr-db-01 (async repl)", "*/15 * * * *", 7)
        ));
        log.info("  Created {} backup jobs", jobs.size());
        return jobs;
    }

    private BackupJob backupJob(String name, BackupType type, String source, String target, String cron, int retention) {
        return BackupJob.builder()
                .name(name).type(type).sourceSystem(source).targetLocation(target)
                .scheduleCron(cron).retentionDays(retention).enabled(true).build();
    }

    private void createBackupExecutions(List<BackupJob> jobs) {
        int count = 0;
        for (BackupJob job : jobs) {
            for (int day = 29; day >= 0; day--) {
                LocalDateTime start = LocalDateTime.now().minusDays(day).withHour(2).withMinute(random.nextInt(30));
                int durationSec = 300 + random.nextInt(3600);
                // ~95% success rate
                ExecutionStatus status = random.nextInt(100) < 95 ? ExecutionStatus.SUCCESS :
                        (random.nextBoolean() ? ExecutionStatus.FAILED : ExecutionStatus.PARTIAL);

                backupExecutionRepository.save(BackupExecution.builder()
                        .job(job)
                        .startTime(start)
                        .endTime(start.plusSeconds(durationSec))
                        .status(status)
                        .dataSizeGb(bd(0.5 + random.nextDouble() * 50))
                        .durationSeconds(durationSec)
                        .notes(status == ExecutionStatus.FAILED ? "Error: network timeout to backup target" : null)
                        .build());
                count++;
            }
        }
        log.info("  Created {} backup executions (30 days history)", count);
    }

    private List<DRPlan> createDRPlans() {
        List<DRPlan> plans = drPlanRepository.saveAll(List.of(
            DRPlan.builder().systemName("Production Database Cluster").rtoMinutes(60).rpoMinutes(15)
                    .recoverySteps("[\"Activate DR database replica\",\"Verify data consistency\",\"Update DNS to point to DR\",\"Notify application teams\",\"Monitor for 30 minutes\"]")
                    .dependencies("[\"Network connectivity to DC3\",\"DR database replica sync\",\"DNS management access\"]")
                    .responsibleTeam("Database Team").contactEmail("dba@company.com")
                    .version(3).lastReviewDate(LocalDate.of(2026, 2, 15)).build(),
            DRPlan.builder().systemName("Web Application Tier").rtoMinutes(30).rpoMinutes(5)
                    .recoverySteps("[\"Deploy containers to DR Kubernetes cluster\",\"Switch load balancer to DR site\",\"Verify health checks pass\",\"Update CDN origin\"]")
                    .dependencies("[\"Container registry access\",\"DR Kubernetes cluster\",\"Load balancer management\"]")
                    .responsibleTeam("Platform Team").contactEmail("platform@company.com")
                    .version(2).lastReviewDate(LocalDate.of(2026, 1, 20)).build(),
            DRPlan.builder().systemName("Email & Collaboration (Exchange)").rtoMinutes(240).rpoMinutes(60)
                    .recoverySteps("[\"Activate O365 backup mailboxes\",\"Restore from Azure backup\",\"Reconfigure mail flow\",\"Test send/receive\"]")
                    .dependencies("[\"Azure Backup vault\",\"O365 admin access\",\"DNS MX record management\"]")
                    .responsibleTeam("Messaging Team").contactEmail("messaging@company.com")
                    .version(1).lastReviewDate(LocalDate.of(2025, 11, 10)).build(),
            DRPlan.builder().systemName("File Share Services").rtoMinutes(120).rpoMinutes(30)
                    .recoverySteps("[\"Mount replicated NAS at DR site\",\"Update DFS namespace\",\"Verify permissions\",\"Notify users\"]")
                    .dependencies("[\"NAS replication up-to-date\",\"AD services available\",\"DFS management access\"]")
                    .responsibleTeam("Infrastructure Team").contactEmail("infra@company.com")
                    .version(2).lastReviewDate(LocalDate.of(2026, 3, 1)).build(),
            DRPlan.builder().systemName("Monitoring & Alerting Stack").rtoMinutes(180).rpoMinutes(120)
                    .recoverySteps("[\"Deploy Grafana/Prometheus to DR\",\"Import dashboard configs\",\"Reconfigure alert targets\",\"Verify metric collection\"]")
                    .dependencies("[\"DR compute resources\",\"Config backup repository\",\"Alert channel access\"]")
                    .responsibleTeam("SRE Team").contactEmail("sre@company.com")
                    .version(1).lastReviewDate(LocalDate.of(2026, 2, 28)).build()
        ));
        log.info("  Created {} DR plans", plans.size());
        return plans;
    }

    private void createDrillLogs(List<DRPlan> plans) {
        drillLogRepository.saveAll(List.of(
            DrillLog.builder().plan(plans.get(0)).drillDate(LocalDate.of(2026, 3, 15))
                    .scope("Full failover of production database to DR site")
                    .participants("[\"Harvey Xia\",\"DBA Team Lead\",\"Network Engineer\"]")
                    .stepsExecuted("All recovery steps executed. Failover completed in 45 minutes.")
                    .issuesFound("DNS propagation took longer than expected (8 minutes vs 2 minutes planned)")
                    .resolution("Updated DNS TTL from 300s to 60s for critical records")
                    .result("PASS").createdBy("admin").build(),
            DrillLog.builder().plan(plans.get(1)).drillDate(LocalDate.of(2026, 2, 20))
                    .scope("Web tier failover to DR Kubernetes cluster")
                    .participants("[\"Harvey Xia\",\"Platform Lead\",\"QA Engineer\"]")
                    .stepsExecuted("Container deployment and LB switch completed. Health checks passed.")
                    .issuesFound("None — all steps completed within RTO")
                    .resolution("N/A")
                    .result("PASS").createdBy("admin").build(),
            DrillLog.builder().plan(plans.get(0)).drillDate(LocalDate.of(2025, 12, 10))
                    .scope("Database failover test - read-only workload")
                    .participants("[\"Harvey Xia\",\"Senior DBA\"]")
                    .stepsExecuted("Partial failover — read-only queries redirected to DR replica")
                    .issuesFound("Replication lag of 30 seconds detected under load")
                    .resolution("Increased WAL shipping frequency and network bandwidth allocation")
                    .result("PASS").createdBy("admin").build(),
            DrillLog.builder().plan(plans.get(2)).drillDate(LocalDate.of(2025, 11, 5))
                    .scope("Email restore from Azure backup - 10 mailboxes")
                    .participants("[\"Messaging Admin\",\"Harvey Xia\"]")
                    .stepsExecuted("Restored 10 test mailboxes from Azure Backup vault")
                    .issuesFound("Restore took 4.5 hours — exceeds 4h RTO target")
                    .resolution("Need to pre-provision restore targets; updated RTO to 240 minutes")
                    .result("FAIL").createdBy("admin").build()
        ));
        log.info("  Created 4 DR drill logs (3 PASS, 1 FAIL)");
    }

    private void createMigrationData() {
        // Completed migration
        MigrationProject completed = migrationProjectRepository.save(
            MigrationProject.builder()
                    .name("Legacy CRM Database Migration")
                    .description("Migrate customer data from legacy Oracle CRM to new PostgreSQL-based platform")
                    .sourceSystem("Oracle 12c (crm-legacy-db)")
                    .targetSystem("PostgreSQL 15 (crm-new-db)")
                    .migrationType("DATABASE")
                    .status(MigrationStatus.COMPLETED)
                    .plannedStartDate(LocalDate.of(2026, 1, 15))
                    .plannedEndDate(LocalDate.of(2026, 2, 28))
                    .actualStartDate(LocalDate.of(2026, 1, 15))
                    .actualEndDate(LocalDate.of(2026, 2, 25))
                    .build());

        List<MigrationTask> completedTasks = migrationTaskRepository.saveAll(List.of(
            migTask(completed, "customers", "CRM.CUSTOMERS", "public.customers", 125000L, 125000L, "COMPLETED", 1),
            migTask(completed, "orders", "CRM.ORDERS", "public.orders", 890000L, 890000L, "COMPLETED", 2),
            migTask(completed, "order_items", "CRM.ORDER_ITEMS", "public.order_items", 2340000L, 2340000L, "COMPLETED", 3),
            migTask(completed, "contacts", "CRM.CONTACTS", "public.contacts", 95000L, 95000L, "COMPLETED", 4),
            migTask(completed, "products", "CRM.PRODUCTS", "public.products", 8500L, 8500L, "COMPLETED", 5)
        ));

        for (MigrationTask task : completedTasks) {
            migrationValidationRepository.saveAll(List.of(
                MigrationValidation.builder().task(task).validationType(ValidationType.ROW_COUNT)
                        .sourceValue(String.valueOf(task.getExpectedRowCount()))
                        .targetValue(String.valueOf(task.getActualRowCount()))
                        .passed(true).notes("Row counts match").executedAt(LocalDateTime.now()).build(),
                MigrationValidation.builder().task(task).validationType(ValidationType.CHECKSUM)
                        .sourceValue("SHA256:a1b2c3...").targetValue("SHA256:a1b2c3...")
                        .passed(true).notes("Checksums verified").executedAt(LocalDateTime.now()).build()
            ));
        }

        // In-progress migration
        MigrationProject inProgress = migrationProjectRepository.save(
            MigrationProject.builder()
                    .name("HR System File Migration")
                    .description("Migrate employee documents and payroll files from on-prem file server to SharePoint Online")
                    .sourceSystem("file-share-01 (SMB)")
                    .targetSystem("SharePoint Online (HR Site)")
                    .migrationType("FILE")
                    .status(MigrationStatus.IN_PROGRESS)
                    .plannedStartDate(LocalDate.of(2026, 3, 1))
                    .plannedEndDate(LocalDate.of(2026, 4, 15))
                    .actualStartDate(LocalDate.of(2026, 3, 3))
                    .build());

        migrationTaskRepository.saveAll(List.of(
            migTask(inProgress, "Employee Records", "/hr/employees/", "HR/Employee Records/", 45000L, 45000L, "COMPLETED", 1),
            migTask(inProgress, "Payroll Documents", "/hr/payroll/", "HR/Payroll/", 120000L, 85000L, "IN_PROGRESS", 2),
            migTask(inProgress, "Benefits Files", "/hr/benefits/", "HR/Benefits/", 30000L, null, "PENDING", 3),
            migTask(inProgress, "Training Certificates", "/hr/training/", "HR/Training/", 15000L, null, "PENDING", 4)
        ));

        log.info("  Created 2 migration projects (1 completed, 1 in-progress) with tasks and validations");
    }

    private MigrationTask migTask(MigrationProject project, String name, String source, String target,
                                   Long expected, Long actual, String status, int order) {
        return MigrationTask.builder()
                .project(project).datasetName(name).sourceTable(source).targetTable(target)
                .expectedRowCount(expected).actualRowCount(actual).status(status).sortOrder(order)
                .build();
    }

    private void createReportTemplates() {
        reportTemplateRepository.saveAll(List.of(
            ReportTemplate.builder().name("Daily Server Health Summary").type("SERVER_HEALTH")
                    .jrxmlPath("reports/server_health.jrxml")
                    .description("Daily overview of all server health metrics including CPU, memory, and disk usage").build(),
            ReportTemplate.builder().name("Weekly VM Capacity Report").type("VM_CAPACITY")
                    .jrxmlPath("reports/vm_capacity.jrxml")
                    .description("Weekly virtualization resource utilization and capacity planning analysis").build(),
            ReportTemplate.builder().name("Monthly Backup Compliance").type("BACKUP_COMPLIANCE")
                    .jrxmlPath("reports/backup_compliance.jrxml")
                    .description("Monthly backup job success rates and compliance status across all systems").build(),
            ReportTemplate.builder().name("Data Migration Progress").type("MIGRATION_PROGRESS")
                    .jrxmlPath("reports/migration_progress.jrxml")
                    .description("Current status of all active data migration projects with validation results").build(),
            ReportTemplate.builder().name("DR Readiness Report").type("DR_READINESS")
                    .jrxmlPath("reports/dr_readiness.jrxml")
                    .description("DR plan review status, drill results, and RTO/RPO compliance summary").build(),
            ReportTemplate.builder().name("System Inventory Report").type("SYSTEM_INVENTORY")
                    .jrxmlPath("reports/system_inventory.jrxml")
                    .description("Complete inventory of all servers, VMs, and infrastructure components").build()
        ));
        log.info("  Created 6 report templates");
    }

    private void createTestData(List<Server> servers) {
        List<TestCase> testCases = testCaseRepository.saveAll(List.of(
            TestCase.builder().name("Ping web-prod-01").category("CONNECTIVITY").server(servers.get(0))
                    .steps("1. Send ICMP ping to 10.0.1.10\n2. Verify response within 100ms")
                    .expectedResult("Reply from 10.0.1.10: time<100ms").enabled(true).build(),
            TestCase.builder().name("Ping web-prod-02").category("CONNECTIVITY").server(servers.get(1))
                    .steps("1. Send ICMP ping to 10.0.1.11\n2. Verify response within 100ms")
                    .expectedResult("Reply from 10.0.1.11: time<100ms").enabled(true).build(),
            TestCase.builder().name("TCP 5432 db-prod-01").category("CONNECTIVITY").server(servers.get(4))
                    .steps("1. TCP connect to 10.0.1.30:5432\n2. Verify PostgreSQL banner")
                    .expectedResult("Connection established, PostgreSQL banner received").enabled(true).build(),
            TestCase.builder().name("Disk space check - db-prod-01").category("PERFORMANCE").server(servers.get(4))
                    .steps("1. Check /data partition usage\n2. Alert if > 80%")
                    .expectedResult("Disk usage below 80% threshold").enabled(true).build(),
            TestCase.builder().name("HTTP 200 check - web-prod-01").category("CONNECTIVITY").server(servers.get(0))
                    .steps("1. GET http://10.0.1.10:80/health\n2. Verify HTTP 200 response")
                    .expectedResult("HTTP 200 OK with body: {\"status\":\"UP\"}").enabled(true).build(),
            TestCase.builder().name("SSL certificate expiry check").category("SECURITY").server(servers.get(0))
                    .steps("1. Connect to 10.0.1.10:443\n2. Check SSL cert expiry date\n3. Alert if < 30 days")
                    .expectedResult("Certificate valid for > 30 days").enabled(true).build(),
            TestCase.builder().name("Backup restore verification").category("BACKUP_RESTORE").server(servers.get(9))
                    .steps("1. Restore latest backup to test DB\n2. Run integrity checks\n3. Verify row counts")
                    .expectedResult("Restore successful, all integrity checks pass").enabled(true).build(),
            TestCase.builder().name("Memory usage check - app-prod-01").category("PERFORMANCE").server(servers.get(2))
                    .steps("1. Check memory usage via API\n2. Alert if > 90%")
                    .expectedResult("Memory usage below 90% threshold").enabled(true).build()
        ));

        for (TestCase tc : testCases) {
            for (int i = 0; i < 5; i++) {
                String result = random.nextInt(100) < 90 ? "PASS" : "FAIL";
                testExecutionRepository.save(TestExecution.builder()
                        .testCase(tc)
                        .result(result)
                        .actualOutput(result.equals("PASS") ? tc.getExpectedResult() : "Timeout after 5000ms")
                        .durationMs(50 + random.nextInt(500))
                        .executedBy("system")
                        .executedAt(LocalDateTime.now().minusDays(i).minusHours(random.nextInt(12)))
                        .build());
            }
        }
        log.info("  Created {} test cases with 40 executions", testCases.size());
    }

    private BigDecimal bd(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
    }
}
