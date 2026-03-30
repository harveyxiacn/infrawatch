package com.infrawatch.dto.response;

import com.infrawatch.model.server.Server;
import com.infrawatch.model.server.enums.Environment;
import com.infrawatch.model.server.enums.ServerStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServerResponse {

    private UUID id;
    private String hostname;
    private String ipAddress;
    private String os;
    private int cpuCores;
    private int ramGb;
    private int diskGb;
    private String location;
    private ServerStatus status;
    private Environment environment;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ServerResponse from(Server server) {
        return ServerResponse.builder()
                .id(server.getId())
                .hostname(server.getHostname())
                .ipAddress(server.getIpAddress())
                .os(server.getOs())
                .cpuCores(server.getCpuCores())
                .ramGb(server.getRamGb())
                .diskGb(server.getDiskGb())
                .location(server.getLocation())
                .status(server.getStatus())
                .environment(server.getEnvironment())
                .description(server.getDescription())
                .createdAt(server.getCreatedAt())
                .updatedAt(server.getUpdatedAt())
                .build();
    }
}
