package com.infrawatch.model.migration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.infrawatch.model.migration.enums.ValidationType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "migration_validations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MigrationValidation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private MigrationTask task;

    @Enumerated(EnumType.STRING)
    @Column(name = "validation_type", nullable = false, length = 20)
    private ValidationType validationType;

    @Column(name = "source_value")
    private String sourceValue;

    @Column(name = "target_value")
    private String targetValue;

    @Column(nullable = false)
    private boolean passed;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "executed_at", nullable = false)
    private LocalDateTime executedAt;
}
