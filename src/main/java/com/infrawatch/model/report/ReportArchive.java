package com.infrawatch.model.report;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "report_archives")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportArchive {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private ReportTemplate template;

    @Column(name = "report_name", nullable = false)
    private String reportName;

    @Column(nullable = false, length = 10)
    private String format;

    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @Column(name = "parameters_json", columnDefinition = "TEXT")
    private String parametersJson;

    @Column(name = "generated_at", nullable = false)
    @Builder.Default
    private LocalDateTime generatedAt = LocalDateTime.now();

    @Column(name = "generated_by", length = 50)
    private String generatedBy;
}
