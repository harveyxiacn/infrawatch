package com.infrawatch.repository.testing;

import com.infrawatch.model.testing.TestCase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TestCaseRepository extends JpaRepository<TestCase, UUID> {

    Page<TestCase> findByCategory(String category, Pageable pageable);

    Page<TestCase> findByServerId(UUID serverId, Pageable pageable);

    List<TestCase> findByEnabledTrue();
}
