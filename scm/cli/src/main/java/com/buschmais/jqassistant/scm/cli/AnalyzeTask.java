package com.buschmais.jqassistant.scm.cli;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.io.DirectoryWalker;
import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.MojoExecutionException;

import com.buschmais.jqassistant.core.analysis.api.Analyzer;
import com.buschmais.jqassistant.core.analysis.api.AnalyzerException;
import com.buschmais.jqassistant.core.analysis.api.ExecutionListener;
import com.buschmais.jqassistant.core.analysis.api.ExecutionListenerException;
import com.buschmais.jqassistant.core.analysis.api.PluginReaderException;
import com.buschmais.jqassistant.core.analysis.api.RuleSelector;
import com.buschmais.jqassistant.core.analysis.api.RuleSetReader;
import com.buschmais.jqassistant.core.analysis.api.RuleSetResolverException;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleSet;
import com.buschmais.jqassistant.core.analysis.impl.AnalyzerImpl;
import com.buschmais.jqassistant.core.analysis.impl.RuleSelectorImpl;
import com.buschmais.jqassistant.core.analysis.impl.RuleSetReaderImpl;
import com.buschmais.jqassistant.core.pluginmanager.api.RulePluginRepository;
import com.buschmais.jqassistant.core.pluginmanager.impl.RulePluginRepositoryImpl;
import com.buschmais.jqassistant.core.report.impl.CompositeReportWriter;
import com.buschmais.jqassistant.core.report.impl.InMemoryReportWriter;
import com.buschmais.jqassistant.core.report.impl.XmlReportWriter;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.scm.common.AnalysisHelper;

import static com.buschmais.jqassistant.scm.cli.Log.getLog;


/**
 * @author jn4, Kontext E GmbH, 24.01.14
 */
public class AnalyzeTask extends CommonJqAssistantTask implements OptionsConsumer {
    public static final String RULES_DIRECTORY = "jqassistant-rules";
    public static final String REPORT_XML = "./jqassistant/jqassistant-report.xml";

    private String baseDir = ".";
    private final RuleSelector ruleSelector = new RuleSelectorImpl();
    private final RuleSetReader ruleSetReader = new RuleSetReaderImpl();

    // todo where to get concepts, constraints, and groups?
    private List<String> concepts = new ArrayList<>();
    private List<String> constraints = new ArrayList<>();
    private List<String> groups = new ArrayList<>();

    public AnalyzeTask(Properties properties) {
        super("analyze", properties);
    }

    @Override
    protected void doTheTask(final Store store) {
        getLog().info("Executing analysis.");
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
        } catch (ExecutionListenerException e) {
            throw new RuntimeException("Cannot create XML report file writer.", e);
        }
        List<ExecutionListener> reportWriters = new LinkedList<>();
        reportWriters.add(inMemoryReportWriter);
        reportWriters.add(xmlReportWriter);
        try {
            CompositeReportWriter reportWriter = new CompositeReportWriter(reportWriters);
            Analyzer analyzer = new AnalyzerImpl(store, reportWriter);
            try {
                analyzer.execute(ruleSet);
            } catch (AnalyzerException e) {
                throw new RuntimeException("Analysis failed.", e);
            }
        } finally {
            IOUtils.closeQuietly(xmlReportFileWriter);
        }
        store.beginTransaction();
        try {
            final AnalysisHelper analysisHelper = new AnalysisHelper(getLog());
            analysisHelper.verifyConceptResults(inMemoryReportWriter);
            analysisHelper.verifyConstraintViolations(inMemoryReportWriter);
        } finally {
            store.commitTransaction();
        }
    }

    // copied from AbstractAnalysisMojo
    protected RuleSet resolveEffectiveRules() {
        RuleSet ruleSet = readRules();
        validateRuleSet(ruleSet);
        try {
            return ruleSelector.getEffectiveRuleSet(ruleSet, concepts, constraints, groups);
        } catch (RuleSetResolverException e) {
            throw new RuntimeException("Cannot resolve rules.", e);
        }
    }

    // copied from AbstractAnalysisMojo
    protected RuleSet readRules() {
        File selectedDirectory;
        selectedDirectory = createSelectedDirectoryFile();
        List<Source> sources = new ArrayList<>();
        // read rules from rules directory
        List<File> ruleFiles = readRulesDirectory(selectedDirectory);
        for (File ruleFile : ruleFiles) {
            getLog().debug("Adding rules from file " + ruleFile.getAbsolutePath());
            sources.add(new StreamSource(ruleFile));
        }
        List<Source> ruleSources = getRulePluginRepository().getRuleSources();
        sources.addAll(ruleSources);
        return ruleSetReader.read(sources);
    }

    protected RulePluginRepository getRulePluginRepository() {
        try {
            return new RulePluginRepositoryImpl();
        } catch (PluginReaderException e) {
            throw new RuntimeException("Cannot create rule plugin repository.", e);
        }
    }


    private File createSelectedDirectoryFile() {
        return new File(baseDir, RULES_DIRECTORY);
    }

    private List<File> readRulesDirectory(File rulesDirectory) {
        if (rulesDirectory.exists() && !rulesDirectory.isDirectory()) {
            throw new RuntimeException(rulesDirectory.getAbsolutePath() + " does not exist or is not a rulesDirectory.");
        }
        getLog().info("Reading rules from rulesDirectory " + rulesDirectory.getAbsolutePath());
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
            throw new RuntimeException("Cannot read rulesDirectory: " + rulesDirectory.getAbsolutePath(), e);
        }
    }

    // copied from AbstractAnalysisMojo
    private void validateRuleSet(RuleSet ruleSet) {
        StringBuilder message = new StringBuilder();
        if (!ruleSet.getMissingConcepts().isEmpty()) {
            message.append("\n  Concepts: ");
            message.append(ruleSet.getMissingConcepts());
        }
        if (!ruleSet.getMissingConstraints().isEmpty()) {
            message.append("\n  Constraints: ");
            message.append(ruleSet.getMissingConstraints());
        }
        if (!ruleSet.getMissingGroups().isEmpty()) {
            message.append("\n  Groups: ");
            message.append(ruleSet.getMissingGroups());
        }
        if (message.length() > 0) {
            throw new RuntimeException("The following rules are referenced but are not available;" + message);
        }
    }

    /**
     * Returns the {@link java.io.File} to write the XML report to.
     *
     * @return The {@link java.io.File} to write the XML report to.
     * @throws MojoExecutionException If the file cannot be determined.
     */
    private File getXmlReportFile() {
        File selectedXmlReportFile = new File(REPORT_XML);
        selectedXmlReportFile.getParentFile().mkdirs();
        return selectedXmlReportFile;
    }

    @Override
    public void withOptions(final CommandLine options) {
        if(options.hasOption("c")) {
            baseDir = options.getOptionValue("c");
        } else {
            System.out.println("No jQAssistant rules directory given, using default "+createSelectedDirectoryFile().getAbsolutePath());
        }
    }

    @Override
    protected void addFunctionSpecificOptions(final List<Option> options) {
        options.add(new Option("c", "conf", true, "basedir for jQAssistant rules, containing the dir jqassistant-rules and a jqassistant-plugin.xml"));
    }
}
