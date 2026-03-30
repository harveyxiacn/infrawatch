package com.infrawatch.repository.migration;

import com.infrawatch.model.migration.MigrationTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MigrationTaskRepository extends JpaRepository<MigrationTask, UUID> {

    List<MigrationTask> findByProjectIdOrderBySortOrderAsc(UUID projectId);

    long countByProjectIdAndStatus(UUID projectId, String status);
}
