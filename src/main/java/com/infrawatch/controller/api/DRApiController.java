package com.infrawatch.controller.api;

import com.infrawatch.dto.request.DRPlanCreateRequest;
import com.infrawatch.dto.response.ApiResponse;
import com.infrawatch.dto.response.PageResponse;
import com.infrawatch.model.backup.DRPlan;
import com.infrawatch.model.backup.DrillLog;
import com.infrawatch.service.backup.DRPlanService;
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
@RequestMapping("/api/dr")
@RequiredArgsConstructor
public class DRApiController {

    private final DRPlanService drPlanService;

    @GetMapping("/plans")
    public ResponseEntity<ApiResponse<PageResponse<DRPlan>>> listPlans(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<DRPlan> page = drPlanService.findAll(pageable);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(page)));
    }

    @GetMapping("/plans/{id}")
    public ResponseEntity<ApiResponse<DRPlan>> getPlan(@PathVariable UUID id) {
        DRPlan plan = drPlanService.findById(id);
        return ResponseEntity.ok(ApiResponse.ok(plan));
    }

    @PostMapping("/plans")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<DRPlan>> createPlan(@Valid @RequestBody DRPlanCreateRequest request) {
        DRPlan plan = drPlanService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(plan, "DR plan created successfully"));
    }

    @PutMapping("/plans/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<DRPlan>> updatePlan(
            @PathVariable UUID id,
            @Valid @RequestBody DRPlanCreateRequest request) {
        DRPlan plan = drPlanService.update(id, request);
        return ResponseEntity.ok(ApiResponse.ok(plan, "DR plan updated successfully"));
    }

    @DeleteMapping("/plans/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deletePlan(@PathVariable UUID id) {
        drPlanService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "DR plan deleted successfully"));
    }

    @GetMapping("/plans/{planId}/drills")
    public ResponseEntity<ApiResponse<PageResponse<DrillLog>>> listDrills(
            @PathVariable UUID planId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<DrillLog> page = drPlanService.findDrillsByPlanId(planId, pageable);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(page)));
    }

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
