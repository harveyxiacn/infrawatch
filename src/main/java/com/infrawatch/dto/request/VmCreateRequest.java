package com.infrawatch.dto.request;

import com.infrawatch.model.virtualization.enums.VmStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VmCreateRequest {

    @NotBlank(message = "VM name is required")
    private String name;

    private String guestOs;

    @Min(value = 1, message = "vCPU must be at least 1")
    private int vcpu = 1;

    @Min(value = 1, message = "vRAM must be at least 1 GB")
    private int vramGb = 1;

    @Min(value = 1, message = "vDisk must be at least 1 GB")
    private int vdiskGb = 10;

    @NotNull(message = "Hypervisor ID is required")
    private UUID hypervisorId;

    private VmStatus status = VmStatus.RUNNING;
}
