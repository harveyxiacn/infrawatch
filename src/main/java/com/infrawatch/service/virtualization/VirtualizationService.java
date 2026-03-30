package com.infrawatch.service.virtualization;

import com.infrawatch.dto.request.VmCreateRequest;
import com.infrawatch.exception.BusinessValidationException;
import com.infrawatch.exception.ResourceNotFoundException;
import com.infrawatch.model.virtualization.Hypervisor;
import com.infrawatch.model.virtualization.VirtualMachine;
import com.infrawatch.model.virtualization.enums.VmStatus;
import com.infrawatch.repository.virtualization.HypervisorRepository;
import com.infrawatch.repository.virtualization.VirtualMachineRepository;
import com.infrawatch.service.auth.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class VirtualizationService {

    private final HypervisorRepository hypervisorRepository;
    private final VirtualMachineRepository virtualMachineRepository;
    private final AuditService auditService;

    // ── Hypervisor CRUD ──────────────────────────────────────────────────

    public Page<Hypervisor> findAllHypervisors(Pageable pageable) {
        return hypervisorRepository.findAll(pageable);
    }

    public Hypervisor findHypervisorById(UUID id) {
        return hypervisorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hypervisor", id));
    }

    public Hypervisor createHypervisor(Hypervisor hypervisor) {
        if (hypervisorRepository.existsByHostname(hypervisor.getHostname())) {
            throw new BusinessValidationException("Hypervisor hostname already exists: " + hypervisor.getHostname());
        }

        hypervisor = hypervisorRepository.save(hypervisor);
        auditService.log("CREATE", "Hypervisor", hypervisor.getId(),
                "Created hypervisor: " + hypervisor.getHostname());
        return hypervisor;
    }

    public void deleteHypervisor(UUID id) {
        Hypervisor hypervisor = findHypervisorById(id);
        hypervisorRepository.delete(hypervisor);
        auditService.log("DELETE", "Hypervisor", id,
                "Deleted hypervisor: " + hypervisor.getHostname());
    }

    public long countVmsByHypervisor(UUID hypervisorId) {
        return virtualMachineRepository.countByHypervisorId(hypervisorId);
    }

    // ── VirtualMachine CRUD ──────────────────────────────────────────────

    public Page<VirtualMachine> findAllVms(Pageable pageable) {
        return virtualMachineRepository.findAll(pageable);
    }

    public Page<VirtualMachine> findVmsByHypervisor(UUID hypervisorId, Pageable pageable) {
        return virtualMachineRepository.findByHypervisorId(hypervisorId, pageable);
    }

    public Page<VirtualMachine> findVmsByStatus(VmStatus status, Pageable pageable) {
        return virtualMachineRepository.findByStatus(status, pageable);
    }

    public VirtualMachine findVmById(UUID id) {
        return virtualMachineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("VirtualMachine", id));
    }

    public VirtualMachine createVm(VmCreateRequest request) {
        Hypervisor hypervisor = findHypervisorById(request.getHypervisorId());

        VirtualMachine vm = VirtualMachine.builder()
                .name(request.getName())
                .guestOs(request.getGuestOs())
                .vcpu(request.getVcpu())
                .vramGb(request.getVramGb())
                .vdiskGb(request.getVdiskGb())
                .status(request.getStatus() != null ? request.getStatus() : VmStatus.RUNNING)
                .hypervisor(hypervisor)
                .build();

        vm = virtualMachineRepository.save(vm);
        auditService.log("CREATE", "VirtualMachine", vm.getId(),
                "Created VM: " + vm.getName() + " on hypervisor: " + hypervisor.getHostname());
        return vm;
    }

    public VirtualMachine updateVm(UUID id, VmCreateRequest request) {
        VirtualMachine vm = findVmById(id);
        Hypervisor hypervisor = findHypervisorById(request.getHypervisorId());

        vm.setName(request.getName());
        vm.setGuestOs(request.getGuestOs());
        vm.setVcpu(request.getVcpu());
        vm.setVramGb(request.getVramGb());
        vm.setVdiskGb(request.getVdiskGb());
        vm.setStatus(request.getStatus() != null ? request.getStatus() : vm.getStatus());
        vm.setHypervisor(hypervisor);

        vm = virtualMachineRepository.save(vm);
        auditService.log("UPDATE", "VirtualMachine", vm.getId(),
                "Updated VM: " + vm.getName());
        return vm;
    }

    public void deleteVm(UUID id) {
        VirtualMachine vm = findVmById(id);
        virtualMachineRepository.delete(vm);
        auditService.log("DELETE", "VirtualMachine", id,
                "Deleted VM: " + vm.getName());
    }
}
