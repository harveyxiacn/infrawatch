package com.infrawatch.service.backup;

import com.infrawatch.dto.request.BackupJobCreateRequest;
import com.infrawatch.dto.response.BackupTrendData;
import com.infrawatch.exception.ResourceNotFoundException;
import com.infrawatch.model.backup.BackupExecution;
import com.infrawatch.model.backup.BackupJob;
import com.infrawatch.model.backup.enums.ExecutionStatus;
import com.infrawatch.repository.backup.BackupExecutionRepository;
import com.infrawatch.repository.backup.BackupJobRepository;
import com.infrawatch.service.auth.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class BackupService {

    private final BackupJobRepository backupJobRepository;
    private final BackupExecutionRepository backupExecutionRepository;
    private final AuditService auditService;

    public Page<BackupJob> findAllJobs(Pageable pageable) {
        return backupJobRepository.findAll(pageable);
    }

    public BackupJob findJobById(UUID id) {
        return backupJobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BackupJob", id));
    }

    public List<BackupJob> findEnabledJobs() {
        return backupJobRepository.findByEnabled(true);
    }

    public BackupJob createJob(BackupJobCreateRequest request) {
        BackupJob job = BackupJob.builder()
                .name(request.getName())
                .type(request.getType())
                .sourceSystem(request.getSourceSystem())
                .targetLocation(request.getTargetLocation())
                .scheduleCron(request.getScheduleCron())
                .retentionDays(request.getRetentionDays())
                .enabled(request.isEnabled())
                .build();

        job = backupJobRepository.save(job);
        auditService.log("CREATE", "BackupJob", job.getId(), "Created backup job: " + job.getName());
        return job;
    }

    public BackupJob updateJob(UUID id, BackupJobCreateRequest request) {
        BackupJob job = findJobById(id);

        job.setName(request.getName());
        job.setType(request.getType());
        job.setSourceSystem(request.getSourceSystem());
        job.setTargetLocation(request.getTargetLocation());
        job.setScheduleCron(request.getScheduleCron());
        job.setRetentionDays(request.getRetentionDays());
        job.setEnabled(request.isEnabled());

        job = backupJobRepository.save(job);
        auditService.log("UPDATE", "BackupJob", job.getId(), "Updated backup job: " + job.getName());
        return job;
    }

    public void deleteJob(UUID id) {
        BackupJob job = findJobById(id);
        backupJobRepository.delete(job);
        auditService.log("DELETE", "BackupJob", id, "Deleted backup job: " + job.getName());
    }

    public BackupExecution recordExecution(BackupExecution execution) {
        execution = backupExecutionRepository.save(execution);
        auditService.log("CREATE", "BackupExecution", execution.getId(),
                "Recorded backup execution for job: " + execution.getJob().getName());
        return execution;
    }

    public Page<BackupExecution> findExecutionsByJobId(UUID jobId, Pageable pageable) {
        return backupExecutionRepository.findByJobId(jobId, pageable);
    }

    public List<BackupExecution> findExecutionsByStatus(ExecutionStatus status) {
        return backupExecutionRepository.findByStatus(status);
    }

    public List<BackupTrendData> getTrendData(int days) {
        List<BackupTrendData> trend = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime from = date.atStartOfDay();
            LocalDateTime to = date.atTime(LocalTime.MAX);

            List<BackupExecution> executions = backupExecutionRepository.findByStartTimeBetween(from, to);

            long successCount = executions.stream()
                    .filter(e -> e.getStatus() == ExecutionStatus.SUCCESS)
                    .count();
            long failedCount = executions.stream()
                    .filter(e -> e.getStatus() == ExecutionStatus.FAILED)
                    .count();

            trend.add(BackupTrendData.builder()
                    .date(date)
                    .successCount(successCount)
                    .failedCount(failedCount)
                    .totalCount(executions.size())
                    .build());
        }

        return trend;
    }
}
