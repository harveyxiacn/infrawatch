package com.infrawatch.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MigrationProgressData {

    private UUID projectId;
    private String projectName;
    private long totalTasks;
    private long completedTasks;
    private long failedTasks;
    private double progressPercent;
}
