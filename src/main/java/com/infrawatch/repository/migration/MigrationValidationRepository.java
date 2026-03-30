package com.infrawatch.repository.migration;

import com.infrawatch.model.migration.MigrationValidation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MigrationValidationRepository extends JpaRepository<MigrationValidation, UUID> {

    List<MigrationValidation> findByTaskId(UUID taskId);
}
