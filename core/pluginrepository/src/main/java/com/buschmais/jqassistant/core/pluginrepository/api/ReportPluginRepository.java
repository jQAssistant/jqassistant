package com.buschmais.jqassistant.core.pluginrepository.api;

import java.util.List;

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
    List<ReportPlugin> getReportPlugins();

}
