package com.infrawatch.repository.testing;

import com.infrawatch.model.testing.TestExecution;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TestExecutionRepository extends JpaRepository<TestExecution, UUID> {

    Page<TestExecution> findByTestCaseIdOrderByExecutedAtDesc(UUID testCaseId, Pageable pageable);

    @Query("SELECT COUNT(e) FROM TestExecution e WHERE e.result = :result")
    long countByResult(String result);
}
