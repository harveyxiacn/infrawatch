package com.infrawatch.model.migration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "migration_tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class MigrationTask {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private MigrationProject project;

    @Column(name = "dataset_name", nullable = false)
    private String datasetName;

    @Column(name = "source_table")
    private String sourceTable;

    @Column(name = "target_table")
    private String targetTable;

    @Column(name = "expected_row_count")
    private Long expectedRowCount;

    @Column(name = "actual_row_count")
    private Long actualRowCount;

    @Column(name = "transformation_rules", columnDefinition = "TEXT")
    private String transformationRules;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "PENDING";

    @Column(name = "sort_order")
    @Builder.Default
    private int sortOrder = 0;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
