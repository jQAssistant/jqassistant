package com.buschmais.jqassistant.scm.maven;

import com.buschmais.jqassistant.core.analysis.api.PluginReaderException;
import com.buschmais.jqassistant.core.analysis.api.RuleSelector;
import com.buschmais.jqassistant.core.analysis.api.RuleSetReader;
import com.buschmais.jqassistant.core.analysis.api.RuleSetResolverException;
import com.buschmais.jqassistant.core.analysis.api.rule.Concept;
import com.buschmais.jqassistant.core.analysis.api.rule.Constraint;
import com.buschmais.jqassistant.core.analysis.api.rule.Group;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleSet;
import com.buschmais.jqassistant.core.analysis.impl.RuleSelectorImpl;
import com.buschmais.jqassistant.core.analysis.impl.RuleSetReaderImpl;
import com.buschmais.jqassistant.core.pluginmanager.api.RulePluginRepository;
import com.buschmais.jqassistant.core.pluginmanager.api.ScannerPluginRepository;
import com.buschmais.jqassistant.core.pluginmanager.impl.RulePluginRepositoryImpl;
import com.buschmais.jqassistant.core.pluginmanager.impl.ScannerPluginRepositoryImpl;
import com.buschmais.jqassistant.core.store.api.Store;
import org.apache.commons.io.DirectoryWalker;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

/**
 * Abstract base implementation for analysis mojos.
 */
public abstract class AbstractAnalysisMojo extends org.apache.maven.plugin.AbstractMojo {

    public static final String REPORT_XML = "jqassistant-report.xml";

    public static final String LOG_LINE_PREFIX = "  \"";

    /**
     * The store directory.
     */
    @Parameter(property = "jqassistant.store.directory")
    protected File storeDirectory;

    /**
     * The directory to scan for rules.
     */
    @Parameter(property = "jqassistant.rules.directory")
    protected File rulesDirectory;

    /**
     * The url to retrieve rules.
     */
    @Parameter(property = "jqassistant.rules.url")
    protected URL rulesUrl;

    /**
     * The list of concept names to be applied.
     */
    @Parameter(property = "jqassistant.concepts")
    protected List<String> concepts;

    /**
     * The list of constraint names to be validated.
     */
    @Parameter(property = "jqassistant.constraints")
    protected List<String> constraints;

    /**
     * The list of group names to be executed.
     */
    @Parameter(property = "jqassistant.groups")
    protected List<String> groups;

    /**
     * The file to write the XML report to.
     */
    @Parameter(property = "jqassistant.report.xml")
    protected File xmlReportFile;

    /**
     * The rules reader instance.
     */
    private RuleSetReader ruleSetReader = new RuleSetReaderImpl();

    /**
     * The rules selector.
     */
    private RuleSelector ruleSelector = new RuleSelectorImpl();

    /**
     * Return the plugin properties.
     *
     * @return The plugin properties.
     */
    protected Properties getPluginProperties() {
        return new Properties();
    }

    /**
     * Return the scanner plugin repository.
     *
     * @param store      The store.
     * @param properties The properties.
     * @return The scanner plugin repository.
     * @throws MojoExecutionException If the repository cannot be created.
     */
    protected ScannerPluginRepository getScannerPluginRepository(Store store, Properties properties) throws MojoExecutionException {
        try {
            return new ScannerPluginRepositoryImpl(store, properties);
        } catch (PluginReaderException e) {
            throw new MojoExecutionException("Cannot create rule plugin repository.", e);
        }
    }

    /**
     * Return the rule plugin repository.
     *
     * @return The rule plugin repository.
     * @throws MojoExecutionException If the repository cannot be created.
     */
    protected RulePluginRepository getRulePluginRepository() throws MojoExecutionException {
        try {
            return new RulePluginRepositoryImpl();
        } catch (PluginReaderException e) {
            throw new MojoExecutionException("Cannot create rule plugin repository.", e);
        }
    }

