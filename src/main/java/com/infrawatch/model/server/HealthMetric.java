package com.infrawatch.model.server;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "health_metrics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HealthMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id", nullable = false)
    private Server server;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    @Column(name = "cpu_percent", precision = 5, scale = 2)
    private BigDecimal cpuPercent;

    @Column(name = "mem_percent", precision = 5, scale = 2)
    private BigDecimal memPercent;

    @Column(name = "disk_percent", precision = 5, scale = 2)
    private BigDecimal diskPercent;

    @Column(name = "network_in_mbps", precision = 10, scale = 2)
    private BigDecimal networkInMbps;

    @Column(name = "network_out_mbps", precision = 10, scale = 2)
    private BigDecimal networkOutMbps;

    @Column(name = "uptime_seconds")
    private Long uptimeSeconds;
}
