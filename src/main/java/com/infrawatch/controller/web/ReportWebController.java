package com.infrawatch.controller.web;

import com.infrawatch.service.report.ReportGenerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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
}
