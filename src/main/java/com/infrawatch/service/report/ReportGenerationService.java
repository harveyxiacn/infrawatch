package com.infrawatch.service.report;

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
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
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
                content = generatePdf(template);
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
            XSSFFont headerFont = workbook.createFont();
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
        org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        List<Server> servers = serverRepository.findAll();
        int rowIdx = 1;
        for (Server s : servers) {
            org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowIdx++);
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
        org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        List<VirtualMachine> vms = vmRepository.findAll();
        int rowIdx = 1;
        for (VirtualMachine vm : vms) {
            org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowIdx++);
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
        org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        List<BackupJob> jobs = backupJobRepository.findAll();
        int rowIdx = 1;
        for (BackupJob j : jobs) {
            org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowIdx++);
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
        org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        List<MigrationProject> projects = migrationProjectRepository.findAll();
        int rowIdx = 1;
        for (MigrationProject p : projects) {
            org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowIdx++);
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

    private byte[] generatePdf(ReportTemplate template) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 36, 36, 54, 36);

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD, new Color(15, 52, 96));
            Font subtitleFont = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.GRAY);
            Font headerFont = new Font(Font.HELVETICA, 9, Font.BOLD, Color.WHITE);
            Font cellFont = new Font(Font.HELVETICA, 8, Font.NORMAL, Color.DARK_GRAY);
            Font footerFont = new Font(Font.HELVETICA, 8, Font.ITALIC, Color.GRAY);

            // Title
            Paragraph title = new Paragraph("InfraWatch Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph sub = new Paragraph(template.getName(), new Font(Font.HELVETICA, 13, Font.NORMAL, new Color(80, 80, 80)));
            sub.setAlignment(Element.ALIGN_CENTER);
            sub.setSpacingAfter(4);
            document.add(sub);

            Paragraph date = new Paragraph("Generated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), subtitleFont);
            date.setAlignment(Element.ALIGN_CENTER);
            date.setSpacingAfter(20);
            document.add(date);

            document.add(new Paragraph(" "));

            Color headerBg = new Color(15, 52, 96);

            switch (template.getType()) {
                case "SERVER_HEALTH":
                case "SYSTEM_INVENTORY": {
                    PdfPTable table = new PdfPTable(new float[]{3f, 2f, 2.5f, 1f, 1f, 1.5f, 1.5f});
                    table.setWidthPercentage(100);
                    for (String h : new String[]{"Hostname", "IP Address", "OS", "CPU", "RAM", "Status", "Environment"}) {
                        PdfPCell hc = new PdfPCell(new Phrase(h, headerFont));
                        hc.setBackgroundColor(headerBg);
                        hc.setPadding(6);
                        table.addCell(hc);
                    }
                    for (Server s : serverRepository.findAll()) {
                        table.addCell(new Phrase(s.getHostname(), cellFont));
                        table.addCell(new Phrase(s.getIpAddress(), cellFont));
                        table.addCell(new Phrase(s.getOs() != null ? s.getOs() : "", cellFont));
                        table.addCell(new Phrase(String.valueOf(s.getCpuCores()), cellFont));
                        table.addCell(new Phrase(s.getRamGb() + " GB", cellFont));
                        table.addCell(new Phrase(s.getStatus().name(), cellFont));
                        table.addCell(new Phrase(s.getEnvironment().name(), cellFont));
                    }
                    document.add(table);
                    break;
                }
                case "VM_CAPACITY": {
                    PdfPTable table = new PdfPTable(new float[]{3f, 2f, 1f, 1f, 1f, 3f, 1.5f});
                    table.setWidthPercentage(100);
                    for (String h : new String[]{"VM Name", "Guest OS", "vCPU", "vRAM", "vDisk", "Hypervisor", "Status"}) {
                        PdfPCell hc = new PdfPCell(new Phrase(h, headerFont));
                        hc.setBackgroundColor(headerBg);
                        hc.setPadding(6);
                        table.addCell(hc);
                    }
                    for (VirtualMachine vm : vmRepository.findAll()) {
                        table.addCell(new Phrase(vm.getName(), cellFont));
                        table.addCell(new Phrase(vm.getGuestOs() != null ? vm.getGuestOs() : "", cellFont));
                        table.addCell(new Phrase(String.valueOf(vm.getVcpu()), cellFont));
                        table.addCell(new Phrase(vm.getVramGb() + " GB", cellFont));
                        table.addCell(new Phrase(vm.getVdiskGb() + " GB", cellFont));
                        table.addCell(new Phrase(vm.getHypervisor().getHostname(), cellFont));
                        table.addCell(new Phrase(vm.getStatus().name(), cellFont));
                    }
                    document.add(table);
                    break;
                }
                case "BACKUP_COMPLIANCE": {
                    PdfPTable table = new PdfPTable(new float[]{3f, 1.5f, 3f, 3f, 2f, 1f});
                    table.setWidthPercentage(100);
                    for (String h : new String[]{"Job Name", "Type", "Source", "Target", "Schedule", "Enabled"}) {
                        PdfPCell hc = new PdfPCell(new Phrase(h, headerFont));
                        hc.setBackgroundColor(headerBg);
                        hc.setPadding(6);
                        table.addCell(hc);
                    }
                    for (BackupJob j : backupJobRepository.findAll()) {
                        table.addCell(new Phrase(j.getName(), cellFont));
                        table.addCell(new Phrase(j.getType().name(), cellFont));
                        table.addCell(new Phrase(j.getSourceSystem(), cellFont));
                        table.addCell(new Phrase(j.getTargetLocation(), cellFont));
                        table.addCell(new Phrase(j.getScheduleCron() != null ? j.getScheduleCron() : "", cellFont));
                        table.addCell(new Phrase(j.isEnabled() ? "Yes" : "No", cellFont));
                    }
                    document.add(table);
                    break;
                }
                case "MIGRATION_PROGRESS": {
                    PdfPTable table = new PdfPTable(new float[]{3f, 2f, 2f, 1.5f, 1.5f});
                    table.setWidthPercentage(100);
                    for (String h : new String[]{"Project", "Source", "Target", "Type", "Status"}) {
                        PdfPCell hc = new PdfPCell(new Phrase(h, headerFont));
                        hc.setBackgroundColor(headerBg);
                        hc.setPadding(6);
                        table.addCell(hc);
                    }
                    for (MigrationProject p : migrationProjectRepository.findAll()) {
                        table.addCell(new Phrase(p.getName(), cellFont));
                        table.addCell(new Phrase(p.getSourceSystem(), cellFont));
                        table.addCell(new Phrase(p.getTargetSystem(), cellFont));
                        table.addCell(new Phrase(p.getMigrationType(), cellFont));
                        table.addCell(new Phrase(p.getStatus().name(), cellFont));
                    }
                    document.add(table);
                    break;
                }
                default: {
                    document.add(new Paragraph("Report: " + template.getName(), cellFont));
                    break;
                }
            }

            document.add(new Paragraph(" "));
            Paragraph footer = new Paragraph("Generated by InfraWatch Report Engine", footerFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
        } catch (DocumentException e) {
            throw new ReportGenerationException("PDF generation failed", e);
        }

        return out.toByteArray();
    }
}
