package com.buschmais.jqassistant.commandline.task;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.buschmais.jqassistant.commandline.CliConfigurationException;
import com.buschmais.jqassistant.commandline.CliExecutionException;
import com.buschmais.jqassistant.commandline.CliRuleViolationException;
import com.buschmais.jqassistant.commandline.configuration.CliConfiguration;
import com.buschmais.jqassistant.core.analysis.api.Analyzer;
import com.buschmais.jqassistant.core.analysis.api.configuration.Analyze;
import com.buschmais.jqassistant.core.analysis.impl.AnalyzerImpl;
import com.buschmais.jqassistant.core.analysis.spi.AnalyzerPluginRepository;
import com.buschmais.jqassistant.core.configuration.api.ConfigurationBuilder;
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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Collections.emptyMap;

/**
 * @author jn4, Kontext E GmbH, 24.01.14
 */
public class AnalyzeTask extends AbstractAnalyzeTask {

    private static final String CMDLINE_OPTION_WARN_ON_SEVERITY = "warnOnSeverity";
    private static final String CMDLINE_OPTION_FAIL_ON_SEVERITY = "failOnSeverity";
    private static final String CMDLINE_OPTION_CONTINUE_ON_FAILURE = "continueOnFailure";
    private static final String CMDLINE_OPTION_RULE_PARAMETERS = "ruleParameters";
    private static final String CMDLINE_OPTION_EXECUTE_APPLIED_CONCEPTS = "executeAppliedConcepts";
    private static final String CMDLINE_OPTION_CREATE_REPORT_ARCHIVE = "createReportArchive";

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractAnalyzeTask.class);

    private File reportDirectory;

    @Override
    public void run(CliConfiguration configuration) throws CliExecutionException {
        Analyze analyze = configuration.analyze();
        Severity.Threshold warnOnSeverity = analyze.report()
            .warnOnSeverity();
        Severity.Threshold failOnSeverity = analyze.report()
            .failOnSeverity();
        LOGGER.info("Will warn on violations starting from severity '" + warnOnSeverity + "'");
        LOGGER.info("Will fail on violations starting from severity '" + failOnSeverity + "'.");
        LOGGER.info("Executing analysis.");
        withStore(configuration, store -> {
            ReportContext reportContext = new ReportContextImpl(pluginRepository.getClassLoader(), store, reportDirectory, reportDirectory);
            Map<String, ReportPlugin> reportPlugins = getReportPlugins(analyze.report(), reportContext);
            InMemoryReportPlugin inMemoryReportPlugin = new InMemoryReportPlugin(new CompositeReportPlugin(reportPlugins));
            try {
                Analyzer analyzer = new AnalyzerImpl(analyze, pluginRepository.getClassLoader(), store, pluginRepository.getAnalyzerPluginRepository()
                    .getRuleInterpreterPlugins(emptyMap()), inMemoryReportPlugin, LOGGER);
                RuleSet availableRules = getAvailableRules(analyze.rule());
                analyzer.execute(availableRules, getRuleSelection(availableRules, analyze));
            } catch (RuleException e) {
                throw new CliExecutionException("Analysis failed.", e);
            }
            if (analyze.report()
                .createArchive()) {
                createReportArchive(reportContext);
            }
            store.beginTransaction();
            LOGGER.info("Verifying results: failOnSeverity=" + failOnSeverity + ", warnOnSeverity=" + warnOnSeverity);
            try {
                final ReportHelper reportHelper = new ReportHelper(configuration.analyze()
                    .report(), LOGGER);
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
     * Reads the given rule parameters file.
     *
     * @param configurationBuilder
     *     The {@link ConfigurationBuilder}.
     * @throws CliExecutionException
     *     If the file cannot be read.
     */
    private void loadRuleParameters(String ruleParametersFile, ConfigurationBuilder configurationBuilder) throws CliConfigurationException {
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(ruleParametersFile));
        } catch (IOException e) {
            throw new CliConfigurationException("Cannot read rule parameters file '" + ruleParametersFile + "'.", e);
        }
        configurationBuilder.with(Analyze.class, Analyze.RULE_PARAMETERS, properties);
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

    @Override
    public void configure(final CommandLine options, ConfigurationBuilder configurationBuilder) throws CliConfigurationException {
        super.configure(options, configurationBuilder);
        String ruleParametersFileName = getOptionValue(options, CMDLINE_OPTION_RULE_PARAMETERS, null);
        if (ruleParametersFileName != null) {
            loadRuleParameters(ruleParametersFileName, configurationBuilder);
        }
        String reportDirectoryValue = getOptionValue(options, CMDLINE_OPTION_REPORTDIR, DEFAULT_REPORT_DIRECTORY);
        reportDirectory = new File(reportDirectoryValue);
        reportDirectory.mkdirs();
        configurationBuilder.with(Analyze.class, Analyze.EXECUTE_APPLIED_CONCEPTS, options.hasOption(CMDLINE_OPTION_EXECUTE_APPLIED_CONCEPTS));
        configurationBuilder.with(Report.class, Report.WARN_ON_SEVERITY, getOptionValue(options, CMDLINE_OPTION_WARN_ON_SEVERITY));
        configurationBuilder.with(Report.class, Report.FAIL_ON_SEVERITY, getOptionValue(options, CMDLINE_OPTION_FAIL_ON_SEVERITY));
        configurationBuilder.with(Report.class, Report.CONTINUE_ON_FAILURE, options.hasOption(CMDLINE_OPTION_CONTINUE_ON_FAILURE));
        configurationBuilder.with(Report.class, Report.CREATE_ARCHIVE, options.hasOption(CMDLINE_OPTION_CREATE_REPORT_ARCHIVE));
    }

    @Override
    public void addTaskOptions(final List<Option> options) {
        super.addTaskOptions(options);
        options.add(OptionBuilder.withArgName(CMDLINE_OPTION_RULE_PARAMETERS)
            .withDescription("The name of a properties file providing rule parameters.")
            .hasArgs()
            .create(CMDLINE_OPTION_RULE_PARAMETERS));
        options.add(OptionBuilder.withArgName(CMDLINE_OPTION_REPORTDIR)
            .withDescription("The directory for writing reports.")
            .hasArgs()
            .create(CMDLINE_OPTION_REPORTDIR));
        options.add(OptionBuilder.withArgName(CMDLINE_OPTION_WARN_ON_SEVERITY)
            .withDescription("The severity threshold to report a warning.")
            .hasArgs()
            .create(CMDLINE_OPTION_WARN_ON_SEVERITY));
        options.add(OptionBuilder.withArgName(CMDLINE_OPTION_FAIL_ON_SEVERITY)
            .withDescription("The severity threshold to report a failure.")
            .hasArgs()
            .create(CMDLINE_OPTION_FAIL_ON_SEVERITY));
        options.add(OptionBuilder.withArgName(CMDLINE_OPTION_CONTINUE_ON_FAILURE)
            .withDescription("Determines if jQAssistant shall continue the build if failures have been detected.")
            .create(CMDLINE_OPTION_CONTINUE_ON_FAILURE));
        options.add(OptionBuilder.withArgName(CMDLINE_OPTION_EXECUTE_APPLIED_CONCEPTS)
            .withDescription("If set also execute concepts which have already been applied.")
            .create(CMDLINE_OPTION_EXECUTE_APPLIED_CONCEPTS));
        options.add(OptionBuilder.withArgName(CMDLINE_OPTION_CREATE_REPORT_ARCHIVE)
            .withDescription("If set a ZIP archive named 'jqassistant-report.zip' is created containing all generated reports.")
            .create(CMDLINE_OPTION_CREATE_REPORT_ARCHIVE));
    }
}
