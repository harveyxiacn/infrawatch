package com.infrawatch.model.server;

import com.infrawatch.model.base.BaseEntity;
import com.infrawatch.model.server.enums.Environment;
import com.infrawatch.model.server.enums.ServerStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "servers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Server extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String hostname;

    @Column(name = "ip_address", nullable = false, length = 45)
    private String ipAddress;

    @Column(length = 100)
    private String os;

    @Column(name = "cpu_cores", nullable = false)
    @Builder.Default
    private int cpuCores = 1;

    @Column(name = "ram_gb", nullable = false)
    @Builder.Default
    private int ramGb = 1;

    @Column(name = "disk_gb", nullable = false)
    @Builder.Default
    private int diskGb = 10;

    private String location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ServerStatus status = ServerStatus.ONLINE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Environment environment = Environment.DEV;

    @Column(columnDefinition = "TEXT")
    private String description;
}
