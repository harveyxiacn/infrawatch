package com.infrawatch.repository.server;

import com.infrawatch.model.server.HealthMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface HealthMetricRepository extends JpaRepository<HealthMetric, UUID> {

    List<HealthMetric> findByServerIdAndTimestampBetweenOrderByTimestampAsc(
            UUID serverId, LocalDateTime from, LocalDateTime to);

    @Query("SELECT h FROM HealthMetric h WHERE h.server.id = :serverId ORDER BY h.timestamp DESC LIMIT 1")
    HealthMetric findLatestByServerId(@Param("serverId") UUID serverId);

    void deleteByTimestampBefore(LocalDateTime before);
}
