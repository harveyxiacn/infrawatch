package com.infrawatch.service.migration;

import com.infrawatch.dto.request.MigrationProjectCreateRequest;
import com.infrawatch.dto.response.MigrationProgressData;
import com.infrawatch.exception.ResourceNotFoundException;
import com.infrawatch.model.migration.MigrationProject;
import com.infrawatch.model.migration.MigrationTask;
import com.infrawatch.model.migration.MigrationValidation;
import com.infrawatch.model.migration.enums.MigrationStatus;
import com.infrawatch.model.migration.enums.ValidationType;
import com.infrawatch.repository.migration.MigrationProjectRepository;
import com.infrawatch.repository.migration.MigrationTaskRepository;
import com.infrawatch.repository.migration.MigrationValidationRepository;
import com.infrawatch.service.auth.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class MigrationService {

    private final MigrationProjectRepository projectRepository;
    private final MigrationTaskRepository taskRepository;
    private final MigrationValidationRepository validationRepository;
    private final AuditService auditService;

    // ── Project CRUD ──

    public Page<MigrationProject> findAllProjects(Pageable pageable) {
        return projectRepository.findAll(pageable);
    }

    public Page<MigrationProject> findProjectsByStatus(MigrationStatus status, Pageable pageable) {
        return projectRepository.findByStatus(status, pageable);
    }

    public MigrationProject findProjectById(UUID id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MigrationProject", id));
    }

    public MigrationProject createProject(MigrationProjectCreateRequest request) {
        MigrationProject project = MigrationProject.builder()
                .name(request.getName())
                .description(request.getDescription())
                .sourceSystem(request.getSourceSystem())
                .targetSystem(request.getTargetSystem())
                .migrationType(request.getMigrationType())
                .plannedStartDate(request.getPlannedStartDate())
                .plannedEndDate(request.getPlannedEndDate())
                .build();

        project = projectRepository.save(project);
        auditService.log("CREATE", "MigrationProject", project.getId(),
                "Created migration project: " + project.getName());
        return project;
    }

    public MigrationProject updateProject(UUID id, MigrationProjectCreateRequest request) {
        MigrationProject project = findProjectById(id);

        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setSourceSystem(request.getSourceSystem());
        project.setTargetSystem(request.getTargetSystem());
        project.setMigrationType(request.getMigrationType());
        project.setPlannedStartDate(request.getPlannedStartDate());
        project.setPlannedEndDate(request.getPlannedEndDate());

        project = projectRepository.save(project);
        auditService.log("UPDATE", "MigrationProject", project.getId(),
                "Updated migration project: " + project.getName());
        return project;
    }

    public MigrationProject updateProjectStatus(UUID id, MigrationStatus status) {
        MigrationProject project = findProjectById(id);
        project.setStatus(status);
        project = projectRepository.save(project);
        auditService.log("UPDATE", "MigrationProject", project.getId(),
                "Updated migration project status to: " + status);
        return project;
    }

    public void deleteProject(UUID id) {
        MigrationProject project = findProjectById(id);
        projectRepository.delete(project);
        auditService.log("DELETE", "MigrationProject", id,
                "Deleted migration project: " + project.getName());
    }

    public MigrationProgressData getProjectProgress(UUID projectId) {
        MigrationProject project = findProjectById(projectId);
        List<MigrationTask> tasks = taskRepository.findByProjectIdOrderBySortOrderAsc(projectId);
        long totalTasks = tasks.size();
        long completedTasks = countTasksByStatus(projectId, "COMPLETED");
        long failedTasks = countTasksByStatus(projectId, "FAILED");
        double progressPercent = totalTasks > 0 ? (completedTasks * 100.0) / totalTasks : 0.0;

        return MigrationProgressData.builder()
                .projectId(projectId)
                .projectName(project.getName())
                .totalTasks(totalTasks)
                .completedTasks(completedTasks)
                .failedTasks(failedTasks)
                .progressPercent(Math.round(progressPercent * 100.0) / 100.0)
                .build();
    }

    // ── Task CRUD ──

    public List<MigrationTask> findTasksByProjectId(UUID projectId) {
        return taskRepository.findByProjectIdOrderBySortOrderAsc(projectId);
    }

    public MigrationTask findTaskById(UUID id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MigrationTask", id));
    }

    public MigrationTask createTask(UUID projectId, MigrationTask task) {
        MigrationProject project = findProjectById(projectId);
        task.setProject(project);
        task = taskRepository.save(task);
        auditService.log("CREATE", "MigrationTask", task.getId(),
                "Created migration task: " + task.getDatasetName());
        return task;
    }

    public MigrationTask updateTask(UUID id, MigrationTask updates) {
        MigrationTask task = findTaskById(id);
        task.setDatasetName(updates.getDatasetName());
        task.setSourceTable(updates.getSourceTable());
        task.setTargetTable(updates.getTargetTable());
        task.setExpectedRowCount(updates.getExpectedRowCount());
        task.setActualRowCount(updates.getActualRowCount());
        task.setTransformationRules(updates.getTransformationRules());
        task.setStatus(updates.getStatus());
        task.setSortOrder(updates.getSortOrder());
        task.setNotes(updates.getNotes());

        task = taskRepository.save(task);
        auditService.log("UPDATE", "MigrationTask", task.getId(),
                "Updated migration task: " + task.getDatasetName());
        return task;
    }

    public void deleteTask(UUID id) {
        MigrationTask task = findTaskById(id);
        taskRepository.delete(task);
        auditService.log("DELETE", "MigrationTask", id,
                "Deleted migration task: " + task.getDatasetName());
    }

    public long countTasksByStatus(UUID projectId, String status) {
        return taskRepository.countByProjectIdAndStatus(projectId, status);
    }

    // ── Validations ──

    public List<MigrationValidation> findValidationsByTaskId(UUID taskId) {
        return validationRepository.findByTaskId(taskId);
    }

    public MigrationValidation recordValidation(UUID taskId, ValidationType validationType,
                                                  String sourceValue, String targetValue,
                                                  boolean passed, String notes) {
        MigrationTask task = findTaskById(taskId);

        MigrationValidation validation = MigrationValidation.builder()
                .task(task)
                .validationType(validationType)
                .sourceValue(sourceValue)
                .targetValue(targetValue)
                .passed(passed)
                .notes(notes)
                .executedAt(LocalDateTime.now())
                .build();

        validation = validationRepository.save(validation);
        auditService.log("CREATE", "MigrationValidation", validation.getId(),
                "Recorded validation for task: " + task.getDatasetName()
                        + " type: " + validationType + " passed: " + passed);
        return validation;
    }
}
