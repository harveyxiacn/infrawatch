package com.infrawatch.repository.backup;

import com.infrawatch.model.backup.DRPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DRPlanRepository extends JpaRepository<DRPlan, UUID> {

    List<DRPlan> findBySystemNameContaining(String systemName);
}
