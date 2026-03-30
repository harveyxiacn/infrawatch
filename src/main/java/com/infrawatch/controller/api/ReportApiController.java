package com.infrawatch.controller.api;

import com.infrawatch.dto.request.ReportGenerateRequest;
import com.infrawatch.dto.response.ApiResponse;
import com.infrawatch.dto.response.PageResponse;
import com.infrawatch.model.report.ReportArchive;
import com.infrawatch.model.report.ReportTemplate;
import com.infrawatch.service.report.ReportGenerationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportApiController {

    private final ReportGenerationService reportService;

    @GetMapping("/templates")
    public ResponseEntity<ApiResponse<List<ReportTemplate>>> listTemplates() {
        return ResponseEntity.ok(ApiResponse.ok(reportService.getAllTemplates()));
    }

    @GetMapping("/templates/{id}")
    public ResponseEntity<ApiResponse<ReportTemplate>> getTemplate(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(reportService.getTemplateById(id)));
    }

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<ReportArchive>> generate(@Valid @RequestBody ReportGenerateRequest request) {
        ReportArchive archive = reportService.generateReport(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(archive, "Report generated successfully"));
    }

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
