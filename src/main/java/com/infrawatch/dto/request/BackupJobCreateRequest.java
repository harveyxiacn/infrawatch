package com.infrawatch.dto.request;

import com.infrawatch.model.backup.enums.BackupType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BackupJobCreateRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Backup type is required")
    private BackupType type;

    @NotBlank(message = "Source system is required")
    private String sourceSystem;

    @NotBlank(message = "Target location is required")
    private String targetLocation;

    private String scheduleCron;

    @Min(value = 1, message = "Retention days must be at least 1")
    private int retentionDays = 30;

    private boolean enabled = true;
}
