package com.infrawatch.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MigrationProjectCreateRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    @NotBlank(message = "Source system is required")
    private String sourceSystem;

    @NotBlank(message = "Target system is required")
    private String targetSystem;

    @NotBlank(message = "Migration type is required")
    private String migrationType;

    private LocalDate plannedStartDate;

    private LocalDate plannedEndDate;
}
