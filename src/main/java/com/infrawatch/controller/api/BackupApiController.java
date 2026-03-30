package com.infrawatch.controller.api;

import com.infrawatch.dto.request.BackupJobCreateRequest;
import com.infrawatch.dto.response.ApiResponse;
import com.infrawatch.dto.response.BackupTrendData;
import com.infrawatch.dto.response.PageResponse;
import com.infrawatch.model.backup.BackupExecution;
import com.infrawatch.model.backup.BackupJob;
import com.infrawatch.service.backup.BackupService;
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
@RequestMapping("/api/backup")
@RequiredArgsConstructor
public class BackupApiController {

    private final BackupService backupService;

    @GetMapping("/jobs")
    public ResponseEntity<ApiResponse<PageResponse<BackupJob>>> listJobs(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<BackupJob> page = backupService.findAllJobs(pageable);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(page)));
    }

    @GetMapping("/jobs/{id}")
    public ResponseEntity<ApiResponse<BackupJob>> getJob(@PathVariable UUID id) {
        BackupJob job = backupService.findJobById(id);
        return ResponseEntity.ok(ApiResponse.ok(job));
    }

    @PostMapping("/jobs")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<BackupJob>> createJob(@Valid @RequestBody BackupJobCreateRequest request) {
        BackupJob job = backupService.createJob(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(job, "Backup job created successfully"));
    }

    @PutMapping("/jobs/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<BackupJob>> updateJob(
            @PathVariable UUID id,
            @Valid @RequestBody BackupJobCreateRequest request) {
        BackupJob job = backupService.updateJob(id, request);
        return ResponseEntity.ok(ApiResponse.ok(job, "Backup job updated successfully"));
    }

    @DeleteMapping("/jobs/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteJob(@PathVariable UUID id) {
        backupService.deleteJob(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Backup job deleted successfully"));
    }

    @GetMapping("/jobs/{jobId}/executions")
    public ResponseEntity<ApiResponse<PageResponse<BackupExecution>>> listExecutions(
            @PathVariable UUID jobId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<BackupExecution> page = backupService.findExecutionsByJobId(jobId, pageable);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(page)));
    }

    @GetMapping("/trend")
    public ResponseEntity<ApiResponse<List<BackupTrendData>>> getTrend(
            @RequestParam(defaultValue = "30") int days) {
        List<BackupTrendData> trend = backupService.getTrendData(days);
        return ResponseEntity.ok(ApiResponse.ok(trend));
    }
}
