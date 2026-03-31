package com.infrawatch.controller.web;

import com.infrawatch.model.report.ReportArchive;
import com.infrawatch.service.report.ReportGenerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@Controller
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportWebController {

    private final ReportGenerationService reportService;

    @GetMapping
    public String templates(Model model) {
        model.addAttribute("templates", reportService.getAllTemplates());
        return "reports/templates";
    }

    @GetMapping("/archives")
    public String archives(@PageableDefault(size = 20) Pageable pageable, Model model) {
        model.addAttribute("archives", reportService.getArchives(pageable));
        return "reports/archive";
    }

    @PostMapping("/generate")
    public String generateReport(@RequestParam UUID templateId, @RequestParam String format,
                                 RedirectAttributes redirectAttributes) {
        ReportArchive archive = reportService.generateReport(templateId, format);
        redirectAttributes.addFlashAttribute("message", "Report generated: " + archive.getReportName());
        return "redirect:/reports/archives";
    }

    @GetMapping("/archives/{id}/download")
    public ResponseEntity<byte[]> downloadArchive(@PathVariable UUID id) {
        ReportArchive archive = reportService.getArchiveById(id);
        byte[] content = reportService.getReportContent(id);

        String contentType = "EXCEL".equals(archive.getFormat())
                ? "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                : "application/octet-stream";
        String ext = "EXCEL".equals(archive.getFormat()) ? ".xlsx" : ".txt";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + archive.getReportName() + ext + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .contentLength(content.length)
                .body(content);
    }
}
