package com.buschmais.jqassistant.core.analysis.spi;

import java.util.Collection;
import java.util.Map;

import com.buschmais.jqassistant.core.analysis.api.RuleInterpreterPlugin;
import com.buschmais.jqassistant.core.report.api.ReportContext;
import com.buschmais.jqassistant.core.report.api.ReportPlugin;
import com.buschmais.jqassistant.core.report.api.configuration.Report;
import com.buschmais.jqassistant.core.shared.lifecycle.LifecycleAware;

/**
 * Defines the interface for the analysis plugin repository.
 */
public interface AnalyzerPluginRepository extends LifecycleAware {

    /**
     * Return the {@link RuleInterpreterPlugin}s.
     *
     * @param properties
     *            The configuration properties.
     * @return The {@link RuleInterpreterPlugin}s.
     */
    Map<String, Collection<RuleInterpreterPlugin>> getRuleInterpreterPlugins(Map<String, Object> properties);

    /**
     * Return the instances of the configured report plugins.
     *
     * @param configuration
     *     The {@link Report} configuration.
     * @param reportContext
     *     The {@link ReportContext}.
     * @return The instances of the configured report plugins.
     */
    Map<String, ReportPlugin> getReportPlugins(Report configuration, ReportContext reportContext);

    @Override
    void initialize();

    void destroy();
}
