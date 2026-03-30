package com.infrawatch.service.testing;

import com.infrawatch.exception.ResourceNotFoundException;
import com.infrawatch.model.testing.TestCase;
import com.infrawatch.model.testing.TestExecution;
import com.infrawatch.repository.testing.TestCaseRepository;
import com.infrawatch.repository.testing.TestExecutionRepository;
import com.infrawatch.service.auth.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class TestCaseService {

    private final TestCaseRepository testCaseRepository;
    private final TestExecutionRepository testExecutionRepository;
    private final AuditService auditService;

    public Page<TestCase> findAll(Pageable pageable) {
        return testCaseRepository.findAll(pageable);
    }

    public TestCase findById(UUID id) {
        return testCaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TestCase", id));
    }

    public Page<TestCase> findByCategory(String category, Pageable pageable) {
        return testCaseRepository.findByCategory(category, pageable);
    }

    public List<TestCase> findEnabled() {
        return testCaseRepository.findByEnabledTrue();
    }

    public TestCase create(TestCase testCase) {
        testCase = testCaseRepository.save(testCase);
        auditService.log("CREATE", "TestCase", testCase.getId(), "Created test case: " + testCase.getName());
        return testCase;
    }

    public TestExecution recordExecution(UUID testCaseId, TestExecution execution) {
        TestCase testCase = findById(testCaseId);
        execution.setTestCase(testCase);
        return testExecutionRepository.save(execution);
    }

    public Page<TestExecution> getExecutions(UUID testCaseId, Pageable pageable) {
        return testExecutionRepository.findByTestCaseIdOrderByExecutedAtDesc(testCaseId, pageable);
    }

    public long countByResult(String result) {
        return testExecutionRepository.countByResult(result);
    }
}
