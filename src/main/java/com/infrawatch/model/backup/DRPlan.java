package com.infrawatch.model.backup;

import com.infrawatch.model.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "dr_plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DRPlan extends BaseEntity {

    @Column(name = "system_name", nullable = false)
    private String systemName;

    @Column(name = "rto_minutes", nullable = false)
    private int rtoMinutes;

    @Column(name = "rpo_minutes", nullable = false)
    private int rpoMinutes;

    @Column(name = "recovery_steps", columnDefinition = "TEXT")
    private String recoverySteps;

    @Column(columnDefinition = "TEXT")
    private String dependencies;

    @Column(name = "responsible_team")
    private String responsibleTeam;

    @Column(name = "contact_email")
    private String contactEmail;

    @Column(nullable = false)
    @Builder.Default
    private int version = 1;

    @Column(name = "last_review_date")
    private LocalDate lastReviewDate;
}
