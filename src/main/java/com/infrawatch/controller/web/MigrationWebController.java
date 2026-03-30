package com.infrawatch.controller.web;

import com.infrawatch.dto.response.MigrationProgressData;
import com.infrawatch.model.migration.MigrationProject;
import com.infrawatch.model.migration.MigrationTask;
import com.infrawatch.model.migration.MigrationValidation;
import com.infrawatch.model.migration.enums.MigrationStatus;
import com.infrawatch.model.migration.enums.ValidationType;
import com.infrawatch.service.migration.MigrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/migrations")
@RequiredArgsConstructor
public class MigrationWebController {

    private final MigrationService migrationService;

    @GetMapping
    public String list(
            @RequestParam(required = false) MigrationStatus status,
            @PageableDefault(size = 20) Pageable pageable,
            Model model) {

        Page<MigrationProject> projects;
        if (status != null) {
            projects = migrationService.findProjectsByStatus(status, pageable);
        } else {
            projects = migrationService.findAllProjects(pageable);
        }

        Map<UUID, MigrationProgressData> progressMap = new HashMap<>();
        for (MigrationProject project : projects.getContent()) {
            progressMap.put(project.getId(), migrationService.getProjectProgress(project.getId()));
        }

        model.addAttribute("projects", projects);
        model.addAttribute("progressMap", progressMap);
        model.addAttribute("statuses", MigrationStatus.values());
        model.addAttribute("currentStatus", status);
        return "migrations/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable UUID id, Model model) {
        MigrationProject project = migrationService.findProjectById(id);
        List<MigrationTask> tasks = migrationService.findTasksByProjectId(id);
        MigrationProgressData progress = migrationService.getProjectProgress(id);

        Map<UUID, List<MigrationValidation>> validationsMap = new HashMap<>();
        for (MigrationTask task : tasks) {
            validationsMap.put(task.getId(), migrationService.findValidationsByTaskId(task.getId()));
        }

        model.addAttribute("project", project);
        model.addAttribute("tasks", tasks);
        model.addAttribute("progress", progress);
        model.addAttribute("validationsMap", validationsMap);
        model.addAttribute("statuses", MigrationStatus.values());
        model.addAttribute("validationTypes", ValidationType.values());
        return "migrations/detail";
    }
}
