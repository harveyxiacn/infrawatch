package com.infrawatch.model.virtualization;

import com.infrawatch.model.base.BaseEntity;
import com.infrawatch.model.virtualization.enums.VmStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "virtual_machines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VirtualMachine extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(name = "guest_os")
    private String guestOs;

    @Column(nullable = false)
    @Builder.Default
    private int vcpu = 1;

    @Column(name = "vram_gb", nullable = false)
    @Builder.Default
    private int vramGb = 1;

    @Column(name = "vdisk_gb", nullable = false)
    @Builder.Default
    private int vdiskGb = 10;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private VmStatus status = VmStatus.RUNNING;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "hypervisor_id", nullable = false)
    private Hypervisor hypervisor;
}
