package com.infrawatch.controller.api;

import com.infrawatch.dto.request.VmCreateRequest;
import com.infrawatch.dto.response.ApiResponse;
import com.infrawatch.dto.response.PageResponse;
import com.infrawatch.model.virtualization.Hypervisor;
import com.infrawatch.model.virtualization.VirtualMachine;
import com.infrawatch.service.virtualization.VirtualizationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/virtualization")
@RequiredArgsConstructor
public class VirtualizationApiController {

    private final VirtualizationService virtualizationService;

    // ── Hypervisor endpoints ─────────────────────────────────────────────

    @GetMapping("/hypervisors")
    public ResponseEntity<ApiResponse<PageResponse<Hypervisor>>> listHypervisors(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<Hypervisor> page = virtualizationService.findAllHypervisors(pageable);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(page)));
    }

    @GetMapping("/hypervisors/{id}")
    public ResponseEntity<ApiResponse<Hypervisor>> getHypervisor(@PathVariable UUID id) {
        Hypervisor hypervisor = virtualizationService.findHypervisorById(id);
        return ResponseEntity.ok(ApiResponse.ok(hypervisor));
    }

    @PostMapping("/hypervisors")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<Hypervisor>> createHypervisor(@Valid @RequestBody Hypervisor hypervisor) {
        Hypervisor created = virtualizationService.createHypervisor(hypervisor);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(created, "Hypervisor created successfully"));
    }

    // ── VirtualMachine endpoints ─────────────────────────────────────────

    @GetMapping("/vms")
    public ResponseEntity<ApiResponse<PageResponse<VirtualMachine>>> listVms(
            @RequestParam(required = false) UUID hypervisorId,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<VirtualMachine> page;
        if (hypervisorId != null) {
            page = virtualizationService.findVmsByHypervisor(hypervisorId, pageable);
        } else {
            page = virtualizationService.findAllVms(pageable);
        }

        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(page)));
    }

    @GetMapping("/vms/{id}")
    public ResponseEntity<ApiResponse<VirtualMachine>> getVm(@PathVariable UUID id) {
        VirtualMachine vm = virtualizationService.findVmById(id);
        return ResponseEntity.ok(ApiResponse.ok(vm));
    }

    @PostMapping("/vms")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<VirtualMachine>> createVm(@Valid @RequestBody VmCreateRequest request) {
        VirtualMachine vm = virtualizationService.createVm(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(vm, "Virtual machine created successfully"));
    }

    @PutMapping("/vms/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<VirtualMachine>> updateVm(
            @PathVariable UUID id,
            @Valid @RequestBody VmCreateRequest request) {
        VirtualMachine vm = virtualizationService.updateVm(id, request);
        return ResponseEntity.ok(ApiResponse.ok(vm, "Virtual machine updated successfully"));
    }

    @DeleteMapping("/vms/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteVm(@PathVariable UUID id) {
        virtualizationService.deleteVm(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Virtual machine deleted successfully"));
    }
}
