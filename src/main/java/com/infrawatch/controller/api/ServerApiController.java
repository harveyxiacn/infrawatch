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

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Tag(name = "Servers", description = "Server inventory and health metrics management")
@RestController
@RequestMapping("/api/servers")
@RequiredArgsConstructor
public class ServerApiController {

    private final ServerService serverService;
    private final HealthMetricService healthMetricService;

    @Operation(summary = "List servers", description = "Returns paginated list of servers with optional status, environment, and search filters")
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

    @Operation(summary = "Get server by ID", description = "Returns a single server by its unique identifier")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Server found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Server not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ServerResponse>> getById(@PathVariable UUID id) {
        Server server = serverService.findById(id);
        return ResponseEntity.ok(ApiResponse.ok(ServerResponse.from(server)));
    }

    @Operation(summary = "Create server", description = "Creates a new server entry in the inventory")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Server created"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request body")
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<ServerResponse>> create(@Valid @RequestBody ServerCreateRequest request) {
        Server server = serverService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(ServerResponse.from(server), "Server created successfully"));
    }

    @Operation(summary = "Update server", description = "Updates an existing server by ID")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Server updated"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Server not found")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<ServerResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody ServerCreateRequest request) {
        Server server = serverService.update(id, request);
        return ResponseEntity.ok(ApiResponse.ok(ServerResponse.from(server), "Server updated successfully"));
    }

    @Operation(summary = "Delete server", description = "Deletes a server by ID (admin only)")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Server deleted"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Server not found")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        serverService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Server deleted successfully"));
    }

    @Operation(summary = "Get server metrics", description = "Returns health metrics for a server within an optional time range")
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

    @Operation(summary = "Get latest metric", description = "Returns the most recent health metric for a server")
    @GetMapping("/{id}/metrics/latest")
    public ResponseEntity<ApiResponse<HealthMetric>> getLatestMetric(@PathVariable UUID id) {
        HealthMetric metric = healthMetricService.getLatest(id);
        return ResponseEntity.ok(ApiResponse.ok(metric));
    }
}
