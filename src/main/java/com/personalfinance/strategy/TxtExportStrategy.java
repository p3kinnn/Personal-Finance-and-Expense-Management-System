package com.personalfinance.strategy;

import com.personalfinance.model.Report;
import com.personalfinance.util.ReportFormatter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class TxtExportStrategy implements ReportExportStrategy {
    @Override public void export(Report report, Path outputPath) throws IOException {
        Files.writeString(outputPath, ReportFormatter.format(report), StandardCharsets.UTF_8);
    }
}
