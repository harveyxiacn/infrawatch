package com.infrawatch.service.dashboard;

import com.infrawatch.dto.response.DashboardSummary;
import com.infrawatch.model.backup.enums.ExecutionStatus;
import com.infrawatch.model.migration.enums.MigrationStatus;
import com.infrawatch.model.server.enums.ServerStatus;
import com.infrawatch.repository.backup.BackupExecutionRepository;
import com.infrawatch.repository.backup.BackupJobRepository;
import com.infrawatch.repository.migration.MigrationProjectRepository;
import com.infrawatch.repository.server.ServerRepository;
import com.infrawatch.repository.testing.TestCaseRepository;
import com.infrawatch.repository.testing.TestExecutionRepository;
import com.infrawatch.repository.virtualization.VirtualMachineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DashboardService {

    private final ServerRepository serverRepository;
    private final VirtualMachineRepository vmRepository;
    private final BackupJobRepository backupJobRepository;
    private final BackupExecutionRepository backupExecutionRepository;
    private final MigrationProjectRepository migrationProjectRepository;
    private final TestCaseRepository testCaseRepository;
    private final TestExecutionRepository testExecutionRepository;

    public DashboardSummary getSummary() {
        return DashboardSummary.builder()
                .totalServers(serverRepository.count())
                .onlineServers(serverRepository.countByStatus(ServerStatus.ONLINE))
                .offlineServers(serverRepository.countByStatus(ServerStatus.OFFLINE))
                .totalVms(vmRepository.count())
                .totalBackupJobs(backupJobRepository.count())
                .recentBackupSuccessCount(backupExecutionRepository.countByStatus(ExecutionStatus.SUCCESS))
                .recentBackupFailCount(backupExecutionRepository.countByStatus(ExecutionStatus.FAILED))
                .activeMigrations(migrationProjectRepository.countByStatus(MigrationStatus.IN_PROGRESS))
                .totalTestCases(testCaseRepository.count())
                .recentTestPass(testExecutionRepository.countByResult("PASS"))
                .recentTestFail(testExecutionRepository.countByResult("FAIL"))
                .build();
    }
}
