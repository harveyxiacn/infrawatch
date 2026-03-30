package com.infrawatch.service.backup;

import com.infrawatch.dto.request.DRPlanCreateRequest;
import com.infrawatch.exception.ResourceNotFoundException;
import com.infrawatch.model.backup.DRPlan;
import com.infrawatch.model.backup.DrillLog;
import com.infrawatch.repository.backup.DRPlanRepository;
import com.infrawatch.repository.backup.DrillLogRepository;
import com.infrawatch.service.auth.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class DRPlanService {

    private final DRPlanRepository drPlanRepository;
    private final DrillLogRepository drillLogRepository;
    private final AuditService auditService;

    public Page<DRPlan> findAll(Pageable pageable) {
        return drPlanRepository.findAll(pageable);
    }

    public DRPlan findById(UUID id) {
        return drPlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DRPlan", id));
    }

    public List<DRPlan> searchBySystemName(String systemName) {
        return drPlanRepository.findBySystemNameContaining(systemName);
    }

    public DRPlan create(DRPlanCreateRequest request) {
        DRPlan plan = DRPlan.builder()
                .systemName(request.getSystemName())
                .rtoMinutes(request.getRtoMinutes())
                .rpoMinutes(request.getRpoMinutes())
                .recoverySteps(request.getRecoverySteps())
                .dependencies(request.getDependencies())
                .responsibleTeam(request.getResponsibleTeam())
                .contactEmail(request.getContactEmail())
                .version(1)
                .lastReviewDate(LocalDate.now())
                .build();

        plan = drPlanRepository.save(plan);
        auditService.log("CREATE", "DRPlan", plan.getId(), "Created DR plan: " + plan.getSystemName());
        return plan;
    }

    public DRPlan update(UUID id, DRPlanCreateRequest request) {
        DRPlan plan = findById(id);

        plan.setSystemName(request.getSystemName());
        plan.setRtoMinutes(request.getRtoMinutes());
        plan.setRpoMinutes(request.getRpoMinutes());
        plan.setRecoverySteps(request.getRecoverySteps());
        plan.setDependencies(request.getDependencies());
        plan.setResponsibleTeam(request.getResponsibleTeam());
        plan.setContactEmail(request.getContactEmail());
        plan.setVersion(plan.getVersion() + 1);
        plan.setLastReviewDate(LocalDate.now());

        plan = drPlanRepository.save(plan);
        auditService.log("UPDATE", "DRPlan", plan.getId(), "Updated DR plan: " + plan.getSystemName());
        return plan;
    }

    public void delete(UUID id) {
        DRPlan plan = findById(id);
        drPlanRepository.delete(plan);
        auditService.log("DELETE", "DRPlan", id, "Deleted DR plan: " + plan.getSystemName());
    }

    public DrillLog addDrillLog(DrillLog drillLog) {
        drillLog = drillLogRepository.save(drillLog);
        auditService.log("CREATE", "DrillLog", drillLog.getId(),
                "Added drill log for plan: " + drillLog.getPlan().getSystemName());
        return drillLog;
    }

    public Page<DrillLog> findDrillsByPlanId(UUID planId, Pageable pageable) {
        return drillLogRepository.findByPlanId(planId, pageable);
    }

    public List<DrillLog> findDrillsByDateRange(LocalDate from, LocalDate to) {
        return drillLogRepository.findByDrillDateBetween(from, to);
    }

    public DrillLog findDrillById(UUID id) {
        return drillLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DrillLog", id));
    }
}
