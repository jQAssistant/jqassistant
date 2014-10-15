package com.buschmais.jqassistant.scm.cli;

import static com.buschmais.jqassistant.scm.cli.Log.getLog;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.io.DirectoryWalker;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import com.buschmais.jqassistant.core.analysis.api.AnalysisException;
import com.buschmais.jqassistant.core.analysis.api.AnalysisListener;
import com.buschmais.jqassistant.core.analysis.api.AnalysisListenerException;
import com.buschmais.jqassistant.core.analysis.api.Analyzer;
import com.buschmais.jqassistant.core.analysis.api.Console;
import com.buschmais.jqassistant.core.analysis.api.RuleSelector;
import com.buschmais.jqassistant.core.analysis.api.RuleSetReader;
import com.buschmais.jqassistant.core.analysis.api.RuleSetResolverException;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleSet;
import com.buschmais.jqassistant.core.analysis.api.rule.Severity;
import com.buschmais.jqassistant.core.analysis.impl.AnalyzerImpl;
import com.buschmais.jqassistant.core.analysis.impl.RuleSelectorImpl;
import com.buschmais.jqassistant.core.analysis.impl.RuleSetReaderImpl;
import com.buschmais.jqassistant.core.plugin.api.PluginConfigurationReader;
import com.buschmais.jqassistant.core.plugin.api.PluginRepositoryException;
import com.buschmais.jqassistant.core.plugin.api.RulePluginRepository;
import com.buschmais.jqassistant.core.plugin.impl.RulePluginRepositoryImpl;
import com.buschmais.jqassistant.core.report.impl.CompositeReportWriter;
import com.buschmais.jqassistant.core.report.impl.InMemoryReportWriter;
import com.buschmais.jqassistant.core.report.impl.XmlReportWriter;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.scm.common.report.ReportHelper;

/**
 * @author jn4, Kontext E GmbH, 24.01.14
 */
public class AnalyzeTask extends AbstractJQATask implements OptionsConsumer {

    public static final String XML_REPORT_FILE = "jqassistant-report.xml";

    public static final String CMDLINE_OPTION_RULEDIR = "r";
    public static final String CMDLINE_OPTION_REPORTDIR = "reportDirectory";
    public static final String CMDLINE_OPTION_GROUPS = "groups";
    public static final String CMDLINE_OPTION_CONSTRAINTS = "constraints";
    public static final String CMDLINE_OPTION_CONCEPTS = "concepts";
    public static final String CMDLINE_OPTION_SEVERITY = "severity";

    private static final Console LOG = Log.getLog();

    private final RuleSelector ruleSelector = new RuleSelectorImpl();
    private final RuleSetReader ruleSetReader = new RuleSetReaderImpl();

    private String ruleDirectory;
    private String reportDirectory;
    private List<String> concepts;
    private List<String> constraints;
    private List<String> groups;
    private Severity severity;

    public AnalyzeTask(PluginConfigurationReader pluginConfigurationReader) {
        super(pluginConfigurationReader);
    }

    @Override
    protected void executeTask(final Store store) {
        LOG.info("Executing analysis.");
        final RuleSet ruleSet = resolveEffectiveRules();
        InMemoryReportWriter inMemoryReportWriter = new InMemoryReportWriter();
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
        List<AnalysisListener> reportWriters = new LinkedList<>();
        reportWriters.add(inMemoryReportWriter);
        reportWriters.add(xmlReportWriter);
        try {
            CompositeReportWriter reportWriter = new CompositeReportWriter(reportWriters);
            Analyzer analyzer = new AnalyzerImpl(store, reportWriter, getLog());
            try {
                analyzer.execute(ruleSet);
            } catch (AnalysisException e) {
                throw new RuntimeException("Analysis failed.", e);
            }
        } finally {
            IOUtils.closeQuietly(xmlReportFileWriter);
        }
        store.beginTransaction();
        try {
            final ReportHelper reportHelper = new ReportHelper(getLog());
            final int conceptViolations = reportHelper.verifyConceptResults(inMemoryReportWriter);
            if (conceptViolations > 0) {
                throw new JqaConstraintViolationException(conceptViolations + " concept(s) returned empty results!");
            }
            final int violations = reportHelper.verifyConstraintViolations(severity, inMemoryReportWriter);
            if (violations > 0) {
                throw new JqaConstraintViolationException(violations + " constraint(s) violated!");
            }
            reportHelper.verifyConstraintViolations(inMemoryReportWriter);
        } catch (AnalysisListenerException e) {
            throw new RuntimeException("Cannot print report.", e);
        } finally {
            store.commitTransaction();
        }
    }

    // copied from AbstractAnalysisMojo
    protected RuleSet resolveEffectiveRules() {
        RuleSet ruleSet = readRules();
        String message = reportHelper.validateRuleSet(ruleSet);
        if (StringUtils.isNotBlank(message)) {
            throw new RuntimeException("Rules are not valid: " + message);
        }
        try {
            return ruleSelector.getEffectiveRuleSet(ruleSet, concepts, constraints, groups);
        } catch (RuleSetResolverException e) {
            throw new RuntimeException("Cannot resolve rules.", e);
        }
    }

