package com.buschmais.jqassistant.core.plugin.api;

import java.util.Map;

import com.buschmais.jqassistant.core.report.api.ReportContext;
import com.buschmais.jqassistant.core.report.api.ReportPlugin;

/**
 * Defines the interface for the report plugin repository.
 */
public interface ReportPluginRepository {

    /**
     * Return the instances of the configured report plugins.
     *
     * @param reportContext
     *            The {@link ReportContext}.
     * @param properties
     *            The report properties.
     * @return The instances of the configured report plugins.
     * @throws PluginRepositoryException
     *             If the instances cannot be created.
     */
    Map<String, ReportPlugin> getReportPlugins(ReportContext reportContext, Map<String, Object> properties) throws PluginRepositoryException;

}
