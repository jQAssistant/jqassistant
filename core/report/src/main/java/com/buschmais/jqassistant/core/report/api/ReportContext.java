package com.buschmais.jqassistant.core.report.api;

import java.io.File;
import java.net.URL;
import java.util.List;

import com.buschmais.jqassistant.core.report.api.configuration.Build;
import com.buschmais.jqassistant.core.rule.api.model.ExecutableRule;
import com.buschmais.jqassistant.core.store.api.Store;

/**
 * Defines the interface for the report context.
 */
public interface ReportContext {

    String JQASSISTANT_REPORT_ARCHIVE = "jqassistant-report.zip";

    String REPORT_DIRECTORY = "report";

    /**
     * Return the {@link Build}.
     *
     * @return The {@link Build}.
     */
    Build getBuild();

    /**
     * Return the plugin {@link ClassLoader}.
     *
     * @return The plugin {@link ClassLoader}.
     */
    ClassLoader getClassLoader();

    /**
     * Return the {@link Store}.
     */
    Store getStore();

    /**
     * Return the directory relative to the directory where report files shall be
     * written to.
     *
     * @param path
     *     The path.
     * @return The report directory.
     */
    File getReportDirectory(String path);

    /**
     * Return the directory where report files shall be written to.
     *
     * @return The output directory.
     */
    File getOutputDirectory();

    /**
     * Add a report for a {@link ExecutableRule}.
     *
     * @param <E>
     *     The type of the {@link ExecutableRule}.
     * @param rule
     *     The rule.
     * @param reportType
     *     The {@link ReportType}.
     * @param url
     *     The {@link URL}.
     * @throws ReportException
     *     If a problem occurs.
     */
    <E extends ExecutableRule<?>> Report<E> addReport(String label, E rule, ReportType reportType, URL url);

    /**
     * Return all {@link Report}s for the given {@link ExecutableRule}.
     *
     * @param <E>
     *     The type of the {@link ExecutableRule}.
     * @param rule
     *     The {@link ExecutableRule}.
     * @return The {@link List} of available {@link Report}s.
     * @throws ReportException
     *     If a problem occurs.
     */
    <E extends ExecutableRule<?>> List<Report<?>> getReports(E rule);

    /**
     * Creates an archive containing the reports as a ZIP {@link File}.
     *
     * @return The archive {@link File}.
     */
    File createReportArchive() throws ReportException;

    /**
     * Defines supported report types.
     */
    enum ReportType {
        IMAGE,
        LINK;
    }

    /**
     * Defines the interface for a report created by a plugin.
     *
     * @param <E>
     *     The type of the {@link ExecutableRule} the report refers to.
     */
    interface Report<E extends ExecutableRule<?>> {

        String getLabel();

        E getRule();

        ReportType getReportType();

        URL getUrl();

    }
}