    // copied from AbstractAnalysisMojo
    protected RuleSet readRules() {
        File selectedDirectory = createSelectedDirectoryFile();
        List<Source> sources = new ArrayList<>();
        // read rules from rules directory
        List<File> ruleFiles = readRulesDirectory(selectedDirectory);
        for (File ruleFile : ruleFiles) {
            LOG.debug("Adding rules from file " + ruleFile.getAbsolutePath());
            sources.add(new StreamSource(ruleFile));
        }
        List<Source> ruleSources = getRulePluginRepository().getRuleSources();
        sources.addAll(ruleSources);
        return ruleSetReader.read(sources);
    }

    protected RulePluginRepository getRulePluginRepository() {
        try {
            return new RulePluginRepositoryImpl(pluginConfigurationReader);
        } catch (PluginRepositoryException e) {
            throw new RuntimeException("Cannot create rule plugin repository.", e);
        }
    }

    private File createSelectedDirectoryFile() {
        return new File(ruleDirectory);
    }

    private List<File> readRulesDirectory(File rulesDirectory) {
        if (rulesDirectory.exists() && !rulesDirectory.isDirectory()) {
            throw new RuntimeException(rulesDirectory.getAbsolutePath() + " does not exist or is not a rulesDirectory.");
        }
        LOG.info("Reading rules from directory " + rulesDirectory.getAbsolutePath());
        final List<File> ruleFiles = new ArrayList<>();
        try {
            new DirectoryWalker<File>() {

                @Override
                protected void handleFile(File file, int depth, Collection<File> results) throws IOException {
                    if (!file.isDirectory() && file.getName().endsWith(".xml")) {
                        results.add(file);
                    }
                }

                public void scan(File directory) throws IOException {
                    super.walk(directory, ruleFiles);
                }
            }.scan(rulesDirectory);
            return ruleFiles;
        } catch (IOException e) {
            throw new RuntimeException("Cannot read rules directory: " + rulesDirectory.getAbsolutePath(), e);
        }
    }

    /**
     * Returns the {@link java.io.File} to write the XML report to.
     *
     * @return The {@link java.io.File} to write the XML report to.
     */
    private File getXmlReportFile() {
        File reportFile = new File(reportDirectory, XML_REPORT_FILE);
        reportFile.getParentFile().mkdirs();
        return reportFile;
    }

    @Override
    public void withOptions(final CommandLine options) {
        ruleDirectory = getOptionValue(options, CMDLINE_OPTION_RULEDIR, DEFAULT_RULE_DIRECTORY);
        reportDirectory = getOptionValue(options, CMDLINE_OPTION_REPORTDIR, DEFAULT_REPORT_DIRECTORY);
        groups = getOptionValues(options, CMDLINE_OPTION_GROUPS, Arrays.asList("default"));
        constraints = getOptionValues(options, CMDLINE_OPTION_CONSTRAINTS, Collections.<String> emptyList());
        concepts = getOptionValues(options, CMDLINE_OPTION_CONCEPTS, Collections.<String> emptyList());
        severity = Severity.valueOf(getOptionValue(options, CMDLINE_OPTION_SEVERITY, Severity.CRITICAL.name()).toUpperCase());
    }

    @Override
    protected void addTaskOptions(final List<Option> options) {
        options.add(OptionBuilder.withArgName(CMDLINE_OPTION_RULEDIR).withLongOpt("ruleDirectory").withDescription("The directory containing rules.").hasArgs()
                .create(CMDLINE_OPTION_RULEDIR));
        options.add(OptionBuilder.withArgName(CMDLINE_OPTION_REPORTDIR).withDescription("The directory for writing reports.").hasArgs()
                .create(CMDLINE_OPTION_REPORTDIR));
        options.add(OptionBuilder.withArgName(CMDLINE_OPTION_GROUPS).withDescription("The groups to execute (default='default').").withValueSeparator(',')
                .hasArgs().create(CMDLINE_OPTION_GROUPS));
        options.add(OptionBuilder.withArgName(CMDLINE_OPTION_CONSTRAINTS).withDescription("The constraints to verify.").withValueSeparator(',').hasArgs()
                .create(CMDLINE_OPTION_CONSTRAINTS));
        options.add(OptionBuilder.withArgName(CMDLINE_OPTION_CONCEPTS).withDescription("The concepts to apply.").withValueSeparator(',').hasArgs()
                .create(CMDLINE_OPTION_CONCEPTS));
        options.add(OptionBuilder.withArgName(CMDLINE_OPTION_SEVERITY).withDescription("The severity threshold to report a failure.").hasArgs()
                .create(CMDLINE_OPTION_SEVERITY));

    }
}
