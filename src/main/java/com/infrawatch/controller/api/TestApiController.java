package com.infrawatch.controller.api;

import com.infrawatch.dto.response.ApiResponse;
import com.infrawatch.dto.response.PageResponse;
import com.infrawatch.model.testing.TestCase;
import com.infrawatch.model.testing.TestExecution;
import com.infrawatch.service.testing.TestCaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/tests")
@RequiredArgsConstructor
public class TestApiController {

    private final TestCaseService testCaseService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<TestCase>>> list(
            @RequestParam(required = false) String category,
            @PageableDefault(size = 20) Pageable pageable) {
        var page = category != null
                ? testCaseService.findByCategory(category, pageable)
                : testCaseService.findAll(pageable);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(page)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TestCase>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(testCaseService.findById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<TestCase>> create(@RequestBody TestCase testCase) {
        TestCase created = testCaseService.create(testCase);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(created));
    }

    @GetMapping("/{id}/executions")
    public ResponseEntity<ApiResponse<PageResponse<TestExecution>>> getExecutions(
            @PathVariable UUID id,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(testCaseService.getExecutions(id, pageable))));
    }

    @PostMapping("/{id}/execute")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<TestExecution>> execute(
            @PathVariable UUID id,
            @RequestBody TestExecution execution) {
        TestExecution result = testCaseService.recordExecution(id, execution);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(result));
    }
}
