package com.buschmais.jqassistant.core.report.api;

import java.io.File;

/**
 * Defines the interface for the report context.
 */
public interface ReportContext {

    /**
     * Return the directory where report files shall be written to.
     *
     * @return The report directory.
     */
    File getReportDirectory();

}
