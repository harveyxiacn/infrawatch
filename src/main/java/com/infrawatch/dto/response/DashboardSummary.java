package com.infrawatch.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummary {

    private long totalServers;
    private long onlineServers;
    private long offlineServers;
    private long totalVms;
    private long totalBackupJobs;
    private long recentBackupSuccessCount;
    private long recentBackupFailCount;
    private long activeMigrations;
    private long totalTestCases;
    private long recentTestPass;
    private long recentTestFail;
}
