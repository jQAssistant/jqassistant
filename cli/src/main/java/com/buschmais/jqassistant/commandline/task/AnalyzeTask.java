package com.buschmais.jqassistant.commandline.task;

import java.io.*;
import java.util.*;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buschmais.jqassistant.commandline.CliConfigurationException;
import com.buschmais.jqassistant.commandline.CliExecutionException;
import com.buschmais.jqassistant.commandline.CliRuleViolationException;
import com.buschmais.jqassistant.core.analysis.api.Analyzer;
import com.buschmais.jqassistant.core.analysis.api.AnalyzerConfiguration;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleException;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleSet;
import com.buschmais.jqassistant.core.analysis.api.rule.Severity;
import com.buschmais.jqassistant.core.analysis.impl.AnalyzerImpl;
import com.buschmais.jqassistant.core.plugin.api.PluginRepositoryException;
import com.buschmais.jqassistant.core.plugin.api.ReportPluginRepository;
import com.buschmais.jqassistant.core.report.api.ReportException;
import com.buschmais.jqassistant.core.report.api.ReportHelper;
import com.buschmais.jqassistant.core.report.api.ReportPlugin;
import com.buschmais.jqassistant.core.report.impl.CompositeReportPlugin;
import com.buschmais.jqassistant.core.report.impl.InMemoryReportWriter;
import com.buschmais.jqassistant.core.report.impl.XmlReportWriter;
import com.buschmais.jqassistant.core.rule.api.reader.RuleConfiguration;
import com.buschmais.jqassistant.core.store.api.Store;

/**
 * @author jn4, Kontext E GmbH, 24.01.14
 */
public class AnalyzeTask extends AbstractAnalyzeTask {

