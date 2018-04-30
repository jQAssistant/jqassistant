package com.buschmais.jqassistant.core.report.impl;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.core.analysis.api.rule.ExecutableRule;
import com.buschmais.jqassistant.core.report.api.ReportContext;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static lombok.AccessLevel.PRIVATE;

/**
 * Implementation of the {@link ReportContext}.
 */
public class ReportContextImpl implements ReportContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportContextImpl.class);

    private final File outputDirectory;

    private final File reportDirectory;

    private final Map<String, List<Report<?>>> reports = new HashMap<>();

    /**
     * Constructor.
     *
     * @param outputDirectory
     *            The output directory.
     */
    public ReportContextImpl(File outputDirectory) {
        this(outputDirectory, new File(outputDirectory, REPORT_DIRECTORY));
    }

    /**
     * Constructor.
     *
     * @param outputDirectory
     *            The output directory.
     */
    public ReportContextImpl(File outputDirectory, File reportDirectory) {
        this.outputDirectory = outputDirectory;
        this.reportDirectory = reportDirectory;
    }

    @Override
    public File getReportDirectory(String path) {
        File directory = new File(reportDirectory, path);
        if (directory.mkdirs()) {
            LOGGER.info("Created report directory '{}.'", directory.getAbsolutePath());
        }
        return directory;
    }

    @Override
    public File getOutputDirectory() {
        return outputDirectory;
    }

    @Override
    public <E extends ExecutableRule<?>> Report<E> addReport(String label, E rule, ReportType reportType, URL url) {
        Report<E> report = ReportImpl.<E> builder().label(label).rule(rule).reportType(reportType).url(url).build();
        getReports(rule).add(report);
        return report;
    }

    @Override
    public <E extends ExecutableRule<?>> List<Report<?>> getReports(E rule) {
        List<Report<?>> ruleReports = reports.get(rule.getId());
        if (ruleReports == null) {
            ruleReports = new ArrayList<>();
            reports.put(rule.getId(), ruleReports);
        }
        return ruleReports;
    }

    @Getter
    @Builder
    @AllArgsConstructor(access = PRIVATE)
    @ToString
    private static final class ReportImpl<E extends ExecutableRule<?>> implements Report<E> {

        private String label;

        private E rule;

        private ReportType reportType;

        private URL url;

    }
}
