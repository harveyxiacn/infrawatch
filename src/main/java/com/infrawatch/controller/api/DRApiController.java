package com.infrawatch.controller.api;

import com.infrawatch.dto.request.DRPlanCreateRequest;
import com.infrawatch.dto.response.ApiResponse;
import com.infrawatch.dto.response.PageResponse;
import com.infrawatch.model.backup.DRPlan;
import com.infrawatch.model.backup.DrillLog;
import com.infrawatch.service.backup.DRPlanService;
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

@Tag(name = "Disaster Recovery", description = "DR plan and drill log management")
@RestController
@RequestMapping("/api/dr")
@RequiredArgsConstructor
public class DRApiController {

    private final DRPlanService drPlanService;

    @Operation(summary = "List DR plans", description = "Returns paginated list of all disaster recovery plans")
    @GetMapping("/plans")
    public ResponseEntity<ApiResponse<PageResponse<DRPlan>>> listPlans(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<DRPlan> page = drPlanService.findAll(pageable);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(page)));
    }

    @Operation(summary = "Get DR plan by ID", description = "Returns a single DR plan by its unique identifier")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "DR plan found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "DR plan not found")
    })
    @GetMapping("/plans/{id}")
    public ResponseEntity<ApiResponse<DRPlan>> getPlan(@PathVariable UUID id) {
        DRPlan plan = drPlanService.findById(id);
        return ResponseEntity.ok(ApiResponse.ok(plan));
    }

    @Operation(summary = "Create DR plan", description = "Creates a new disaster recovery plan")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "DR plan created"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request body")
    })
    @PostMapping("/plans")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<DRPlan>> createPlan(@Valid @RequestBody DRPlanCreateRequest request) {
        DRPlan plan = drPlanService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(plan, "DR plan created successfully"));
    }

    @Operation(summary = "Update DR plan", description = "Updates an existing DR plan by ID")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "DR plan updated"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "DR plan not found")
    })
    @PutMapping("/plans/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<DRPlan>> updatePlan(
            @PathVariable UUID id,
            @Valid @RequestBody DRPlanCreateRequest request) {
        DRPlan plan = drPlanService.update(id, request);
        return ResponseEntity.ok(ApiResponse.ok(plan, "DR plan updated successfully"));
    }

    @Operation(summary = "Delete DR plan", description = "Deletes a DR plan by ID (admin only)")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "DR plan deleted"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "DR plan not found")
    })
    @DeleteMapping("/plans/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deletePlan(@PathVariable UUID id) {
        drPlanService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "DR plan deleted successfully"));
    }

    @Operation(summary = "List drills for a plan", description = "Returns paginated drill log history for a specific DR plan")
    @GetMapping("/plans/{planId}/drills")
    public ResponseEntity<ApiResponse<PageResponse<DrillLog>>> listDrills(
            @PathVariable UUID planId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<DrillLog> page = drPlanService.findDrillsByPlanId(planId, pageable);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(page)));
    }

    @Operation(summary = "Add drill log", description = "Records a new DR drill execution for a plan")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Drill log added"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "DR plan not found")
    })
    @PostMapping("/plans/{planId}/drills")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<DrillLog>> addDrill(
            @PathVariable UUID planId,
            @Valid @RequestBody DrillLog drillLog) {
        DRPlan plan = drPlanService.findById(planId);
        drillLog.setPlan(plan);
        DrillLog saved = drPlanService.addDrillLog(drillLog);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(saved, "Drill log added successfully"));
    }
}
