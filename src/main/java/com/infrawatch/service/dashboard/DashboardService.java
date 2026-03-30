package com.infrawatch.service.dashboard;

import com.infrawatch.dto.response.DashboardSummary;
import com.infrawatch.model.server.enums.ServerStatus;
import com.infrawatch.repository.server.ServerRepository;
import com.infrawatch.repository.testing.TestExecutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DashboardService {

    private final ServerRepository serverRepository;
    private final TestExecutionRepository testExecutionRepository;

    public DashboardSummary getSummary() {
        return DashboardSummary.builder()
                .totalServers(serverRepository.count())
                .onlineServers(serverRepository.countByStatus(ServerStatus.ONLINE))
                .offlineServers(serverRepository.countByStatus(ServerStatus.OFFLINE))
                .totalTestCases(0)
                .recentTestPass(testExecutionRepository.countByResult("PASS"))
                .recentTestFail(testExecutionRepository.countByResult("FAIL"))
                .build();
    }
}
