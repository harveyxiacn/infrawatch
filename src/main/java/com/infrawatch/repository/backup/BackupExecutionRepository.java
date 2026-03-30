package com.infrawatch.repository.backup;

import com.infrawatch.model.backup.BackupExecution;
import com.infrawatch.model.backup.enums.ExecutionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface BackupExecutionRepository extends JpaRepository<BackupExecution, UUID> {

    Page<BackupExecution> findByJobId(UUID jobId, Pageable pageable);

    List<BackupExecution> findByStatus(ExecutionStatus status);

    List<BackupExecution> findByStartTimeBetween(LocalDateTime from, LocalDateTime to);

    long countByStatus(ExecutionStatus status);
}
