package com.personalfinance.strategy;

import com.personalfinance.model.Report;
import java.io.IOException;
import java.nio.file.Path;

public interface ReportExportStrategy { void export(Report report, Path outputPath) throws IOException; }
