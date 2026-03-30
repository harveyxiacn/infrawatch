package com.infrawatch.model.migration;

import com.infrawatch.model.base.BaseEntity;
import com.infrawatch.model.migration.enums.MigrationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "migration_projects")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MigrationProject extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "source_system", nullable = false)
    private String sourceSystem;

    @Column(name = "target_system", nullable = false)
    private String targetSystem;

    @Column(name = "migration_type", nullable = false)
    private String migrationType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private MigrationStatus status = MigrationStatus.PLANNING;

    @Column(name = "planned_start_date")
    private LocalDate plannedStartDate;

    @Column(name = "planned_end_date")
    private LocalDate plannedEndDate;

    @Column(name = "actual_start_date")
    private LocalDate actualStartDate;

    @Column(name = "actual_end_date")
    private LocalDate actualEndDate;
}
