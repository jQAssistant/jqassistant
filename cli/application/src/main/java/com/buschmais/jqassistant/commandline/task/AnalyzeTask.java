package com.buschmais.jqassistant.commandline.task;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

import com.buschmais.jqassistant.commandline.CliConfigurationException;
import com.buschmais.jqassistant.commandline.CliExecutionException;
import com.buschmais.jqassistant.commandline.CliRuleViolationException;
import com.buschmais.jqassistant.core.analysis.api.Analyzer;
import com.buschmais.jqassistant.core.analysis.api.AnalyzerConfiguration;
import com.buschmais.jqassistant.core.analysis.impl.AnalyzerImpl;
import com.buschmais.jqassistant.core.analysis.spi.AnalyzerPluginRepository;
import com.buschmais.jqassistant.core.report.api.ReportContext;
import com.buschmais.jqassistant.core.report.api.ReportHelper;
import com.buschmais.jqassistant.core.report.api.ReportPlugin;
import com.buschmais.jqassistant.core.report.impl.CompositeReportPlugin;
import com.buschmais.jqassistant.core.report.impl.InMemoryReportPlugin;
import com.buschmais.jqassistant.core.report.impl.ReportContextImpl;
import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.core.rule.api.model.RuleSet;
import com.buschmais.jqassistant.core.rule.api.model.Severity;
import com.buschmais.jqassistant.core.rule.api.reader.RuleConfiguration;
import com.buschmais.jqassistant.core.store.api.Store;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jn4, Kontext E GmbH, 24.01.14
 */
public class AnalyzeTask extends AbstractAnalyzeTask {

    private static final String CMDLINE_OPTION_FAIL_ON_SEVERITY = "failOnSeverity";
    private static final String CMDLINE_OPTION_WARN_ON_SEVERITY = "warnOnSeverity";
    private static final String CMDLINE_OPTION_RULEPARAMETERS = "ruleParameters";
    private static final String CMDLINE_OPTION_EXECUTEAPPLIEDCONCEPTS = "executeAppliedConcepts";

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractAnalyzeTask.class);

    private File ruleParametersFile;
    private File outputDirectory;
    private Severity failOnSeverity;
    private Severity warnOnSeverity;
    private boolean executeAppliedConcepts;

    @Override
    protected void executeTask(final Store store) throws CliExecutionException {
        LOGGER.info("Will warn on violations starting form severity '" + warnOnSeverity + "'");
        LOGGER.info("Will fail on violations starting from severity '" + failOnSeverity + "'.");
        LOGGER.info("Executing analysis.");

        ReportContext reportContext = new ReportContextImpl(outputDirectory, outputDirectory);
        Map<String, ReportPlugin> reportPlugins = getReportPlugins(reportContext);
        InMemoryReportPlugin inMemoryReportPlugin = new InMemoryReportPlugin(new CompositeReportPlugin(reportPlugins));
        AnalyzerConfiguration configuration = new AnalyzerConfiguration();
        configuration.setExecuteAppliedConcepts(executeAppliedConcepts);
        Map<String, String> ruleParameters = getRuleParameters();
        try {
            Analyzer analyzer = new AnalyzerImpl(configuration, store, pluginRepository.getAnalyzerPluginRepository().getRuleInterpreterPlugins(Collections.<String, Object>emptyMap()), inMemoryReportPlugin, LOGGER);
            RuleSet availableRules = getAvailableRules();
            analyzer.execute(availableRules, getRuleSelection(availableRules), ruleParameters);
        } catch (RuleException e) {
            throw new CliExecutionException("Analysis failed.", e);
        }
        store.beginTransaction();
        LOGGER.info("Verifying results: failOnSeverity=" + failOnSeverity + ", warnOnSeverity=" + warnOnSeverity);
        try {
            final ReportHelper reportHelper = new ReportHelper(LOGGER);
            final int conceptViolations = reportHelper.verifyConceptResults(warnOnSeverity, failOnSeverity, inMemoryReportPlugin);
            final int constraintViolations = reportHelper.verifyConstraintResults(warnOnSeverity, failOnSeverity, inMemoryReportPlugin);
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
     * @param reportContext
     *            The ReportContext.
     * @return The list of report plugins.
     * @param reportContext
     */
    private Map<String, ReportPlugin> getReportPlugins(ReportContext reportContext) {
        AnalyzerPluginRepository analyzerPluginRepository;
        analyzerPluginRepository = pluginRepository.getAnalyzerPluginRepository();
        return analyzerPluginRepository.getReportPlugins(reportContext, pluginProperties);
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
        String reportDirectoryValue = getOptionValue(options, CMDLINE_OPTION_REPORTDIR, DEFAULT_REPORT_DIRECTORY);
        outputDirectory = new File(reportDirectoryValue);
        outputDirectory.mkdirs();
        failOnSeverity = getSeverity(
                getOptionValue(options, CMDLINE_OPTION_FAIL_ON_SEVERITY, RuleConfiguration.DEFAULT.getDefaultConstraintSeverity().getValue()));
        warnOnSeverity = getSeverity(
                getOptionValue(options, CMDLINE_OPTION_WARN_ON_SEVERITY, RuleConfiguration.DEFAULT.getDefaultConceptSeverity().getValue()));
        executeAppliedConcepts = options.hasOption(CMDLINE_OPTION_EXECUTEAPPLIEDCONCEPTS);
    }

    @Override
    public void addTaskOptions(final List<Option> options) {
        super.addTaskOptions(options);
        options.add(OptionBuilder.withArgName(CMDLINE_OPTION_RULEPARAMETERS).withDescription("The name of a properties file providing rule parameters.")
                .hasArgs().create(CMDLINE_OPTION_RULEPARAMETERS));
        options.add(OptionBuilder.withArgName(CMDLINE_OPTION_REPORTDIR).withDescription("The directory for writing reports.").hasArgs()
                .create(CMDLINE_OPTION_REPORTDIR));
        options.add(OptionBuilder.withArgName(CMDLINE_OPTION_FAIL_ON_SEVERITY)
                .withDescription("The severity threshold to fail on rule violations, i.e. to exit with an error code.").hasArgs()
                .create(CMDLINE_OPTION_FAIL_ON_SEVERITY));
        options.add(OptionBuilder.withArgName(CMDLINE_OPTION_WARN_ON_SEVERITY).withDescription("The severity threshold to warn on rule violations.").hasArgs()
                .create(CMDLINE_OPTION_WARN_ON_SEVERITY));
        options.add(OptionBuilder.withArgName(CMDLINE_OPTION_EXECUTEAPPLIEDCONCEPTS)
                .withDescription("If set also execute concepts which have already been applied.").create(CMDLINE_OPTION_EXECUTEAPPLIEDCONCEPTS));
    }
}
