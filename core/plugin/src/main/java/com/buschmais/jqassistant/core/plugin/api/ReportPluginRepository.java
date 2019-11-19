package com.buschmais.jqassistant.core.plugin.api;

import java.util.Map;

import com.buschmais.jqassistant.core.report.api.ReportContext;
import com.buschmais.jqassistant.core.report.api.ReportPlugin;
import com.buschmais.jqassistant.core.shared.lifecycle.LifecycleAware;

/**
 * Defines the interface for the report plugin repository.
 */
public interface ReportPluginRepository extends LifecycleAware {

    /**
     * Return the instances of the configured report plugins.
     *
     * @param reportContext
     *            The {@link ReportContext}.
     * @param properties
     *            The report properties.
     * @return The instances of the configured report plugins.
     */
    Map<String, ReportPlugin> getReportPlugins(ReportContext reportContext, Map<String, Object> properties) ;

    @Override
    void initialize();

    void destroy();
}
