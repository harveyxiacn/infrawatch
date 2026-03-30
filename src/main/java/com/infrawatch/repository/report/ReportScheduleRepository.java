package com.infrawatch.repository.report;

import com.infrawatch.model.report.ReportSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReportScheduleRepository extends JpaRepository<ReportSchedule, UUID> {

    List<ReportSchedule> findByEnabledTrue();

    List<ReportSchedule> findByTemplateId(UUID templateId);
}
