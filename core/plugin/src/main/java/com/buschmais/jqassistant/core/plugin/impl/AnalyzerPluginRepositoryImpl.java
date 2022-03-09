package com.buschmais.jqassistant.core.plugin.impl;

import java.util.*;

import com.buschmais.jqassistant.core.analysis.api.RuleInterpreterPlugin;
import com.buschmais.jqassistant.core.analysis.spi.AnalyzerPluginRepository;
import com.buschmais.jqassistant.core.plugin.api.PluginConfigurationReader;
import com.buschmais.jqassistant.core.plugin.api.PluginRepositoryException;
import com.buschmais.jqassistant.core.report.api.ReportContext;
import com.buschmais.jqassistant.core.report.api.ReportException;
import com.buschmais.jqassistant.core.report.api.ReportPlugin;
import com.buschmais.jqassistant.core.report.api.configuration.Report;

import org.jqassistant.schema.plugin.v1.IdClassListType;
import org.jqassistant.schema.plugin.v1.IdClassType;
import org.jqassistant.schema.plugin.v1.JqassistantPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Collections.unmodifiableMap;

/**
 * Report plugin repository implementation.
 */
public class AnalyzerPluginRepositoryImpl extends AbstractPluginRepository implements AnalyzerPluginRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnalyzerPluginRepositoryImpl.class);

    private final Map<String, ReportPlugin> reportPlugins = new HashMap<>();

    private final Map<String, Collection<RuleInterpreterPlugin>> ruleInterpreterPlugins = new HashMap<>();

    /**
     * Constructor.
     */
    public AnalyzerPluginRepositoryImpl(PluginConfigurationReader pluginConfigurationReader) {
        super(pluginConfigurationReader);
    }

    @Override
    public Map<String, ReportPlugin> getReportPlugins(Report configuration, ReportContext reportContext) {
        Map<String, Object> properties = unmodifiableMap(configuration.properties());
        for (ReportPlugin reportPlugin : reportPlugins.values()) {
            try {
                reportPlugin.configure(reportContext, properties);
            } catch (ReportException e) {
                throw new PluginRepositoryException("Cannot configure report plugin " + reportPlugin, e);
            }
        }
        return reportPlugins;
    }

    @Override
    public Map<String, Collection<RuleInterpreterPlugin>> getRuleInterpreterPlugins(Map<String, Object> properties) {
        for (Collection<RuleInterpreterPlugin> languagePlugins : ruleInterpreterPlugins.values()) {
            for (RuleInterpreterPlugin ruleInterpreterPlugin : languagePlugins) {
                ruleInterpreterPlugin.configure(properties);
            }
        }
        return ruleInterpreterPlugins;
    };

    @Override
    public void initialize() {
        for (JqassistantPlugin plugin : plugins) {
            IdClassListType reportTypes = plugin.getReport();
            initializeReportPlugins(reportTypes);
            initializeRuleInterpreterPlugins(plugin);
        }
    }

    private void initializeReportPlugins(IdClassListType reportTypes) {
        if (reportTypes != null) {
            for (IdClassType classType : reportTypes.getClazz()) {
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

    private void initializeRuleInterpreterPlugins(JqassistantPlugin plugin) {
        IdClassListType ruleInterpreters = plugin.getRuleInterpreter();
        if (ruleInterpreters != null) {
            for (IdClassType pluginType : ruleInterpreters.getClazz()) {
                RuleInterpreterPlugin ruleInterpreterPlugin = createInstance(pluginType.getValue());
                ruleInterpreterPlugin.initialize();
                for (String language : ruleInterpreterPlugin.getLanguages()) {
                    Collection<RuleInterpreterPlugin> plugins = ruleInterpreterPlugins.get(language.toLowerCase());
                    if (plugins == null) {
                        plugins = new ArrayList<>();
                        ruleInterpreterPlugins.put(language.toLowerCase(), plugins);
                    }
                    plugins.add(ruleInterpreterPlugin);
                }
            }
        }
    }

    @Override
    public void destroy() {
        reportPlugins.values().forEach(plugin -> {
            try {
                plugin.destroy();
            } catch (ReportException e) {
                LOGGER.warn("Cannot destroy plugin " + plugin, e);
            }
        });
        ruleInterpreterPlugins.values().stream().flatMap(plugins -> plugins.stream()).forEach(plugin -> plugin.destroy());
    }
}
