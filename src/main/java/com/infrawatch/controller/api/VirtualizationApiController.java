package com.infrawatch.controller.api;

import com.infrawatch.dto.request.VmCreateRequest;
import com.infrawatch.dto.response.ApiResponse;
import com.infrawatch.dto.response.PageResponse;
import com.infrawatch.model.virtualization.Hypervisor;
import com.infrawatch.model.virtualization.VirtualMachine;
import com.infrawatch.service.virtualization.VirtualizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Virtualization", description = "Hypervisor and virtual machine management")
@RestController
@RequestMapping("/api/virtualization")
@RequiredArgsConstructor
public class VirtualizationApiController {

    private final VirtualizationService virtualizationService;

    // ── Hypervisor endpoints ─────────────────────────────────────────────

    @Operation(summary = "List hypervisors", description = "Returns paginated list of all hypervisors")
    @GetMapping("/hypervisors")
    public ResponseEntity<ApiResponse<PageResponse<Hypervisor>>> listHypervisors(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<Hypervisor> page = virtualizationService.findAllHypervisors(pageable);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(page)));
    }

    @Operation(summary = "Get hypervisor by ID", description = "Returns a single hypervisor by its unique identifier")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Hypervisor found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Hypervisor not found")
    })
    @GetMapping("/hypervisors/{id}")
    public ResponseEntity<ApiResponse<Hypervisor>> getHypervisor(@PathVariable UUID id) {
        Hypervisor hypervisor = virtualizationService.findHypervisorById(id);
        return ResponseEntity.ok(ApiResponse.ok(hypervisor));
    }

    @Operation(summary = "Create hypervisor", description = "Creates a new hypervisor entry")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Hypervisor created"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request body")
    })
    @PostMapping("/hypervisors")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<Hypervisor>> createHypervisor(@Valid @RequestBody Hypervisor hypervisor) {
        Hypervisor created = virtualizationService.createHypervisor(hypervisor);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(created, "Hypervisor created successfully"));
    }

    // ── VirtualMachine endpoints ─────────────────────────────────────────

    @Operation(summary = "List virtual machines", description = "Returns paginated list of VMs with optional hypervisor filter")
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

    @Operation(summary = "Get VM by ID", description = "Returns a single virtual machine by its unique identifier")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "VM found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "VM not found")
    })
    @GetMapping("/vms/{id}")
    public ResponseEntity<ApiResponse<VirtualMachine>> getVm(@PathVariable UUID id) {
        VirtualMachine vm = virtualizationService.findVmById(id);
        return ResponseEntity.ok(ApiResponse.ok(vm));
    }

    @Operation(summary = "Create VM", description = "Creates a new virtual machine on a hypervisor")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "VM created"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request body")
    })
    @PostMapping("/vms")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<VirtualMachine>> createVm(@Valid @RequestBody VmCreateRequest request) {
        VirtualMachine vm = virtualizationService.createVm(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(vm, "Virtual machine created successfully"));
    }

    @Operation(summary = "Update VM", description = "Updates an existing virtual machine by ID")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "VM updated"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "VM not found")
    })
    @PutMapping("/vms/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<VirtualMachine>> updateVm(
            @PathVariable UUID id,
            @Valid @RequestBody VmCreateRequest request) {
        VirtualMachine vm = virtualizationService.updateVm(id, request);
        return ResponseEntity.ok(ApiResponse.ok(vm, "Virtual machine updated successfully"));
    }

    @Operation(summary = "Delete VM", description = "Deletes a virtual machine by ID (admin only)")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "VM deleted"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "VM not found")
    })
    @DeleteMapping("/vms/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteVm(@PathVariable UUID id) {
        virtualizationService.deleteVm(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Virtual machine deleted successfully"));
    }
}
