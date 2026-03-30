package com.infrawatch.controller.web;

import com.infrawatch.model.server.Server;
import com.infrawatch.model.server.enums.Environment;
import com.infrawatch.model.server.enums.ServerStatus;
import com.infrawatch.service.server.HealthMetricService;
import com.infrawatch.service.server.ServerService;
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

import java.time.LocalDateTime;
import java.util.UUID;

@Controller
@RequestMapping("/servers")
@RequiredArgsConstructor
public class ServerWebController {

    private final ServerService serverService;
    private final HealthMetricService healthMetricService;

    @GetMapping
    public String list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) ServerStatus status,
            @RequestParam(required = false) Environment environment,
            @PageableDefault(size = 20) Pageable pageable,
            Model model) {

        Page<Server> servers;
        if (search != null && !search.isBlank()) {
            servers = serverService.search(search, pageable);
        } else if (status != null) {
            servers = serverService.findByStatus(status, pageable);
        } else if (environment != null) {
            servers = serverService.findByEnvironment(environment, pageable);
        } else {
            servers = serverService.findAll(pageable);
        }

        model.addAttribute("servers", servers);
        model.addAttribute("statuses", ServerStatus.values());
        model.addAttribute("environments", Environment.values());
        model.addAttribute("search", search);
        model.addAttribute("currentStatus", status);
        model.addAttribute("currentEnvironment", environment);
        return "servers/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable UUID id, Model model) {
        Server server = serverService.findById(id);
        model.addAttribute("server", server);
        model.addAttribute("latestMetric", healthMetricService.getLatest(id));
        model.addAttribute("metrics", healthMetricService.getMetrics(id,
                LocalDateTime.now().minusDays(7), LocalDateTime.now()));
        return "servers/detail";
    }

    @GetMapping("/new")
    public String newServerForm(Model model) {
        model.addAttribute("server", new Server());
        model.addAttribute("statuses", ServerStatus.values());
        model.addAttribute("environments", Environment.values());
        return "servers/form";
    }

    @GetMapping("/{id}/edit")
    public String editServerForm(@PathVariable UUID id, Model model) {
        model.addAttribute("server", serverService.findById(id));
        model.addAttribute("statuses", ServerStatus.values());
        model.addAttribute("environments", Environment.values());
        return "servers/form";
    }
}
