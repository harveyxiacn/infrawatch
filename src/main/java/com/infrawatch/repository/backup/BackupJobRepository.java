package com.infrawatch.repository.backup;

import com.infrawatch.model.backup.BackupJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BackupJobRepository extends JpaRepository<BackupJob, UUID> {

    List<BackupJob> findByEnabled(boolean enabled);
}
