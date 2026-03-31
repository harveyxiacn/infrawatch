package com.infrawatch.controller.api;

import com.infrawatch.dto.request.ReportGenerateRequest;
import com.infrawatch.dto.response.ApiResponse;
import com.infrawatch.dto.response.PageResponse;
import com.infrawatch.model.report.ReportArchive;
import com.infrawatch.model.report.ReportTemplate;
import com.infrawatch.service.report.ReportGenerationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Reports", description = "Report template, generation, and archive management")
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportApiController {

    private final ReportGenerationService reportService;

    @Operation(summary = "List report templates", description = "Returns all available report templates")
    @GetMapping("/templates")
    public ResponseEntity<ApiResponse<List<ReportTemplate>>> listTemplates() {
        return ResponseEntity.ok(ApiResponse.ok(reportService.getAllTemplates()));
    }

    @Operation(summary = "Get report template by ID", description = "Returns a single report template by its unique identifier")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Template found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Template not found")
    })
    @GetMapping("/templates/{id}")
    public ResponseEntity<ApiResponse<ReportTemplate>> getTemplate(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(reportService.getTemplateById(id)));
    }

    @Operation(summary = "Generate report", description = "Generates a new report from a template in the specified format")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Report generated"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request body"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Template not found")
    })
    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<ReportArchive>> generate(@Valid @RequestBody ReportGenerateRequest request) {
        ReportArchive archive = reportService.generateReport(request.getTemplateId(), request.getFormat());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(archive, "Report generated successfully"));
    }

    @Operation(summary = "List report archives", description = "Returns paginated list of generated reports with optional template filter")
    @GetMapping("/archives")
    public ResponseEntity<ApiResponse<PageResponse<ReportArchive>>> listArchives(
            @RequestParam(required = false) UUID templateId,
            @PageableDefault(size = 20) Pageable pageable) {
        var page = templateId != null
                ? reportService.getArchivesByTemplate(templateId, pageable)
                : reportService.getArchives(pageable);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(page)));
    }
}
