package com.buschmais.jqassistant.core.plugin.api;

import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.core.report.api.ReportPlugin;

/**
 * Defines the interface for the scanner plugin repository.
 */
public interface ReportPluginRepository {

    /**
     * Return the instances of the configured report plugins.
     * 
     * @return The instances of the configured report plugins.
     * @throws PluginRepositoryException
     *             If the instances cannot be created.
     */
    Map<String, ReportPlugin> getReportPlugins(Map<String, Object> properties) throws PluginRepositoryException;

}
