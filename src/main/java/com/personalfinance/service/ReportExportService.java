package com.personalfinance.service;

import com.personalfinance.model.Report;
import com.personalfinance.strategy.ReportExportStrategy;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public final class ReportExportService {
    private ReportExportStrategy strategy;
    public ReportExportService(ReportExportStrategy strategy) { setStrategy(strategy); }
    public void setStrategy(ReportExportStrategy strategy) { this.strategy = Objects.requireNonNull(strategy); }
    public void exportReport(Report report, Path outputPath) throws IOException {
        Files.createDirectories(outputPath.toAbsolutePath().getParent());
        strategy.export(report, outputPath);
    }
}
