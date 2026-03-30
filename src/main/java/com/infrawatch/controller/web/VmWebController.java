package com.infrawatch.controller.web;

import com.infrawatch.model.virtualization.Hypervisor;
import com.infrawatch.model.virtualization.VirtualMachine;
import com.infrawatch.model.virtualization.enums.VmStatus;
import com.infrawatch.service.virtualization.VirtualizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@Controller
@RequestMapping("/virtualization")
@RequiredArgsConstructor
public class VmWebController {

    private final VirtualizationService virtualizationService;

    @GetMapping
    public String hosts(
            @PageableDefault(size = 20) Pageable pageable,
            Model model) {
        Page<Hypervisor> hypervisors = virtualizationService.findAllHypervisors(pageable);
        model.addAttribute("hypervisors", hypervisors);
        return "virtualization/hosts";
    }

    @GetMapping("/vms")
    public String vms(
            @RequestParam(required = false) UUID hypervisorId,
            @RequestParam(required = false) VmStatus status,
            @PageableDefault(size = 20) Pageable pageable,
            Model model) {

        Page<VirtualMachine> vms;
        if (hypervisorId != null) {
            vms = virtualizationService.findVmsByHypervisor(hypervisorId, pageable);
        } else if (status != null) {
            vms = virtualizationService.findVmsByStatus(status, pageable);
        } else {
            vms = virtualizationService.findAllVms(pageable);
        }

        Page<Hypervisor> hypervisors = virtualizationService.findAllHypervisors(Pageable.unpaged());

        model.addAttribute("vms", vms);
        model.addAttribute("hypervisors", hypervisors);
        model.addAttribute("statuses", VmStatus.values());
        model.addAttribute("currentHypervisorId", hypervisorId);
        model.addAttribute("currentStatus", status);
        return "virtualization/vms";
    }
}
