package com.infrawatch.controller.api;

import com.infrawatch.dto.request.MigrationProjectCreateRequest;
import com.infrawatch.dto.response.ApiResponse;
import com.infrawatch.dto.response.MigrationProgressData;
import com.infrawatch.dto.response.PageResponse;
import com.infrawatch.model.migration.MigrationProject;
import com.infrawatch.model.migration.MigrationTask;
import com.infrawatch.model.migration.MigrationValidation;
import com.infrawatch.model.migration.enums.MigrationStatus;
import com.infrawatch.model.migration.enums.ValidationType;
import com.infrawatch.service.migration.MigrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/migrations")
@RequiredArgsConstructor
public class MigrationApiController {

    private final MigrationService migrationService;

    // ── Projects ──

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<MigrationProject>>> listProjects(
            @RequestParam(required = false) MigrationStatus status,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<MigrationProject> page;
        if (status != null) {
            page = migrationService.findProjectsByStatus(status, pageable);
        } else {
            page = migrationService.findAllProjects(pageable);
        }
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(page)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MigrationProject>> getProject(@PathVariable UUID id) {
        MigrationProject project = migrationService.findProjectById(id);
        return ResponseEntity.ok(ApiResponse.ok(project));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<MigrationProject>> createProject(
            @Valid @RequestBody MigrationProjectCreateRequest request) {
        MigrationProject project = migrationService.createProject(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(project, "Migration project created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<MigrationProject>> updateProject(
            @PathVariable UUID id,
            @Valid @RequestBody MigrationProjectCreateRequest request) {
        MigrationProject project = migrationService.updateProject(id, request);
        return ResponseEntity.ok(ApiResponse.ok(project, "Migration project updated successfully"));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<MigrationProject>> updateProjectStatus(
            @PathVariable UUID id,
            @RequestParam MigrationStatus status) {
        MigrationProject project = migrationService.updateProjectStatus(id, status);
        return ResponseEntity.ok(ApiResponse.ok(project, "Migration project status updated"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteProject(@PathVariable UUID id) {
        migrationService.deleteProject(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Migration project deleted successfully"));
    }

    @GetMapping("/{id}/progress")
    public ResponseEntity<ApiResponse<MigrationProgressData>> getProjectProgress(@PathVariable UUID id) {
        MigrationProgressData progress = migrationService.getProjectProgress(id);
        return ResponseEntity.ok(ApiResponse.ok(progress));
    }

    // ── Tasks ──

    @GetMapping("/{projectId}/tasks")
    public ResponseEntity<ApiResponse<List<MigrationTask>>> listTasks(@PathVariable UUID projectId) {
        List<MigrationTask> tasks = migrationService.findTasksByProjectId(projectId);
        return ResponseEntity.ok(ApiResponse.ok(tasks));
    }

    @GetMapping("/tasks/{taskId}")
    public ResponseEntity<ApiResponse<MigrationTask>> getTask(@PathVariable UUID taskId) {
        MigrationTask task = migrationService.findTaskById(taskId);
        return ResponseEntity.ok(ApiResponse.ok(task));
    }

    @PostMapping("/{projectId}/tasks")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<MigrationTask>> createTask(
            @PathVariable UUID projectId,
            @RequestBody MigrationTask task) {
        MigrationTask created = migrationService.createTask(projectId, task);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(created, "Migration task created successfully"));
    }

    @PutMapping("/tasks/{taskId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<MigrationTask>> updateTask(
            @PathVariable UUID taskId,
            @RequestBody MigrationTask task) {
        MigrationTask updated = migrationService.updateTask(taskId, task);
        return ResponseEntity.ok(ApiResponse.ok(updated, "Migration task updated successfully"));
    }

    @DeleteMapping("/tasks/{taskId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteTask(@PathVariable UUID taskId) {
        migrationService.deleteTask(taskId);
        return ResponseEntity.ok(ApiResponse.ok(null, "Migration task deleted successfully"));
    }

    // ── Validations ──

    @GetMapping("/tasks/{taskId}/validations")
    public ResponseEntity<ApiResponse<List<MigrationValidation>>> listValidations(
            @PathVariable UUID taskId) {
        List<MigrationValidation> validations = migrationService.findValidationsByTaskId(taskId);
        return ResponseEntity.ok(ApiResponse.ok(validations));
    }

    @PostMapping("/tasks/{taskId}/validations")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<MigrationValidation>> recordValidation(
            @PathVariable UUID taskId,
            @RequestParam ValidationType validationType,
            @RequestParam String sourceValue,
            @RequestParam String targetValue,
            @RequestParam boolean passed,
            @RequestParam(required = false) String notes) {
        MigrationValidation validation = migrationService.recordValidation(
                taskId, validationType, sourceValue, targetValue, passed, notes);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(validation, "Validation recorded successfully"));
    }
}