    @Deprecated
    private static final String CMDLINE_OPTION_SEVERITY = "severity";
    private static final String CMDLINE_OPTION_FAIL_ON_SEVERITY = "failOnSeverity";
    private static final String CMDLINE_OPTION_WARN_ON_SEVERITY = "warnOnSeverity";
    private static final String CMDLINE_OPTION_RULEPARAMETERS = "ruleParameters";
    private static final String CMDLINE_OPTION_EXECUTEAPPLIEDCONCEPTS = "executeAppliedConcepts";

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractAnalyzeTask.class);

    private File ruleParametersFile;
    private String reportDirectory;
    private Severity failOnSeverity;
    private Severity warnOnSeverity;
    private boolean executeAppliedConcepts;

    @Override
    protected void executeTask(final Store store) throws CliExecutionException {
        LOGGER.info("Will warn on violation of constraints starting form severity '" + warnOnSeverity + "'");
        LOGGER.info("Will fail on violation of constraints starting from severity '" + failOnSeverity + "'.");
        LOGGER.info("Executing analysis.");

        Writer xmlReportFileWriter;
        try {
            xmlReportFileWriter = new OutputStreamWriter(new FileOutputStream(getXmlReportFile()), XmlReportWriter.ENCODING);
        } catch (IOException e) {
            throw new RuntimeException("Cannot create XML report file.", e);
        }
        XmlReportWriter xmlReportWriter;
        try {
            xmlReportWriter = new XmlReportWriter(xmlReportFileWriter);
        } catch (ReportException e) {
            throw new RuntimeException("Cannot create XML report file writer.", e);
        }
        Map<String, ReportPlugin> reportWriters = new HashMap<>();
        reportWriters.put(XmlReportWriter.TYPE, xmlReportWriter);
        reportWriters.putAll(getReportPlugins());
        CompositeReportPlugin reportWriter = new CompositeReportPlugin(reportWriters);
        InMemoryReportWriter inMemoryReportWriter = new InMemoryReportWriter(reportWriter);
        AnalyzerConfiguration configuration = new AnalyzerConfiguration();
        configuration.setExecuteAppliedConcepts(executeAppliedConcepts);
        Map<String, String> ruleParameters = getRuleParameters();
        try {
            Analyzer analyzer = new AnalyzerImpl(configuration, store, inMemoryReportWriter, LOGGER);
            RuleSet availableRules = getAvailableRules();
            analyzer.execute(availableRules, getRuleSelection(availableRules), ruleParameters);
        } catch (RuleException e) {
            throw new CliExecutionException("Analysis failed.", e);
        } finally {
            IOUtils.closeQuietly(xmlReportFileWriter);
        }
        store.beginTransaction();
        LOGGER.info("Verifying results: failOnSeverity=" + failOnSeverity + ", warnOnSeverity=" + warnOnSeverity);
        try {
            final ReportHelper reportHelper = new ReportHelper(LOGGER);
            final int conceptViolations = reportHelper.verifyConceptResults(warnOnSeverity, failOnSeverity, inMemoryReportWriter);
            final int constraintViolations = reportHelper.verifyConstraintResults(warnOnSeverity, failOnSeverity, inMemoryReportWriter);
            if (conceptViolations > 0 || constraintViolations > 0) {
                throw new CliRuleViolationException("Failed rules detected: " + conceptViolations + " concepts, " + constraintViolations + " constraints");
            }
        } finally {
            store.commitTransaction();
        }
    }

    /**
     * Reads the given rule parameters file.
     *
     * @return The map containing the rule parameters.
     * @throws CliExecutionException
     *             If the file cannot be read.
     */
    private Map<String, String> getRuleParameters() throws CliExecutionException {
        Map<String, String> ruleParameters;
        if (ruleParametersFile == null) {
            ruleParameters = Collections.emptyMap();
        } else {
            Properties properties = new Properties();
            try {
                properties.load(new FileInputStream(ruleParametersFile));
            } catch (IOException e) {
                throw new CliExecutionException("Cannot read rule parameters file '" + ruleParametersFile.getPath() + "'.");
            }
            ruleParameters = new TreeMap<>();
            for (String name : properties.stringPropertyNames()) {
                ruleParameters.put(name, properties.getProperty(name));
            }
        }
        return ruleParameters;
    }

    /**
     * Get all configured report plugins.
     *
     * @return The list of report plugins.
     * @throws CliExecutionException
     *             If the plugins cannot be loaded or configured.
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
        String ruleParametersFileName = getOptionValue(options, CMDLINE_OPTION_RULEPARAMETERS, null);
        if (ruleParametersFileName != null) {
            this.ruleParametersFile = new File(ruleParametersFileName);
            if (!this.ruleParametersFile.exists()) {
                throw new CliConfigurationException("Cannot find rule parameters file '" + ruleParametersFileName + "'.");
            }
        } else {
            this.ruleParametersFile = null;
        }
        reportDirectory = getOptionValue(options, CMDLINE_OPTION_REPORTDIR, DEFAULT_REPORT_DIRECTORY);
        String severityValue = getOptionValue(options, CMDLINE_OPTION_SEVERITY, null);
        if (severityValue != null) {
            Severity severity = getSeverity(severityValue);
            failOnSeverity = severity;
            LOGGER.warn("'" + CMDLINE_OPTION_SEVERITY + "' has been deprecated, please use '" + CMDLINE_OPTION_FAIL_ON_SEVERITY + "' instead.");
        } else {
            failOnSeverity = getSeverity(getOptionValue(options, CMDLINE_OPTION_FAIL_ON_SEVERITY, RuleConfiguration.DEFAULT.getDefaultConstraintSeverity().getValue()));
        }
        warnOnSeverity = getSeverity(getOptionValue(options, CMDLINE_OPTION_WARN_ON_SEVERITY, RuleConfiguration.DEFAULT.getDefaultConceptSeverity().getValue()));
        executeAppliedConcepts = options.hasOption(CMDLINE_OPTION_EXECUTEAPPLIEDCONCEPTS);
    }

    @Override
    protected void addTaskOptions(final List<Option> options) {
        super.addTaskOptions(options);
        options.add(OptionBuilder.withArgName(CMDLINE_OPTION_RULEPARAMETERS).withDescription("The name of a properties file providing rule parameters.")
                .hasArgs().create(CMDLINE_OPTION_RULEPARAMETERS));
        options.add(OptionBuilder.withArgName(CMDLINE_OPTION_REPORTDIR).withDescription("The directory for writing reports.").hasArgs()
                .create(CMDLINE_OPTION_REPORTDIR));
        options.add(OptionBuilder.withArgName(CMDLINE_OPTION_SEVERITY)
                .withDescription("The severity threshold to report a failure. Deprecated: please use " + CMDLINE_OPTION_FAIL_ON_SEVERITY + " instead.")
                .hasArgs().create(CMDLINE_OPTION_SEVERITY));
        options.add(OptionBuilder.withArgName(CMDLINE_OPTION_FAIL_ON_SEVERITY)
                .withDescription("The severity threshold to fail on rule violations, i.e. to exit with an error code.").hasArgs()
                .create(CMDLINE_OPTION_FAIL_ON_SEVERITY));
        options.add(OptionBuilder.withArgName(CMDLINE_OPTION_WARN_ON_SEVERITY)
                .withDescription("The severity threshold to warn on rule violations.").hasArgs().create(CMDLINE_OPTION_WARN_ON_SEVERITY));
        options.add(OptionBuilder.withArgName(CMDLINE_OPTION_EXECUTEAPPLIEDCONCEPTS)
                .withDescription("If set also execute concepts which have already been applied.").create(CMDLINE_OPTION_EXECUTEAPPLIEDCONCEPTS));
    }
}
