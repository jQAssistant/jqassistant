package com.buschmais.jqassistant.core.plugin.impl;

import com.buschmais.jqassistant.core.plugin.api.PluginConfigurationReader;
import com.buschmais.jqassistant.core.plugin.api.PluginRepositoryException;
import com.buschmais.jqassistant.core.plugin.api.ReportPluginRepository;
import com.buschmais.jqassistant.core.plugin.schema.v1.IdClassType;
import com.buschmais.jqassistant.core.plugin.schema.v1.JqassistantPlugin;
import com.buschmais.jqassistant.core.plugin.schema.v1.ReportType;
import com.buschmais.jqassistant.core.report.api.ReportException;
import com.buschmais.jqassistant.core.report.api.ReportPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Report plugin repository implementation.
 */
public class ReportPluginRepositoryImpl extends AbstractPluginRepository implements ReportPluginRepository {

    private final Map<String, ReportPlugin> reportPlugins;

    /**
     * Constructor.
     */
    public ReportPluginRepositoryImpl(PluginConfigurationReader pluginConfigurationReader) throws PluginRepositoryException {
        super(pluginConfigurationReader);
        List<JqassistantPlugin> plugins = pluginConfigurationReader.getPlugins();
        this.reportPlugins = getReportPlugins(plugins);
    }

    @Override
    public Map<String, ReportPlugin> getReportPlugins(Map<String, Object> properties) throws PluginRepositoryException {
        for (ReportPlugin reportPlugin : reportPlugins.values()) {
            try {
                reportPlugin.configure(properties);
            } catch (ReportException e) {
                throw new PluginRepositoryException("Cannot configure report plugin " + reportPlugin, e);
            }
        }
        return reportPlugins;
    }

    private Map<String, ReportPlugin> getReportPlugins(List<JqassistantPlugin> plugins) throws PluginRepositoryException {
        Map<String, ReportPlugin> reportPlugins = new HashMap<>();
        for (JqassistantPlugin plugin : plugins) {
            ReportType reportType = plugin.getReport();
            if (reportType != null) {
                for (IdClassType classType : reportType.getClazz()) {
                    ReportPlugin reportPlugin = createInstance(classType.getValue());
                    if (reportPlugin != null) {
                        try {
                            reportPlugin.initialize();
                        } catch (ReportException e) {
                            throw new PluginRepositoryException("Cannot initialize report plugin " + reportPlugin, e);
                        }
                        String id = classType.getId();
                        if (id == null) {
                            id = reportPlugin.getClass().getSimpleName();
                        }
                        reportPlugins.put(id, reportPlugin);
                    }
                }
            }
        }
        return reportPlugins;
    }

}
