package com.infrawatch.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DRPlanCreateRequest {

    @NotBlank(message = "System name is required")
    private String systemName;

    @Min(value = 1, message = "RTO must be at least 1 minute")
    private int rtoMinutes;

    @Min(value = 1, message = "RPO must be at least 1 minute")
    private int rpoMinutes;

    private String recoverySteps;

    private String dependencies;

    @NotBlank(message = "Responsible team is required")
    private String responsibleTeam;

    @Email(message = "Invalid email format")
    private String contactEmail;
}
