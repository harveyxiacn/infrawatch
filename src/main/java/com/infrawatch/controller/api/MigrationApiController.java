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

import java.util.List;
import java.util.UUID;

@Tag(name = "Migrations", description = "Data migration project, task, and validation management")
@RestController
@RequestMapping("/api/migrations")
@RequiredArgsConstructor
public class MigrationApiController {

    private final MigrationService migrationService;

    // ── Projects ──

    @Operation(summary = "List migration projects", description = "Returns paginated list of migration projects with optional status filter")
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

    @Operation(summary = "Get migration project by ID", description = "Returns a single migration project by its unique identifier")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Project found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Project not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MigrationProject>> getProject(@PathVariable UUID id) {
        MigrationProject project = migrationService.findProjectById(id);
        return ResponseEntity.ok(ApiResponse.ok(project));
    }

    @Operation(summary = "Create migration project", description = "Creates a new data migration project")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Project created"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request body")
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<MigrationProject>> createProject(
            @Valid @RequestBody MigrationProjectCreateRequest request) {
        MigrationProject project = migrationService.createProject(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(project, "Migration project created successfully"));
    }

    @Operation(summary = "Update migration project", description = "Updates an existing migration project by ID")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Project updated"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Project not found")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<MigrationProject>> updateProject(
            @PathVariable UUID id,
            @Valid @RequestBody MigrationProjectCreateRequest request) {
        MigrationProject project = migrationService.updateProject(id, request);
        return ResponseEntity.ok(ApiResponse.ok(project, "Migration project updated successfully"));
    }

    @Operation(summary = "Update project status", description = "Updates only the status of a migration project")
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<MigrationProject>> updateProjectStatus(
            @PathVariable UUID id,
            @RequestParam MigrationStatus status) {
        MigrationProject project = migrationService.updateProjectStatus(id, status);
        return ResponseEntity.ok(ApiResponse.ok(project, "Migration project status updated"));
    }

    @Operation(summary = "Delete migration project", description = "Deletes a migration project by ID (admin only)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteProject(@PathVariable UUID id) {
        migrationService.deleteProject(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Migration project deleted successfully"));
    }

    @Operation(summary = "Get project progress", description = "Returns progress data including task completion percentages for a project")
    @GetMapping("/{id}/progress")
    public ResponseEntity<ApiResponse<MigrationProgressData>> getProjectProgress(@PathVariable UUID id) {
        MigrationProgressData progress = migrationService.getProjectProgress(id);
        return ResponseEntity.ok(ApiResponse.ok(progress));
    }

    // ── Tasks ──

    @Operation(summary = "List tasks for a project", description = "Returns all migration tasks belonging to a specific project")
    @GetMapping("/{projectId}/tasks")
    public ResponseEntity<ApiResponse<List<MigrationTask>>> listTasks(@PathVariable UUID projectId) {
        List<MigrationTask> tasks = migrationService.findTasksByProjectId(projectId);
        return ResponseEntity.ok(ApiResponse.ok(tasks));
    }

    @Operation(summary = "Get task by ID", description = "Returns a single migration task by its unique identifier")
    @GetMapping("/tasks/{taskId}")
    public ResponseEntity<ApiResponse<MigrationTask>> getTask(@PathVariable UUID taskId) {
        MigrationTask task = migrationService.findTaskById(taskId);
        return ResponseEntity.ok(ApiResponse.ok(task));
    }

    @Operation(summary = "Create task", description = "Creates a new migration task under a project")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Task created"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request body")
    })
    @PostMapping("/{projectId}/tasks")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<MigrationTask>> createTask(
            @PathVariable UUID projectId,
            @RequestBody MigrationTask task) {
        MigrationTask created = migrationService.createTask(projectId, task);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(created, "Migration task created successfully"));
    }

    @Operation(summary = "Update task", description = "Updates an existing migration task by ID")
    @PutMapping("/tasks/{taskId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<MigrationTask>> updateTask(
            @PathVariable UUID taskId,
            @RequestBody MigrationTask task) {
        MigrationTask updated = migrationService.updateTask(taskId, task);
        return ResponseEntity.ok(ApiResponse.ok(updated, "Migration task updated successfully"));
    }

    @Operation(summary = "Delete task", description = "Deletes a migration task by ID (admin only)")
    @DeleteMapping("/tasks/{taskId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteTask(@PathVariable UUID taskId) {
        migrationService.deleteTask(taskId);
        return ResponseEntity.ok(ApiResponse.ok(null, "Migration task deleted successfully"));
    }

    // ── Validations ──

    @Operation(summary = "List validations for a task", description = "Returns all validation results for a specific migration task")
    @GetMapping("/tasks/{taskId}/validations")
    public ResponseEntity<ApiResponse<List<MigrationValidation>>> listValidations(
            @PathVariable UUID taskId) {
        List<MigrationValidation> validations = migrationService.findValidationsByTaskId(taskId);
        return ResponseEntity.ok(ApiResponse.ok(validations));
    }

    @Operation(summary = "Record validation", description = "Records a new validation result for a migration task")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Validation recorded"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Task not found")
    })
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
