package com.infrawatch.service.server;

import com.infrawatch.model.server.HealthMetric;
import com.infrawatch.model.server.Server;
import com.infrawatch.repository.server.HealthMetricRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class HealthMetricService {

    private final HealthMetricRepository healthMetricRepository;
    private final ServerService serverService;

    public List<HealthMetric> getMetrics(UUID serverId, LocalDateTime from, LocalDateTime to) {
        return healthMetricRepository.findByServerIdAndTimestampBetweenOrderByTimestampAsc(serverId, from, to);
    }

    public HealthMetric getLatest(UUID serverId) {
        return healthMetricRepository.findLatestByServerId(serverId);
    }

    public HealthMetric record(UUID serverId, HealthMetric metric) {
        Server server = serverService.findById(serverId);
        metric.setServer(server);
        if (metric.getTimestamp() == null) {
            metric.setTimestamp(LocalDateTime.now());
        }
        return healthMetricRepository.save(metric);
    }

    public void cleanupOldMetrics(int retentionDays) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(retentionDays);
        healthMetricRepository.deleteByTimestampBefore(cutoff);
    }
}
