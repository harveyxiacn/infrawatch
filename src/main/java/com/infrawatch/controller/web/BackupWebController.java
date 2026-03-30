package com.infrawatch.controller.web;

import com.infrawatch.model.backup.BackupJob;
import com.infrawatch.model.backup.DRPlan;
import com.infrawatch.model.backup.enums.BackupType;
import com.infrawatch.service.backup.BackupService;
import com.infrawatch.service.backup.DRPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;

@Controller
@RequestMapping("/backup")
@RequiredArgsConstructor
public class BackupWebController {

    private final BackupService backupService;
    private final DRPlanService drPlanService;

    @GetMapping("/jobs")
    public String listJobs(@PageableDefault(size = 20) Pageable pageable, Model model) {
        Page<BackupJob> jobs = backupService.findAllJobs(pageable);
        model.addAttribute("jobs", jobs);
        model.addAttribute("backupTypes", BackupType.values());
        return "backup/jobs";
    }

    @GetMapping("/trend")
    public String trend(Model model) {
        model.addAttribute("trendData", backupService.getTrendData(30));
        return "backup/trend";
    }

    @GetMapping("/dr/plans")
    public String listPlans(@PageableDefault(size = 20) Pageable pageable, Model model) {
        Page<DRPlan> plans = drPlanService.findAll(pageable);
        model.addAttribute("plans", plans);
        return "backup/dr/plans";
    }

    @GetMapping("/dr/plans/{planId}/drills")
    public String listDrills(@PathVariable UUID planId,
                             @PageableDefault(size = 20) Pageable pageable,
                             Model model) {
        model.addAttribute("plan", drPlanService.findById(planId));
        model.addAttribute("drills", drPlanService.findDrillsByPlanId(planId, pageable));
        return "backup/dr/drills";
    }
}