    /**
     * Reads the available rules from the rules directory and deployed catalogs.
     *
     * @return A {@link com.buschmais.jqassistant.core.analysis.api.rule.RuleSet}.
     * @throws MojoExecutionException If the rules cannot be read.
     */
    protected RuleSet readRules(MavenProject baseProject) throws MojoExecutionException {
        File selectedDirectory;
        if (rulesDirectory != null) {
            selectedDirectory = rulesDirectory;
        } else {
            selectedDirectory = new File(baseProject.getBasedir(), BaseProjectResolver.RULES_DIRECTORY);
        }
        List<Source> sources = new ArrayList<>();
        // read rules from rules directory
        if (selectedDirectory != null) {
            List<File> ruleFiles = readRulesDirectory(selectedDirectory);
            for (File ruleFile : ruleFiles) {
                getLog().debug("Adding rules from file " + ruleFile.getAbsolutePath());
                sources.add(new StreamSource(ruleFile));
            }
        }
        if (rulesUrl != null) {
            try {
                sources.add(new StreamSource(rulesUrl.openStream(), rulesUrl.toExternalForm()));
            } catch (IOException e) {
                throw new MojoExecutionException("Cannot open rule URL " + rulesUrl.toExternalForm());
            }
        }
        List<Source> ruleSources = getRulePluginRepository().getRuleSources();
        sources.addAll(ruleSources);
        return ruleSetReader.read(sources);
    }

    /**
     * Resolves the effective rules.
     *
     * @return The resolved rules set.
     * @throws MojoExecutionException If resolving fails.
     */
    protected RuleSet resolveEffectiveRules(MavenProject baseProject) throws MojoExecutionException {
        RuleSet ruleSet = readRules(baseProject);
        validateRuleSet(ruleSet);
        try {
            return ruleSelector.getEffectiveRuleSet(ruleSet, concepts, constraints, groups);
        } catch (RuleSetResolverException e) {
            throw new MojoExecutionException("Cannot resolve rules.", e);
        }
    }

    /**
     * Retrieves the list of available rules from the rules directory.
     *
     * @param rulesDirectory The rules directory.
     * @return The {@link java.util.List} of available rules
     * {@link java.io.File}s.
     * @throws MojoExecutionException If the rules directory cannot be read.
     */
    private List<File> readRulesDirectory(File rulesDirectory) throws MojoExecutionException {
        if (rulesDirectory.exists() && !rulesDirectory.isDirectory()) {
            throw new MojoExecutionException(rulesDirectory.getAbsolutePath() + " does not exist or is not a rulesDirectory.");
        }
        getLog().info("Reading rules from rulesDirectory " + rulesDirectory.getAbsolutePath());
        final List<File> ruleFiles = new ArrayList<File>();
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
            throw new MojoExecutionException("Cannot read rulesDirectory: " + rulesDirectory.getAbsolutePath(), e);
        }
    }

    /**
     * Validates the given rules set for unresolved concepts, constraints or
     * groups.
     *
     * @param ruleSet The rules set.
     * @throws MojoExecutionException If there are unresolved concepts, constraints or groups.
     */
    private void validateRuleSet(RuleSet ruleSet) throws MojoExecutionException {
        StringBuffer message = new StringBuffer();
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
            throw new MojoExecutionException("The following rules are referenced but are not available;" + message);
        }
    }

    /**
     * Logs the given {@link RuleSet} on level info.
     *
     * @param ruleSet The {@link RuleSet}.
     */
    protected void logRuleSet(RuleSet ruleSet) {
        getLog().info("Groups [" + ruleSet.getGroups().size() + "]");
        for (Group group : ruleSet.getGroups().values()) {
            getLog().info(LOG_LINE_PREFIX + group.getId() + "\"");
        }
        getLog().info("Constraints [" + ruleSet.getConstraints().size() + "]");
        for (Constraint constraint : ruleSet.getConstraints().values()) {
            getLog().info(LOG_LINE_PREFIX + constraint.getId() + "\" - " + constraint.getDescription());
        }
        getLog().info("Concepts [" + ruleSet.getConcepts().size() + "]");
        for (Concept concept : ruleSet.getConcepts().values()) {
            getLog().info(LOG_LINE_PREFIX + concept.getId() + "\" - " + concept.getDescription());
        }
        if (!ruleSet.getMissingConcepts().isEmpty()) {
            getLog().warn("Missing concepts [" + ruleSet.getMissingConcepts().size() + "]");
            for (String missingConcept : ruleSet.getMissingConcepts()) {
                getLog().warn(LOG_LINE_PREFIX + missingConcept);
            }
        }
        if (!ruleSet.getMissingConstraints().isEmpty()) {
            getLog().warn("Missing constraints [" + ruleSet.getMissingConstraints().size() + "]");
            for (String missingConstraint : ruleSet.getMissingConstraints()) {
                getLog().warn(LOG_LINE_PREFIX + missingConstraint);
            }
        }
        if (!ruleSet.getMissingGroups().isEmpty()) {
            getLog().warn("Missing groups [" + ruleSet.getMissingGroups().size() + "]");
            for (String missingGroup : ruleSet.getMissingGroups()) {
                getLog().warn(LOG_LINE_PREFIX + missingGroup);
            }
        }
    }
}
