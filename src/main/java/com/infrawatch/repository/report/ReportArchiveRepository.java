package com.infrawatch.repository.report;

import com.infrawatch.model.report.ReportArchive;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface ReportArchiveRepository extends JpaRepository<ReportArchive, UUID> {

    Page<ReportArchive> findByTemplateIdOrderByGeneratedAtDesc(UUID templateId, Pageable pageable);

    Page<ReportArchive> findAllByOrderByGeneratedAtDesc(Pageable pageable);

    void deleteByGeneratedAtBefore(LocalDateTime before);
}
