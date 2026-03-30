package com.infrawatch.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BackupTrendData {

    private LocalDate date;
    private long successCount;
    private long failedCount;
    private long totalCount;
}
