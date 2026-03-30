package com.infrawatch.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthMetricSummary {

    private UUID serverId;
    private BigDecimal avgCpuPercent;
    private BigDecimal avgMemPercent;
    private BigDecimal avgDiskPercent;

    public HealthMetricSummary(UUID serverId, Double avgCpu, Double avgMem, Double avgDisk) {
        this.serverId = serverId;
        this.avgCpuPercent = avgCpu != null ? BigDecimal.valueOf(avgCpu) : null;
        this.avgMemPercent = avgMem != null ? BigDecimal.valueOf(avgMem) : null;
        this.avgDiskPercent = avgDisk != null ? BigDecimal.valueOf(avgDisk) : null;
    }
}
