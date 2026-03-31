package com.infrawatch.service.report;

import com.infrawatch.dto.request.ReportGenerateRequest;
import com.infrawatch.exception.ReportGenerationException;
import com.infrawatch.exception.ResourceNotFoundException;
import com.infrawatch.model.report.ReportArchive;
import com.infrawatch.model.report.ReportTemplate;
import com.infrawatch.model.server.Server;
import com.infrawatch.model.virtualization.VirtualMachine;
import com.infrawatch.model.backup.BackupJob;
import com.infrawatch.model.migration.MigrationProject;
import com.infrawatch.repository.backup.BackupJobRepository;
import com.infrawatch.repository.migration.MigrationProjectRepository;
import com.infrawatch.repository.report.ReportArchiveRepository;
import com.infrawatch.repository.report.ReportTemplateRepository;
import com.infrawatch.repository.server.ServerRepository;
import com.infrawatch.repository.virtualization.VirtualMachineRepository;
import com.infrawatch.service.auth.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ReportGenerationService {

    private final ReportTemplateRepository templateRepository;
    private final ReportArchiveRepository archiveRepository;
    private final ServerRepository serverRepository;
    private final VirtualMachineRepository vmRepository;
    private final BackupJobRepository backupJobRepository;
    private final MigrationProjectRepository migrationProjectRepository;
    private final AuditService auditService;

    @Value("${infrawatch.reports.output-dir:./reports}")
    private String outputDir;

    public List<ReportTemplate> getAllTemplates() {
        return templateRepository.findAll();
    }

    public ReportTemplate getTemplateById(UUID id) {
        return templateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ReportTemplate", id));
    }

    public ReportArchive getArchiveById(UUID id) {
        return archiveRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ReportArchive", id));
    }

    public Page<ReportArchive> getArchives(Pageable pageable) {
        return archiveRepository.findAllByOrderByGeneratedAtDesc(pageable);
    }

    public Page<ReportArchive> getArchivesByTemplate(UUID templateId, Pageable pageable) {
        return archiveRepository.findByTemplateIdOrderByGeneratedAtDesc(templateId, pageable);
    }

    public ReportArchive generateReport(UUID templateId, String format) {
        ReportTemplate template = getTemplateById(templateId);

        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String ext = "EXCEL".equalsIgnoreCase(format) ? "xlsx" : "pdf";
            String fileName = template.getType() + "_" + timestamp + "." + ext;

            File dir = new File(outputDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            File reportFile = new File(dir, fileName);

            byte[] content;
            if ("EXCEL".equalsIgnoreCase(format)) {
                content = generateExcel(template);
            } else {
                content = generatePdfText(template);
            }

            try (FileOutputStream fos = new FileOutputStream(reportFile)) {
                fos.write(content);
            }

            ReportArchive archive = ReportArchive.builder()
                    .template(template)
                    .reportName(template.getName() + " - " + timestamp)
                    .format(format.toUpperCase())
                    .filePath(reportFile.getAbsolutePath())
                    .fileSizeBytes((long) content.length)
                    .generatedAt(LocalDateTime.now())
                    .build();

            archive = archiveRepository.save(archive);
            auditService.log("CREATE", "ReportArchive", archive.getId(),
                    "Generated report: " + archive.getReportName());

            return archive;

        } catch (IOException e) {
            throw new ReportGenerationException("Failed to generate report: " + e.getMessage(), e);
        }
    }

    public byte[] getReportContent(UUID archiveId) {
        ReportArchive archive = getArchiveById(archiveId);
        File file = new File(archive.getFilePath());
        if (!file.exists()) {
            throw new ResourceNotFoundException("Report file not found on disk: " + archive.getFilePath());
        }
        try (FileInputStream fis = new FileInputStream(file)) {
            return fis.readAllBytes();
        } catch (IOException e) {
            throw new ReportGenerationException("Failed to read report file", e);
        }
    }

    private byte[] generateExcel(ReportTemplate template) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 11);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            switch (template.getType()) {
                case "SERVER_HEALTH":
                case "SYSTEM_INVENTORY":
                    buildServerSheet(workbook, headerStyle);
                    break;
                case "VM_CAPACITY":
                    buildVmSheet(workbook, headerStyle);
                    break;
                case "BACKUP_COMPLIANCE":
                    buildBackupSheet(workbook, headerStyle);
                    break;
                case "MIGRATION_PROGRESS":
                    buildMigrationSheet(workbook, headerStyle);
                    break;
                default:
                    buildServerSheet(workbook, headerStyle);
                    break;
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    private void buildServerSheet(XSSFWorkbook workbook, CellStyle headerStyle) {
        Sheet sheet = workbook.createSheet("Servers");
        String[] headers = {"Hostname", "IP Address", "OS", "CPU Cores", "RAM (GB)", "Disk (GB)", "Status", "Environment", "Location"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        List<Server> servers = serverRepository.findAll();
        int rowIdx = 1;
        for (Server s : servers) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(s.getHostname());
            row.createCell(1).setCellValue(s.getIpAddress());
            row.createCell(2).setCellValue(s.getOs() != null ? s.getOs() : "");
            row.createCell(3).setCellValue(s.getCpuCores());
            row.createCell(4).setCellValue(s.getRamGb());
            row.createCell(5).setCellValue(s.getDiskGb());
            row.createCell(6).setCellValue(s.getStatus().name());
            row.createCell(7).setCellValue(s.getEnvironment().name());
            row.createCell(8).setCellValue(s.getLocation() != null ? s.getLocation() : "");
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void buildVmSheet(XSSFWorkbook workbook, CellStyle headerStyle) {
        Sheet sheet = workbook.createSheet("Virtual Machines");
        String[] headers = {"VM Name", "Guest OS", "vCPU", "vRAM (GB)", "vDisk (GB)", "Hypervisor", "Status"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        List<VirtualMachine> vms = vmRepository.findAll();
        int rowIdx = 1;
        for (VirtualMachine vm : vms) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(vm.getName());
            row.createCell(1).setCellValue(vm.getGuestOs() != null ? vm.getGuestOs() : "");
            row.createCell(2).setCellValue(vm.getVcpu());
            row.createCell(3).setCellValue(vm.getVramGb());
            row.createCell(4).setCellValue(vm.getVdiskGb());
            row.createCell(5).setCellValue(vm.getHypervisor().getHostname());
            row.createCell(6).setCellValue(vm.getStatus().name());
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void buildBackupSheet(XSSFWorkbook workbook, CellStyle headerStyle) {
        Sheet sheet = workbook.createSheet("Backup Jobs");
        String[] headers = {"Job Name", "Type", "Source", "Target", "Schedule", "Retention (days)", "Enabled"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        List<BackupJob> jobs = backupJobRepository.findAll();
        int rowIdx = 1;
        for (BackupJob j : jobs) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(j.getName());
            row.createCell(1).setCellValue(j.getType().name());
            row.createCell(2).setCellValue(j.getSourceSystem());
            row.createCell(3).setCellValue(j.getTargetLocation());
            row.createCell(4).setCellValue(j.getScheduleCron() != null ? j.getScheduleCron() : "");
            row.createCell(5).setCellValue(j.getRetentionDays());
            row.createCell(6).setCellValue(j.isEnabled() ? "Yes" : "No");
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void buildMigrationSheet(XSSFWorkbook workbook, CellStyle headerStyle) {
        Sheet sheet = workbook.createSheet("Migration Projects");
        String[] headers = {"Project", "Source", "Target", "Type", "Status", "Planned Start", "Planned End"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        List<MigrationProject> projects = migrationProjectRepository.findAll();
        int rowIdx = 1;
        for (MigrationProject p : projects) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(p.getName());
            row.createCell(1).setCellValue(p.getSourceSystem());
            row.createCell(2).setCellValue(p.getTargetSystem());
            row.createCell(3).setCellValue(p.getMigrationType());
            row.createCell(4).setCellValue(p.getStatus().name());
            row.createCell(5).setCellValue(p.getPlannedStartDate() != null ? p.getPlannedStartDate().toString() : "");
            row.createCell(6).setCellValue(p.getPlannedEndDate() != null ? p.getPlannedEndDate().toString() : "");
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private byte[] generatePdfText(ReportTemplate template) {
        // Simplified text-based PDF content (real JasperReports would go here)
        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════════════════\n");
        sb.append("  INFRAWATCH REPORT\n");
        sb.append("  ").append(template.getName()).append("\n");
        sb.append("═══════════════════════════════════════════════════\n\n");
        sb.append("Generated: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
        sb.append("Type: ").append(template.getType()).append("\n\n");

        switch (template.getType()) {
            case "SERVER_HEALTH":
            case "SYSTEM_INVENTORY":
                sb.append("SERVER INVENTORY\n");
                sb.append("─────────────────────────────────────────────────\n");
                List<Server> servers = serverRepository.findAll();
                sb.append(String.format("%-25s %-15s %-8s %-10s%n", "HOSTNAME", "IP", "STATUS", "ENV"));
                sb.append("─────────────────────────────────────────────────\n");
                for (Server s : servers) {
                    sb.append(String.format("%-25s %-15s %-8s %-10s%n", s.getHostname(), s.getIpAddress(), s.getStatus(), s.getEnvironment()));
                }
                sb.append("\nTotal: ").append(servers.size()).append(" servers\n");
                break;
            case "VM_CAPACITY":
                sb.append("VM CAPACITY REPORT\n");
                sb.append("─────────────────────────────────────────────────\n");
                List<VirtualMachine> vms = vmRepository.findAll();
                sb.append(String.format("%-25s %-6s %-8s %-8s %-10s%n", "VM NAME", "vCPU", "vRAM", "vDisk", "STATUS"));
                sb.append("─────────────────────────────────────────────────\n");
                for (VirtualMachine vm : vms) {
                    sb.append(String.format("%-25s %-6d %-8s %-8s %-10s%n", vm.getName(), vm.getVcpu(), vm.getVramGb()+"GB", vm.getVdiskGb()+"GB", vm.getStatus()));
                }
                sb.append("\nTotal: ").append(vms.size()).append(" VMs\n");
                break;
            default:
                sb.append("Report data for: ").append(template.getName()).append("\n");
                break;
        }

        sb.append("\n═══════════════════════════════════════════════════\n");
        sb.append("  Generated by InfraWatch Report Engine\n");
        sb.append("═══════════════════════════════════════════════════\n");
        return sb.toString().getBytes();
    }
}
