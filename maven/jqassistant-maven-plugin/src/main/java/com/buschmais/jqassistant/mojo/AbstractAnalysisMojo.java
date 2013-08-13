package com.buschmais.jqassistant.mojo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import com.buschmais.jqassistant.core.model.api.rules.AnalysisGroup;
import org.apache.commons.io.DirectoryWalker;
import org.apache.maven.plugin.MojoExecutionException;

import com.buschmais.jqassistant.core.analysis.api.CatalogReader;
import com.buschmais.jqassistant.core.analysis.api.RulesReader;
import com.buschmais.jqassistant.core.analysis.impl.CatalogReaderImpl;
import com.buschmais.jqassistant.core.analysis.impl.RulesReaderImpl;
import com.buschmais.jqassistant.core.model.api.rules.Concept;
import com.buschmais.jqassistant.core.model.api.rules.Constraint;
import com.buschmais.jqassistant.core.model.api.rules.RuleSet;

/**
 * Abstract base implementation for analysis MOJOs.
 */
public abstract class AbstractAnalysisMojo extends AbstractStoreMojo {

    public static final String DEFAULT_RULES_DIRECTORY = "jqassistant";

    /**
     * The directory to scan for rules.
     *
     * @parameter expression="${jqassistant.rules.rulesDirectory}"
     */
    protected File rulesDirectory;

    /**
     * The list of constraint group names to be executed.
     *
     * @parameter
     */
    protected List<String> analysisGroups;

    private CatalogReader catalogReader = new CatalogReaderImpl();

    private RulesReader rulesReader = new RulesReaderImpl();

    /**
     * Return the selected analysis groups.
     *
     * @param ruleSet The {@link RuleSet}.
     * @return The selected constraint groups.
     * @throws org.apache.maven.plugin.MojoExecutionException
     *          If an undefined group is referenced.
     */
    protected List<AnalysisGroup> getSelectedAnalysisGroups(RuleSet ruleSet) throws MojoExecutionException {
        final List<AnalysisGroup> selectedAnalysisGroups = new ArrayList<AnalysisGroup>();
        if (analysisGroups != null) {
            for (String analysisGroup : analysisGroups) {
                AnalysisGroup group = ruleSet.getAnalysisGroups().get(analysisGroup);
                if (group == null) {
                    throw new MojoExecutionException("The analysis group '" + analysisGroup + "' is not defined.");
                }
                selectedAnalysisGroups.add(group);
            }
        } else {
            selectedAnalysisGroups.addAll(ruleSet.getAnalysisGroups().values());
        }
        return selectedAnalysisGroups;
    }

    /**
     * Reads the available rules from the rules directory and deployed catalogs.
     *
     * @return A {@link java.util.Map} containing {@link com.buschmais.jqassistant.core.model.api.rules.AnalysisGroup}s identified by their id.
     * @throws org.apache.maven.plugin.MojoExecutionException
     *          If the rules cannot be read.
     */
    protected RuleSet readRules() throws MojoExecutionException {
        if (rulesDirectory == null) {
            rulesDirectory = new File(basedir.getAbsoluteFile() + File.separator + DEFAULT_RULES_DIRECTORY);
        }
        List<Source> sources = new ArrayList<Source>();
        // read rules from rules directory
        List<File> ruleFiles = readRulesDirectory(rulesDirectory);
        for (File ruleFile : ruleFiles) {
            getLog().debug("Adding rules from file " + ruleFile.getAbsolutePath());
            sources.add(new StreamSource(ruleFile));
        }
        sources.addAll(catalogReader.readCatalogs());

        return rulesReader.read(sources);
    }

    /**
     * Retrieves the list of available rules from the rules directory.
     *
     * @param rulesDirectory The rules directory.
     * @return The {@link java.util.List} of available rule {@link java.io.File}s.
     * @throws org.apache.maven.plugin.MojoExecutionException
     *          If the rules directory cannot be read.
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
     * Logs the given {@link RuleSet} on level info.
     *
     * @param ruleSet The {@link RuleSet}.
     */
    protected void logRuleSet(RuleSet ruleSet) {
        getLog().info("Analysis groups [" + ruleSet.getAnalysisGroups().size() + "]");
        for (AnalysisGroup analysisGroup : ruleSet.getAnalysisGroups().values()) {
            getLog().info("  " + analysisGroup.getId());
        }
        getLog().info("Constraints [" + ruleSet.getConstraints().size() + "]");
        for (Constraint constraint : ruleSet.getConstraints().values()) {
            getLog().info("  \"" + constraint.getId() + "\" - " + constraint.getDescription());
        }
        getLog().info("Concepts [" + ruleSet.getConcepts().size() + "]");
        for (Concept concept : ruleSet.getConcepts().values()) {
            getLog().info("  \"" + concept.getId() + "\" - " + concept.getDescription());
        }
    }
}
