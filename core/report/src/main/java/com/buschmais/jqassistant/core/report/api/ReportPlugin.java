package com.buschmais.jqassistant.core.report.api;

import java.util.Map;

import com.buschmais.jqassistant.core.analysis.api.AnalysisListener;

/**
 * Defines the interface for report plugins.
 * 
 * A report plugin is a specialized
 * {@link com.buschmais.jqassistant.core.analysis.api.AnalysisListener}.
 */
public interface ReportPlugin extends AnalysisListener<ReportException> {

    /**
     * Initializes the plugin with the given properties.
     * 
     * @param properties
     *            The properties.
     * @throws ReportException
     *             If the plugin cannot be initialized.
     */
    void initialize(Map<String, Object> properties) throws ReportException;
}
