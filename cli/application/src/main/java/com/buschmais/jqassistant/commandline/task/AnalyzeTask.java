package com.buschmais.jqassistant.commandline.task;

import java.io.File;
import java.util.Map;

import com.buschmais.jqassistant.commandline.CliConfigurationException;
import com.buschmais.jqassistant.commandline.CliExecutionException;
import com.buschmais.jqassistant.commandline.CliRuleViolationException;
import com.buschmais.jqassistant.commandline.configuration.CliConfiguration;
import com.buschmais.jqassistant.core.analysis.api.Analyzer;
import com.buschmais.jqassistant.core.analysis.api.configuration.Analyze;
import com.buschmais.jqassistant.core.analysis.impl.AnalyzerImpl;
import com.buschmais.jqassistant.core.analysis.spi.AnalyzerPluginRepository;
import com.buschmais.jqassistant.core.report.api.ReportContext;
import com.buschmais.jqassistant.core.report.api.ReportException;
import com.buschmais.jqassistant.core.report.api.ReportHelper;
import com.buschmais.jqassistant.core.report.api.ReportPlugin;
import com.buschmais.jqassistant.core.report.api.configuration.Report;
import com.buschmais.jqassistant.core.report.impl.CompositeReportPlugin;
import com.buschmais.jqassistant.core.report.impl.InMemoryReportPlugin;
import com.buschmais.jqassistant.core.report.impl.ReportContextImpl;
import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.core.rule.api.model.RuleSet;
import com.buschmais.jqassistant.core.rule.api.model.Severity;

import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Collections.emptyMap;

/**
 * @author jn4, Kontext E GmbH, 24.01.14
 */
public class AnalyzeTask extends AbstractAnalyzeTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractAnalyzeTask.class);

    @Override
    public void run(CliConfiguration configuration, Options options) throws CliExecutionException {
        withStore(configuration, store -> {
            Analyze analyze = configuration.analyze();
            Report report = analyze
                .report();
            Severity.Threshold warnOnSeverity = report.warnOnSeverity();
            Severity.Threshold failOnSeverity = report.failOnSeverity();
            LOGGER.info("Will warn on violations starting from severity '" + warnOnSeverity + "'");
            LOGGER.info("Will fail on violations starting from severity '" + failOnSeverity + "'.");
            LOGGER.info("Executing analysis.");
            File reportDirectory = new File(report.directory()
                .orElse(DEFAULT_REPORT_DIRECTORY));
            ReportContext reportContext = new ReportContextImpl(pluginRepository.getClassLoader(), store, reportDirectory, reportDirectory);
            Map<String, ReportPlugin> reportPlugins = getReportPlugins(analyze
                .report(), reportContext);
            InMemoryReportPlugin inMemoryReportPlugin = new InMemoryReportPlugin(new CompositeReportPlugin(reportPlugins));
            try {
                Analyzer analyzer = new AnalyzerImpl(analyze, pluginRepository.getClassLoader(), store, pluginRepository.getAnalyzerPluginRepository()
                    .getRuleInterpreterPlugins(emptyMap()), inMemoryReportPlugin);
                RuleSet availableRules = getAvailableRules(analyze
                    .rule());
                analyzer.execute(availableRules, getRuleSelection(availableRules, analyze));
            } catch (RuleException e) {
                throw new CliExecutionException("Analysis failed.", e);
            }
            if (report.createArchive()) {
                createReportArchive(reportContext);
            }
            store.beginTransaction();
            LOGGER.info("Verifying results: failOnSeverity=" + failOnSeverity + ", warnOnSeverity=" + warnOnSeverity);
            try {
                final ReportHelper reportHelper = new ReportHelper(report, LOGGER);
                reportHelper.verify(inMemoryReportPlugin, message -> {
                    throw new CliRuleViolationException(message);
                });
            } finally {
                store.commitTransaction();
            }
        });


    }

    private void createReportArchive(ReportContext reportContext) throws CliConfigurationException {
        File reportArchive = null;
        try {
            reportArchive = reportContext.createReportArchive();
        } catch (ReportException e) {
            throw new CliConfigurationException("Cannot create report archive.", e);
        }
        LOGGER.info("Created report archive '{}'.", reportArchive.getAbsolutePath());
    }

    /**
     * Get all configured report plugins.
     *
     * @param configuration
     *     The {@link Report} configuration.
     * @param reportContext
     *     The ReportContext.
     * @return The list of report plugins.
     */
    private Map<String, ReportPlugin> getReportPlugins(Report configuration, ReportContext reportContext) {
        AnalyzerPluginRepository analyzerPluginRepository;
        analyzerPluginRepository = pluginRepository.getAnalyzerPluginRepository();
        return analyzerPluginRepository.getReportPlugins(configuration, reportContext);
    }
}
