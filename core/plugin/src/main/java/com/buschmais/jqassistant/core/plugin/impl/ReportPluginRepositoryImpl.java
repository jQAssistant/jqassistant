package com.buschmais.jqassistant.core.plugin.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.core.analysis.api.AnalysisListenerException;
import com.buschmais.jqassistant.core.plugin.api.PluginConfigurationReader;
import com.buschmais.jqassistant.core.plugin.api.PluginRepositoryException;
import com.buschmais.jqassistant.core.plugin.api.ReportPluginRepository;
import com.buschmais.jqassistant.core.plugin.schema.v1.JqassistantPlugin;
import com.buschmais.jqassistant.core.plugin.schema.v1.ReportType;
import com.buschmais.jqassistant.core.report.api.ReportPlugin;

/**
 * Report plugin repository implementation.
 */
public class ReportPluginRepositoryImpl extends AbstractPluginRepository implements ReportPluginRepository {

    private final List<ReportPlugin> reportPlugins;

    /**
     * Constructor.
     */
    public ReportPluginRepositoryImpl(PluginConfigurationReader pluginConfigurationReader, Map<String, Object> properties) throws PluginRepositoryException {
        super(pluginConfigurationReader);
        List<JqassistantPlugin> plugins = pluginConfigurationReader.getPlugins();
        this.reportPlugins = getReportPlugins(plugins, properties);
    }

    @Override
    public List<ReportPlugin> getReportPlugins() {
        return reportPlugins;
    }

    private List<ReportPlugin> getReportPlugins(List<JqassistantPlugin> plugins, Map<String, Object> properties) throws PluginRepositoryException {
        List<ReportPlugin> reportPlugins = new ArrayList<>();
        for (JqassistantPlugin plugin : plugins) {
            ReportType reportType = plugin.getReport();
            if (reportType != null) {
                for (String reportPluginName : reportType.getClazz()) {
                    ReportPlugin reportPlugin = createInstance(reportPluginName);
                    if (reportPlugin != null) {
                        try {
                            reportPlugin.initialize(new HashMap<>(properties));
                        } catch (AnalysisListenerException e) {
                            throw new PluginRepositoryException("Cannot initialize plugin " + reportPluginName, e);
                        }
                        reportPlugins.add(reportPlugin);
                    }
                }
            }
        }
        return reportPlugins;
    }

}
