package com.infrawatch.controller.api;

import com.infrawatch.dto.request.ServerCreateRequest;
import com.infrawatch.dto.response.ApiResponse;
import com.infrawatch.dto.response.PageResponse;
import com.infrawatch.dto.response.ServerResponse;
import com.infrawatch.model.server.HealthMetric;
import com.infrawatch.model.server.Server;
import com.infrawatch.model.server.enums.Environment;
import com.infrawatch.model.server.enums.ServerStatus;
import com.infrawatch.service.server.HealthMetricService;
import com.infrawatch.service.server.ServerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/servers")
@RequiredArgsConstructor
public class ServerApiController {

    private final ServerService serverService;
    private final HealthMetricService healthMetricService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ServerResponse>>> list(
            @RequestParam(required = false) ServerStatus status,
            @RequestParam(required = false) Environment environment,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<Server> page;
        if (search != null && !search.isBlank()) {
            page = serverService.search(search, pageable);
        } else if (status != null) {
            page = serverService.findByStatus(status, pageable);
        } else if (environment != null) {
            page = serverService.findByEnvironment(environment, pageable);
        } else {
            page = serverService.findAll(pageable);
        }

        Page<ServerResponse> responsePage = page.map(ServerResponse::from);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(responsePage)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ServerResponse>> getById(@PathVariable UUID id) {
        Server server = serverService.findById(id);
        return ResponseEntity.ok(ApiResponse.ok(ServerResponse.from(server)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<ServerResponse>> create(@Valid @RequestBody ServerCreateRequest request) {
        Server server = serverService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(ServerResponse.from(server), "Server created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<ServerResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody ServerCreateRequest request) {
        Server server = serverService.update(id, request);
        return ResponseEntity.ok(ApiResponse.ok(ServerResponse.from(server), "Server updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        serverService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Server deleted successfully"));
    }

    @GetMapping("/{id}/metrics")
    public ResponseEntity<ApiResponse<List<HealthMetric>>> getMetrics(
            @PathVariable UUID id,
            @RequestParam(required = false) LocalDateTime from,
            @RequestParam(required = false) LocalDateTime to) {
        if (from == null) from = LocalDateTime.now().minusDays(1);
        if (to == null) to = LocalDateTime.now();
        List<HealthMetric> metrics = healthMetricService.getMetrics(id, from, to);
        return ResponseEntity.ok(ApiResponse.ok(metrics));
    }

    @GetMapping("/{id}/metrics/latest")
    public ResponseEntity<ApiResponse<HealthMetric>> getLatestMetric(@PathVariable UUID id) {
        HealthMetric metric = healthMetricService.getLatest(id);
        return ResponseEntity.ok(ApiResponse.ok(metric));
    }
}
