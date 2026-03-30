package com.infrawatch.repository.virtualization;

import com.infrawatch.model.virtualization.VirtualMachine;
import com.infrawatch.model.virtualization.enums.VmStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface VirtualMachineRepository extends JpaRepository<VirtualMachine, UUID> {

    Page<VirtualMachine> findByHypervisorId(UUID hypervisorId, Pageable pageable);

    Page<VirtualMachine> findByStatus(VmStatus status, Pageable pageable);

    long countByHypervisorId(UUID hypervisorId);

    long countByStatus(VmStatus status);
}
