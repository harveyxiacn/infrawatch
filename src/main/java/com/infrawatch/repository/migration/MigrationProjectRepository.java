package com.infrawatch.repository.migration;

import com.infrawatch.model.migration.MigrationProject;
import com.infrawatch.model.migration.enums.MigrationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MigrationProjectRepository extends JpaRepository<MigrationProject, UUID> {

    Page<MigrationProject> findByStatus(MigrationStatus status, Pageable pageable);

    @Query("SELECT COUNT(p) FROM MigrationProject p WHERE p.status = :status")
    long countByStatus(MigrationStatus status);
}
