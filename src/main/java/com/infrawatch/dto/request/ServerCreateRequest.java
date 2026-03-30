package com.infrawatch.dto.request;

import com.infrawatch.model.server.enums.Environment;
import com.infrawatch.model.server.enums.ServerStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServerCreateRequest {

    @NotBlank(message = "Hostname is required")
    private String hostname;

    @NotBlank(message = "IP address is required")
    private String ipAddress;

    private String os;

    @Min(value = 1, message = "CPU cores must be at least 1")
    private int cpuCores = 1;

    @Min(value = 1, message = "RAM must be at least 1 GB")
    private int ramGb = 1;

    @Min(value = 1, message = "Disk must be at least 1 GB")
    private int diskGb = 10;

    private String location;

    @NotNull(message = "Status is required")
    private ServerStatus status = ServerStatus.ONLINE;

    @NotNull(message = "Environment is required")
    private Environment environment = Environment.DEV;

    private String description;
}
