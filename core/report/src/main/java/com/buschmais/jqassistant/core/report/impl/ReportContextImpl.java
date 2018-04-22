package com.buschmais.jqassistant.core.report.impl;

import java.io.File;

import com.buschmais.jqassistant.core.report.api.ReportContext;

/**
 * Implementation of the {@link ReportContext}.
 */
public class ReportContextImpl implements ReportContext {

    private final File reportDirectory;

    /**
     * Constructor.
     *
     * @param reportDirectory
     *            The report directory.
     */
    public ReportContextImpl(File reportDirectory) {
        this.reportDirectory = reportDirectory;
    }

    @Override
    public File getReportDirectory() {
        return reportDirectory;
    }
}
