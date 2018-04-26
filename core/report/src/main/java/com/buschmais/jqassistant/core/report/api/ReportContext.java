package com.buschmais.jqassistant.core.report.api;

import java.io.File;
import java.net.URL;
import java.util.List;

import com.buschmais.jqassistant.core.analysis.api.rule.ExecutableRule;

/**
 * Defines the interface for the report context.
 */
public interface ReportContext {

    /**
     * Return the directory relative to the directory where report files shall be
     * written to.
     *
     * @param path
     *            The path.
     * @return The report directory.
     */
    File getReportDirectory(String path);

    /**
     * Return the directory where report files shall be written to.
     *
     * @return The output directory.
     */
    File getReportDirectory();

    /**
     * Add a report for a {@link ExecutableRule}.
     *
     * @param <E>
     *            The type of the {@link ExecutableRule}.
     * @param rule
     *            The rule.
     * @param reportType
     *            The {@link ReportType}.
     * @param url
     *            The {@link URL}.
     * @throws ReportException
     *             If a problem occurs.
     */
    <E extends ExecutableRule<?>> Report<E> addReport(E rule, ReportType reportType, URL url) throws ReportException;

    /**
     * Return all {@link Report}s for the given {@link ExecutableRule}.
     *
     * @param <E>
     *            The type of the {@link ExecutableRule}.
     * @param rule
     *            The {@link ExecutableRule}.
     * @return The {@link List} of available {@link Report}s.
     * @throws ReportException
     *             If a problem occurs.
     */
    <E extends ExecutableRule<?>> List<Report<?>> getReports(E rule) throws ReportException;

    /**
     * Defines supported report types.
     */
    enum ReportType {
        IMAGE, LINK;
    }

    /**
     * Defines the interface for a report created by a plugin.
     *
     * @param <E>
     *            The type of the {@link ExecutableRule} the report refers to.
     */
    interface Report<E extends ExecutableRule<?>> {

        E getRule();

        ReportType getReportType();

        URL getUrl();

    }
}
