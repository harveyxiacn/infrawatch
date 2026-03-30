package com.infrawatch.repository.report;

import com.infrawatch.model.report.ReportTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReportTemplateRepository extends JpaRepository<ReportTemplate, UUID> {

    Optional<ReportTemplate> findByName(String name);

    Optional<ReportTemplate> findByType(String type);
}
