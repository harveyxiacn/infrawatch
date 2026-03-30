package com.infrawatch.model.backup;

import com.infrawatch.model.backup.enums.BackupType;
import com.infrawatch.model.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "backup_jobs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BackupJob extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private BackupType type = BackupType.FULL;

    @Column(name = "source_system", nullable = false)
    private String sourceSystem;

    @Column(name = "target_location", nullable = false)
    private String targetLocation;

    @Column(name = "schedule_cron")
    private String scheduleCron;

    @Column(name = "retention_days", nullable = false)
    @Builder.Default
    private int retentionDays = 30;

    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = true;
}
