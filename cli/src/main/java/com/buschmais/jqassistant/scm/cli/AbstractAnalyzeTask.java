package com.buschmais.jqassistant.scm.cli;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.io.DirectoryWalker;
import org.apache.commons.lang.StringUtils;

import com.buschmais.jqassistant.core.analysis.api.RuleSelector;
import com.buschmais.jqassistant.core.analysis.api.RuleSetReader;
import com.buschmais.jqassistant.core.analysis.api.RuleSetResolverException;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleSet;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleSource;
import com.buschmais.jqassistant.core.analysis.impl.RuleSelectorImpl;
import com.buschmais.jqassistant.core.analysis.impl.RuleSetReaderImpl;

/**
 * Abstract base class for all tasks working with rules.
 */
public abstract class AbstractAnalyzeTask extends AbstractJQATask {

    private static final String CMDLINE_OPTION_RULEDIR = "r";
    private static final String CMDLINE_OPTION_GROUPS = "groups";
    private static final String CMDLINE_OPTION_CONSTRAINTS = "constraints";
    private static final String CMDLINE_OPTION_CONCEPTS = "concepts";

    private static final Log LOG = Log.getLog();

    private final RuleSelector ruleSelector = new RuleSelectorImpl();
    private final RuleSetReader ruleSetReader = new RuleSetReaderImpl();
    private String ruleDirectory;
    private List<String> concepts;
    private List<String> constraints;
    private List<String> groups;

    // copied from AbstractAnalysisMojo
    protected RuleSet getEffectiveRules() {
        RuleSet ruleSet = getAvailableRules();
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
    protected RuleSet getAvailableRules() {
        File selectedDirectory = new File(ruleDirectory);
        List<RuleSource> sources = new ArrayList<>();
        // read rules from rules directory
        List<File> ruleFiles = readRulesDirectory(selectedDirectory);
        for (final File ruleFile : ruleFiles) {
            LOG.debug("Adding rules from file " + ruleFile.getAbsolutePath());
            sources.add(new RuleSource() {
                @Override
                public String getId() {
                    return ruleFile.getName();
                }

                @Override
                public InputStream getInputStream() throws IOException {
                    return new FileInputStream(ruleFile);
                }
            });
        }
        List<RuleSource> ruleSources = rulePluginRepository.getRuleSources();
        sources.addAll(ruleSources);
        return ruleSetReader.read(sources);
    }

    private List<File> readRulesDirectory(File rulesDirectory) {
        if (rulesDirectory.exists() && !rulesDirectory.isDirectory()) {
            throw new RuntimeException(rulesDirectory.getAbsolutePath() + " does not exist or is not a directory.");
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

    @Override
    public void withOptions(CommandLine options) {
        ruleDirectory = getOptionValue(options, CMDLINE_OPTION_RULEDIR, DEFAULT_RULE_DIRECTORY);
        groups = getOptionValues(options, CMDLINE_OPTION_GROUPS, Arrays.asList("default"));
        constraints = getOptionValues(options, CMDLINE_OPTION_CONSTRAINTS, Collections.<String> emptyList());
        concepts = getOptionValues(options, CMDLINE_OPTION_CONCEPTS, Collections.<String> emptyList());
    }

    @Override
    protected void addTaskOptions(List<Option> options) {
        options.add(OptionBuilder.withArgName(CMDLINE_OPTION_RULEDIR).withLongOpt("ruleDirectory").withDescription("The directory containing rules.").hasArgs()
                .create(CMDLINE_OPTION_RULEDIR));
        options.add(OptionBuilder.withArgName(CMDLINE_OPTION_GROUPS).withDescription("The groups to execute (default='default').").withValueSeparator(',')
                .hasArgs().create(CMDLINE_OPTION_GROUPS));
        options.add(OptionBuilder.withArgName(CMDLINE_OPTION_CONSTRAINTS).withDescription("The constraints to verify.").withValueSeparator(',').hasArgs()
                .create(CMDLINE_OPTION_CONSTRAINTS));
        options.add(OptionBuilder.withArgName(CMDLINE_OPTION_CONCEPTS).withDescription("The concepts to apply.").withValueSeparator(',').hasArgs()
                .create(CMDLINE_OPTION_CONCEPTS));
    }
}
