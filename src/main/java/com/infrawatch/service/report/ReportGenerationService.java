package com.infrawatch.service.report;

import com.infrawatch.dto.request.ReportGenerateRequest;
import com.infrawatch.exception.ReportGenerationException;
import com.infrawatch.exception.ResourceNotFoundException;
import com.infrawatch.model.report.ReportArchive;
import com.infrawatch.model.report.ReportTemplate;
import com.infrawatch.repository.report.ReportArchiveRepository;
import com.infrawatch.repository.report.ReportTemplateRepository;
import com.infrawatch.service.auth.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ReportGenerationService {

    private final ReportTemplateRepository templateRepository;
    private final ReportArchiveRepository archiveRepository;
    private final AuditService auditService;

    @Value("${infrawatch.reports.output-dir:./reports}")
    private String outputDir;

    public List<ReportTemplate> getAllTemplates() {
        return templateRepository.findAll();
    }

    public ReportTemplate getTemplateById(UUID id) {
        return templateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ReportTemplate", id));
    }

    public Page<ReportArchive> getArchives(Pageable pageable) {
        return archiveRepository.findAllByOrderByGeneratedAtDesc(pageable);
    }

    public Page<ReportArchive> getArchivesByTemplate(UUID templateId, Pageable pageable) {
        return archiveRepository.findByTemplateIdOrderByGeneratedAtDesc(templateId, pageable);
    }

    public ReportArchive generateReport(ReportGenerateRequest request) {
        ReportTemplate template = getTemplateById(request.getTemplateId());

        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = template.getType() + "_" + timestamp + "." + request.getFormat().toLowerCase();

            File dir = new File(outputDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            File reportFile = new File(dir, fileName);

            // Generate placeholder report content
            byte[] content = generateReportContent(template, request);
            try (FileOutputStream fos = new FileOutputStream(reportFile)) {
                fos.write(content);
            }

            ReportArchive archive = ReportArchive.builder()
                    .template(template)
                    .reportName(template.getName() + " - " + timestamp)
                    .format(request.getFormat())
                    .filePath(reportFile.getAbsolutePath())
                    .fileSizeBytes(reportFile.length())
                    .generatedAt(LocalDateTime.now())
                    .build();

            archive = archiveRepository.save(archive);
            auditService.log("CREATE", "ReportArchive", archive.getId(),
                    "Generated report: " + archive.getReportName());

            return archive;

        } catch (IOException e) {
            throw new ReportGenerationException("Failed to generate report: " + e.getMessage(), e);
        }
    }

    private byte[] generateReportContent(ReportTemplate template, ReportGenerateRequest request) {
        // Placeholder — JasperReports/POI integration would go here
        String content = String.format("Report: %s\nType: %s\nGenerated: %s\nFormat: %s",
                template.getName(), template.getType(), LocalDateTime.now(), request.getFormat());
        return content.getBytes();
    }
}
