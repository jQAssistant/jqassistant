package com.buschmais.jqassistant.mojo;

import com.buschmais.jqassistant.core.analysis.api.CatalogReader;
import com.buschmais.jqassistant.core.analysis.api.RulesReader;
import com.buschmais.jqassistant.core.analysis.impl.CatalogReaderImpl;
import com.buschmais.jqassistant.core.analysis.impl.RulesReaderImpl;
import com.buschmais.jqassistant.core.model.api.rules.Concept;
import com.buschmais.jqassistant.core.model.api.rules.Constraint;
import com.buschmais.jqassistant.core.model.api.rules.Group;
import com.buschmais.jqassistant.core.model.api.rules.RuleSet;
import org.apache.commons.io.DirectoryWalker;
import org.apache.maven.plugin.MojoExecutionException;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Abstract base implementation for analysis MOJOs.
 */
public abstract class AbstractAnalysisMojo extends AbstractStoreMojo {

    public static final String DEFAULT_RULES_DIRECTORY = "jqassistant";

    /**
     * The directory to scan for rules.
     *
     * @parameter expression="${jqassistant.rules.directory}"
     */
    protected File rulesDirectory;

    /**
     * The list of concept names to be applied.
     *
     * @parameter expression="${jqassistant.concepts}"
     */
    protected List<String> concepts;

    /**
     * The list of constraint names to be validated.
     *
     * @parameter expression="${jqassistant.constraints}"
     */
    protected List<String> constraints;

    /**
     * The list of group names to be executed.
     *
     * @parameter expression="${jqassistant.groups}"
     */
    protected List<String> groups;

    /**
     * The catalog reader instance.
     */
    private CatalogReader catalogReader = new CatalogReaderImpl();

    /**
     * The rules reader instance.
     */
    private RulesReader rulesReader = new RulesReaderImpl();

    /**
     * Return the selected concepts.
     *
     * @param ruleSet The {@link RuleSet}.
     * @return The selected concepts.
     * @throws org.apache.maven.plugin.MojoExecutionException
     *          If an undefined concept  is referenced.
     */
    protected List<Concept> getSelectedConcepts(RuleSet ruleSet) throws MojoExecutionException {
        final List<Concept> selectedConcepts = new ArrayList<>();
        if (concepts != null) {
            for (String conceptName : concepts) {
                Concept concept = ruleSet.getConcepts().get(conceptName);
                if (concept == null) {
                    throw new MojoExecutionException("The concept '" + conceptName + "' is not defined.");
                }
                selectedConcepts.add(concept);
            }
        }
        return selectedConcepts;
    }

    /**
     * Return the selected constraints.
     *
     * @param ruleSet The {@link RuleSet}.
     * @return The selected constraints.
     * @throws org.apache.maven.plugin.MojoExecutionException
     *          If an undefined constraint is referenced.
     */
    protected List<Constraint> getSelectedConstraints(RuleSet ruleSet) throws MojoExecutionException {
        final List<Constraint> selectedConstraints = new ArrayList<>();
        if (constraints != null) {
            for (String constraintName : constraints) {
                Constraint concept = ruleSet.getConstraints().get(constraintName);
                if (concept == null) {
                    throw new MojoExecutionException("The constraint '" + constraintName + "' is not defined.");
                }
                selectedConstraints.add(concept);
            }
        }
        return selectedConstraints;
    }

    /**
     * Return the selected groups.
     *
     * @param ruleSet The {@link RuleSet}.
     * @return The selected groups.
     * @throws org.apache.maven.plugin.MojoExecutionException
     *          If an undefined group is referenced.
     */
    protected List<Group> getSelectedGroups(RuleSet ruleSet) throws MojoExecutionException {
        final List<Group> selectedGroups = new ArrayList<>();
        if (groups != null) {
            for (String groupName : groups) {
                Group group = ruleSet.getGroups().get(groupName);
                if (group == null) {
                    throw new MojoExecutionException("The group '" + groupName + "' is not defined.");
                }
                selectedGroups.add(group);
            }
        }
        return selectedGroups;
    }


    /**
     * Reads the available rules from the rules directory and deployed catalogs.
     *
     * @return A {@link java.util.Map} containing {@link com.buschmais.jqassistant.core.model.api.rules.Group}s identified by their id.
     * @throws org.apache.maven.plugin.MojoExecutionException
     *          If the rules cannot be read.
     */
    protected RuleSet readRules() throws MojoExecutionException {
        if (rulesDirectory == null) {
            rulesDirectory = new File(basedir.getAbsoluteFile() + File.separator + DEFAULT_RULES_DIRECTORY);
        }
        List<Source> sources = new ArrayList<>();
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
     * Resolves the effective rules.
     *
     * @return The resolved rule set.
     * @throws MojoExecutionException If resolving fails.
     */
    protected RuleSet resolveEffectiveRules() throws MojoExecutionException {
        RuleSet ruleSet = readRules();
        RuleSet targetRuleSet = new RuleSet();
        resolveConcepts(getSelectedConcepts(ruleSet), targetRuleSet);
        resolveConstraints(getSelectedConstraints(ruleSet), targetRuleSet);
        resolveGroups(getSelectedGroups(ruleSet), targetRuleSet);
        return targetRuleSet;
    }

    /**
     * Resolve the given selected groups names into the target rule set.
     *
     * @param groups        The selected group names.
     * @param targetRuleSet The target rule set.
     */
    private void resolveGroups(Collection<Group> groups, RuleSet targetRuleSet) {
        for (Group group : groups) {
            if (!targetRuleSet.getGroups().containsKey(group.getId())) {
                targetRuleSet.getGroups().put(group.getId(), group);
                resolveGroups(group.getGroups(), targetRuleSet);
                resolveConcepts(group.getConcepts(), targetRuleSet);
                resolveConstraints(group.getConstraints(), targetRuleSet);
            }
        }
    }

    /**
     * Resolve the given selected constraint names into the target rule set.
     *
     * @param constraints   The selected constraint names.
     * @param targetRuleSet The target rule set.
     */
    private void resolveConstraints(Collection<Constraint> constraints, RuleSet targetRuleSet) {
        for (Constraint constraint : constraints) {
            if (!targetRuleSet.getConstraints().containsKey(constraint.getId())) {
                targetRuleSet.getConstraints().put(constraint.getId(), constraint);
                resolveConcepts(constraint.getRequiredConcepts(), targetRuleSet);
            }
        }
    }

    /**
     * Resolve the given selected concept names into the target rule set.
     *
     * @param concepts      The selected concept names.
     * @param targetRuleSet The target rule set.
     */
    private void resolveConcepts(Collection<Concept> concepts, RuleSet targetRuleSet) {
        for (Concept concept : concepts) {
            if (!targetRuleSet.getConcepts().containsKey(concept.getId())) {
                targetRuleSet.getConcepts().put(concept.getId(), concept);
                resolveConcepts(concept.getRequiredConcepts(), targetRuleSet);
            }
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
            getLog().info("  \"" + group.getId() + "\"");
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
