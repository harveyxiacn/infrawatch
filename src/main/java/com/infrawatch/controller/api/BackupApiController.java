package com.infrawatch.controller.api;

import com.infrawatch.dto.request.BackupJobCreateRequest;
import com.infrawatch.dto.response.ApiResponse;
import com.infrawatch.dto.response.BackupTrendData;
import com.infrawatch.dto.response.PageResponse;
import com.infrawatch.model.backup.BackupExecution;
import com.infrawatch.model.backup.BackupJob;
import com.infrawatch.service.backup.BackupService;
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

@Tag(name = "Backup", description = "Backup job and execution management")
@RestController
@RequestMapping("/api/backup")
@RequiredArgsConstructor
public class BackupApiController {

    private final BackupService backupService;

    @Operation(summary = "List backup jobs", description = "Returns paginated list of all backup jobs")
    @GetMapping("/jobs")
    public ResponseEntity<ApiResponse<PageResponse<BackupJob>>> listJobs(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<BackupJob> page = backupService.findAllJobs(pageable);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(page)));
    }

    @Operation(summary = "Get backup job by ID", description = "Returns a single backup job by its unique identifier")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Backup job found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Backup job not found")
    })
    @GetMapping("/jobs/{id}")
    public ResponseEntity<ApiResponse<BackupJob>> getJob(@PathVariable UUID id) {
        BackupJob job = backupService.findJobById(id);
        return ResponseEntity.ok(ApiResponse.ok(job));
    }

    @Operation(summary = "Create backup job", description = "Creates a new backup job configuration")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Backup job created"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request body")
    })
    @PostMapping("/jobs")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<BackupJob>> createJob(@Valid @RequestBody BackupJobCreateRequest request) {
        BackupJob job = backupService.createJob(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(job, "Backup job created successfully"));
    }

    @Operation(summary = "Update backup job", description = "Updates an existing backup job by ID")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Backup job updated"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Backup job not found")
    })
    @PutMapping("/jobs/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<BackupJob>> updateJob(
            @PathVariable UUID id,
            @Valid @RequestBody BackupJobCreateRequest request) {
        BackupJob job = backupService.updateJob(id, request);
        return ResponseEntity.ok(ApiResponse.ok(job, "Backup job updated successfully"));
    }

    @Operation(summary = "Delete backup job", description = "Deletes a backup job by ID (admin only)")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Backup job deleted"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Backup job not found")
    })
    @DeleteMapping("/jobs/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteJob(@PathVariable UUID id) {
        backupService.deleteJob(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Backup job deleted successfully"));
    }

    @Operation(summary = "List job executions", description = "Returns paginated execution history for a specific backup job")
    @GetMapping("/jobs/{jobId}/executions")
    public ResponseEntity<ApiResponse<PageResponse<BackupExecution>>> listExecutions(
            @PathVariable UUID jobId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<BackupExecution> page = backupService.findExecutionsByJobId(jobId, pageable);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(page)));
    }

    @Operation(summary = "Get backup trend", description = "Returns backup success/failure trend data for the specified number of days")
    @GetMapping("/trend")
    public ResponseEntity<ApiResponse<List<BackupTrendData>>> getTrend(
            @RequestParam(defaultValue = "30") int days) {
        List<BackupTrendData> trend = backupService.getTrendData(days);
        return ResponseEntity.ok(ApiResponse.ok(trend));
    }
}
