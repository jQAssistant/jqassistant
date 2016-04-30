package com.buschmais.jqassistant.scm.cli.task;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.core.analysis.api.*;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleSet;
import com.buschmais.jqassistant.core.analysis.api.rule.Severity;
import com.buschmais.jqassistant.core.analysis.impl.AnalyzerImpl;
import com.buschmais.jqassistant.core.plugin.api.PluginRepositoryException;
import com.buschmais.jqassistant.core.plugin.api.ReportPluginRepository;
import com.buschmais.jqassistant.core.report.api.ReportHelper;
import com.buschmais.jqassistant.core.report.api.ReportPlugin;
import com.buschmais.jqassistant.core.report.impl.CompositeReportWriter;
import com.buschmais.jqassistant.core.report.impl.InMemoryReportWriter;
import com.buschmais.jqassistant.core.report.impl.XmlReportWriter;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.scm.cli.CliConfigurationException;
import com.buschmais.jqassistant.scm.cli.CliExecutionException;
import com.buschmais.jqassistant.scm.cli.CliRuleViolationException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jn4, Kontext E GmbH, 24.01.14
 */
public class AnalyzeTask extends AbstractAnalyzeTask {

    private static final String CMDLINE_OPTION_SEVERITY = "severity";
    protected static final String CMDLINE_OPTION_EXECUTEAPPLIEDCONCEPTS = "executeAppliedConcepts";

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractAnalyzeTask.class);

    private String reportDirectory;
    private Severity severity;
    private boolean executeAppliedConcepts;

    @Override
    protected void executeTask(final Store store) throws CliExecutionException {
        LOGGER.info("Executing analysis.");
        FileWriter xmlReportFileWriter;
        try {
            xmlReportFileWriter = new FileWriter(getXmlReportFile());
        } catch (IOException e) {
            throw new RuntimeException("Cannot create XML report file.", e);
        }
        XmlReportWriter xmlReportWriter;
        try {
            xmlReportWriter = new XmlReportWriter(xmlReportFileWriter);
        } catch (AnalysisListenerException e) {
            throw new RuntimeException("Cannot create XML report file writer.", e);
        }
        Map<String, AnalysisListener> reportWriters = new HashMap<>();
        reportWriters.put(XmlReportWriter.TYPE, xmlReportWriter);
        reportWriters.putAll(getReportPlugins());
        CompositeReportWriter reportWriter = new CompositeReportWriter(reportWriters);
        InMemoryReportWriter inMemoryReportWriter = new InMemoryReportWriter(reportWriter);
        AnalyzerConfiguration configuration = new AnalyzerConfiguration();
        configuration.setExecuteAppliedConcepts(executeAppliedConcepts);
        try {
            Analyzer analyzer = new AnalyzerImpl(configuration, store, inMemoryReportWriter, LOGGER);
            RuleSet availableRules = getAvailableRules();
            analyzer.execute(availableRules, getRuleSelection(availableRules));
        } catch (AnalysisException e) {
            throw new CliExecutionException("Analysis failed.", e);
        } finally {
            IOUtils.closeQuietly(xmlReportFileWriter);
        }
        store.beginTransaction();
        LOGGER.info("Verifying results, severity=" + severity);
        try {
            final ReportHelper reportHelper = new ReportHelper(LOGGER);
            final int conceptViolations = reportHelper.verifyConceptResults(severity, inMemoryReportWriter);
            final int constraintViolations = reportHelper.verifyConstraintResults(severity, inMemoryReportWriter);
            if (conceptViolations > 0 || constraintViolations > 0) {
                throw new CliRuleViolationException("Violations detected: " + conceptViolations + " concepts, " + constraintViolations
                        + " constraints");
            }
        } finally {
            store.commitTransaction();
        }
    }

    /**
     * Get all configured report plugins.
     *
     * @return The list of report plugins.
     * @throws CliExecutionException If the plugins cannot be loaded or configured.
     */
    private Map<String, ReportPlugin> getReportPlugins() throws CliExecutionException {
        ReportPluginRepository reportPluginRepository;
        try {
            reportPluginRepository = pluginRepository.getReportPluginRepository();
            return reportPluginRepository.getReportPlugins(pluginProperties);
        } catch (PluginRepositoryException e) {
            throw new CliExecutionException("Cannot get report plugins.", e);
        }
    }

    /**
     * Returns the {@link java.io.File} to write the XML report to.
     *
     * @return The {@link java.io.File} to write the XML report to.
     */
    private File getXmlReportFile() {
        File reportFile = new File(reportDirectory, REPORT_FILE_XML);
        reportFile.getParentFile().mkdirs();
        return reportFile;
    }

    @Override
    public void withOptions(final CommandLine options) throws CliConfigurationException {
        super.withOptions(options);
        reportDirectory = getOptionValue(options, CMDLINE_OPTION_REPORTDIR, DEFAULT_REPORT_DIRECTORY);
        severity = Severity.valueOf(getOptionValue(options, CMDLINE_OPTION_SEVERITY, Severity.CRITICAL.name()).toUpperCase());
        executeAppliedConcepts = options.hasOption(CMDLINE_OPTION_EXECUTEAPPLIEDCONCEPTS);
    }

    @Override
    protected void addTaskOptions(final List<Option> options) {
        super.addTaskOptions(options);
        options.add(OptionBuilder.withArgName(CMDLINE_OPTION_REPORTDIR).withDescription("The directory for writing reports.").hasArgs().create(
                CMDLINE_OPTION_REPORTDIR));
        options.add(OptionBuilder.withArgName(CMDLINE_OPTION_SEVERITY).withDescription("The severity threshold to report a failure.").hasArgs()
                .create(CMDLINE_OPTION_SEVERITY));
        options.add(OptionBuilder.withArgName(CMDLINE_OPTION_EXECUTEAPPLIEDCONCEPTS).withDescription(
                "If set also execute concepts which have already been applied.").create(CMDLINE_OPTION_EXECUTEAPPLIEDCONCEPTS));
    }
}
