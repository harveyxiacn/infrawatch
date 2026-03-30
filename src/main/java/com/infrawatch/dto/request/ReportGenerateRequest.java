package com.infrawatch.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportGenerateRequest {

    @NotNull(message = "Template ID is required")
    private UUID templateId;

    private String format = "PDF";
    private LocalDate startDate;
    private LocalDate endDate;
    private String environmentFilter;
}
