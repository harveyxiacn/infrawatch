package com.infrawatch.model.virtualization;

import com.infrawatch.model.base.BaseEntity;
import com.infrawatch.model.virtualization.enums.HypervisorType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "hypervisors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Hypervisor extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String hostname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private HypervisorType type;

    private String version;

    @Column(name = "total_cpu_cores", nullable = false)
    @Builder.Default
    private int totalCpuCores = 1;

    @Column(name = "total_ram_gb", nullable = false)
    @Builder.Default
    private int totalRamGb = 1;

    @Column(name = "total_storage_gb", nullable = false)
    @Builder.Default
    private int totalStorageGb = 10;

    @Column(name = "cluster")
    private String cluster;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "ACTIVE";
}
