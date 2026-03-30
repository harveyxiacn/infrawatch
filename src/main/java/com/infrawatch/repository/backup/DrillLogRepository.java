package com.infrawatch.repository.backup;

import com.infrawatch.model.backup.DrillLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface DrillLogRepository extends JpaRepository<DrillLog, UUID> {

    Page<DrillLog> findByPlanId(UUID planId, Pageable pageable);

    List<DrillLog> findByDrillDateBetween(LocalDate from, LocalDate to);
}
