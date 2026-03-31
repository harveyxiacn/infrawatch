package com.infrawatch.controller.web;

import com.infrawatch.model.testing.TestExecution;
import com.infrawatch.service.testing.TestCaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@Controller
@RequestMapping("/tests")
@RequiredArgsConstructor
public class TestWebController {

    private final TestCaseService testCaseService;

    @GetMapping
    public String list(@RequestParam(required = false) String category,
                       @PageableDefault(size = 20) Pageable pageable,
                       Model model) {
        var page = category != null
                ? testCaseService.findByCategory(category, pageable)
                : testCaseService.findAll(pageable);
        model.addAttribute("testCases", page);
        return "tests/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable UUID id,
                         @PageableDefault(size = 20) Pageable pageable,
                         Model model) {
        model.addAttribute("testCase", testCaseService.findById(id));
        model.addAttribute("executions", testCaseService.getExecutions(id, pageable));
        return "tests/detail";
    }

    @PostMapping("/{id}/execute")
    public String executeTest(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        // Simulate a test execution
        TestExecution execution = TestExecution.builder()
                .result(new java.util.Random().nextInt(100) < 90 ? "PASS" : "FAIL")
                .actualOutput("Automated test execution at " + java.time.LocalDateTime.now())
                .durationMs(50 + new java.util.Random().nextInt(500))
                .executedBy("admin")
                .executedAt(java.time.LocalDateTime.now())
                .build();
        testCaseService.recordExecution(id, execution);
        redirectAttributes.addFlashAttribute("message", "Test executed: " + execution.getResult());
        return "redirect:/tests/" + id;
    }
}
