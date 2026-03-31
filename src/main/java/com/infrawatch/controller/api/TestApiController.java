package com.infrawatch.controller.api;

import com.infrawatch.dto.response.ApiResponse;
import com.infrawatch.dto.response.PageResponse;
import com.infrawatch.model.testing.TestCase;
import com.infrawatch.model.testing.TestExecution;
import com.infrawatch.service.testing.TestCaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Testing", description = "Test case and execution management")
@RestController
@RequestMapping("/api/tests")
@RequiredArgsConstructor
public class TestApiController {

    private final TestCaseService testCaseService;

    @Operation(summary = "List test cases", description = "Returns paginated list of test cases with optional category filter")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<TestCase>>> list(
            @RequestParam(required = false) String category,
            @PageableDefault(size = 20) Pageable pageable) {
        var page = category != null
                ? testCaseService.findByCategory(category, pageable)
                : testCaseService.findAll(pageable);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(page)));
    }

    @Operation(summary = "Get test case by ID", description = "Returns a single test case by its unique identifier")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Test case found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Test case not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TestCase>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(testCaseService.findById(id)));
    }

    @Operation(summary = "Create test case", description = "Creates a new test case definition")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Test case created"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request body")
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<TestCase>> create(@RequestBody TestCase testCase) {
        TestCase created = testCaseService.create(testCase);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(created));
    }

    @Operation(summary = "List executions", description = "Returns paginated execution history for a specific test case")
    @GetMapping("/{id}/executions")
    public ResponseEntity<ApiResponse<PageResponse<TestExecution>>> getExecutions(
            @PathVariable UUID id,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(testCaseService.getExecutions(id, pageable))));
    }

    @Operation(summary = "Execute test case", description = "Records a new test execution result for a test case")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Test execution recorded"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Test case not found")
    })
    @PostMapping("/{id}/execute")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<TestExecution>> execute(
            @PathVariable UUID id,
            @RequestBody TestExecution execution) {
        TestExecution result = testCaseService.recordExecution(id, execution);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(result));
    }
}
