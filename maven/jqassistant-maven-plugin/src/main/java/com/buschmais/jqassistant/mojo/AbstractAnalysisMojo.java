package com.buschmais.jqassistant.mojo;

import com.buschmais.jqassistant.core.analysis.api.CatalogReader;
import com.buschmais.jqassistant.core.analysis.api.RulesReader;
import com.buschmais.jqassistant.core.analysis.impl.CatalogReaderImpl;
import com.buschmais.jqassistant.core.analysis.impl.RulesReaderImpl;
import com.buschmais.jqassistant.core.model.api.rules.Concept;
import com.buschmais.jqassistant.core.model.api.rules.Constraint;
import com.buschmais.jqassistant.core.model.api.rules.Group;
import com.buschmais.jqassistant.core.model.api.rules.RuleSet;
import edu.emory.mathcs.backport.java.util.Arrays;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.DirectoryWalker;
import org.apache.commons.io.IOUtils;
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
 *
 * @aggregator true
 */
public abstract class AbstractAnalysisMojo extends AbstractStoreMojo {

    public static final String DEFAULT_RULES_DIRECTORY = "jqassistant";
    public static final String DEFAULT_GROUP = "default";
    public static final String LOG_LINE_PREFIX = "  \"";

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
     * @param conceptNames The list of concept names.
     * @param ruleSet      The {@link RuleSet}.
     * @return The selected concepts.
     * @throws org.apache.maven.plugin.MojoExecutionException
     *          If an undefined concept  is referenced.
     */
    private List<Concept> getSelectedConcepts(List<String> conceptNames, RuleSet ruleSet) throws MojoExecutionException {
        final List<Concept> selectedConcepts = new ArrayList<>();
        if (conceptNames != null) {
            for (String conceptName : conceptNames) {
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
     * @param constraintNames The list of constraint names.
     * @param ruleSet         The {@link RuleSet}.
     * @return The selected constraints.
     * @throws org.apache.maven.plugin.MojoExecutionException
     *          If an undefined constraint is referenced.
     */
    private List<Constraint> getSelectedConstraints(List<String> constraintNames, RuleSet ruleSet) throws MojoExecutionException {
        final List<Constraint> selectedConstraints = new ArrayList<>();
        if (constraintNames != null) {
            for (String constraintName : constraintNames) {
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
     * @param groupNames The list of constraint names.
     * @param ruleSet    The {@link RuleSet}.
     * @return The selected groups.
     * @throws org.apache.maven.plugin.MojoExecutionException
     *          If an undefined group is referenced.
     */
    private List<Group> getSelectedGroups(List<String> groupNames, RuleSet ruleSet) throws MojoExecutionException {
        final List<Group> selectedGroups = new ArrayList<>();
        for (String groupName : groupNames) {
            Group group = ruleSet.getGroups().get(groupName);
            if (group == null) {
                throw new MojoExecutionException("The group '" + groupName + "' is not defined.");
            }
            selectedGroups.add(group);
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
        validateRuleSet(ruleSet);
        RuleSet effectiveRuleSet = new RuleSet();
        // Use the default group if no group, constraint or concept is specified.
        List<String> groupNames;
        if (CollectionUtils.isEmpty(groups) && CollectionUtils.isEmpty(constraints) && CollectionUtils.isEmpty(concepts)) {
            groupNames = Arrays.asList(new String[]{DEFAULT_GROUP});
        } else {
            groupNames = groups;
        }
        resolveConcepts(getSelectedConcepts(concepts, ruleSet), effectiveRuleSet);
        resolveConstraints(getSelectedConstraints(constraints, ruleSet), effectiveRuleSet);
        resolveGroups(getSelectedGroups(groupNames, ruleSet), effectiveRuleSet);
        return effectiveRuleSet;
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
     * Validates the given rule set for unresolved concepts, constraints or groups.
     *
     * @param ruleSet The rule set.
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
