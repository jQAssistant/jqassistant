package com.buschmais.jqassistant.scm.common.report;

import java.util.Set;

import com.buschmais.jqassistant.core.analysis.api.AnalysisException;
import com.buschmais.jqassistant.core.analysis.api.Console;
import com.buschmais.jqassistant.core.analysis.api.RuleSelection;
import com.buschmais.jqassistant.core.analysis.api.rule.Concept;
import com.buschmais.jqassistant.core.analysis.api.rule.Constraint;
import com.buschmais.jqassistant.core.analysis.api.rule.Group;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleSet;
import com.buschmais.jqassistant.core.analysis.api.rule.visitor.CollectRulesVisitor;
import com.buschmais.jqassistant.core.analysis.impl.RuleExecutor;

/**
 * Provides utility functionality for creating reports.
 */
public final class RuleHelper {

    public static final String LOG_LINE_PREFIX = "  \"";
    private Console console;

    /**
     * Constructor.
     *
     * @param console
     *            The console to use for printing messages.
     */
    public RuleHelper(Console console) {
        this.console = console;
    }

    /**
     * Logs the given
     * {@link com.buschmais.jqassistant.core.analysis.api.rule.DefaultRuleSet}
     * on level info.
     *
     * @param ruleSet
     *            The
     *            {@link com.buschmais.jqassistant.core.analysis.api.rule.DefaultRuleSet}
     *            .
     */
    public void printRuleSet(RuleSet ruleSet) throws AnalysisException {
        RuleSelection ruleSelection = RuleSelection.Builder.allOf(ruleSet);
        printRuleSet(ruleSet, ruleSelection);
    }

    /**
     *
     * @param ruleSet
     * @param ruleSelection
     * @throws AnalysisException
     */
    public void printRuleSet(RuleSet ruleSet, RuleSelection ruleSelection) throws AnalysisException {
        CollectRulesVisitor visitor = getAllRules(ruleSet, ruleSelection);
        printValidRules(visitor);
        printMissingRules(visitor);
    }

    /**
     * Prints all valid rules.
     * 
     * @param visitor
     *            The visitor.
     */
    private void printValidRules(CollectRulesVisitor visitor) {
        console.info("Groups [" + visitor.getGroups().size() + "]");
        for (Group group : visitor.getGroups()) {
            console.info(LOG_LINE_PREFIX + group.getId() + "\"");
        }
        console.info("Constraints [" + visitor.getConstraints().size() + "]");
        for (Constraint constraint : visitor.getConstraints().keySet()) {
            console.info(LOG_LINE_PREFIX + constraint.getId() + "\" - " + constraint.getDescription());
        }
        console.info("Concepts [" + visitor.getConcepts().size() + "]");
        for (Concept concept : visitor.getConcepts().keySet()) {
            console.info(LOG_LINE_PREFIX + concept.getId() + "\" - " + concept.getDescription());
        }
    }

    /**
     * Determines all rules.
     * 
     * @param ruleSet
     *            The rule set.
     * @return The visitor with all valid and missing rules.
     * @throws AnalysisException
     *             If the rules cannot be evaluated.
     */
    private CollectRulesVisitor getAllRules(RuleSet ruleSet, RuleSelection ruleSelection) throws AnalysisException {
        CollectRulesVisitor visitor = new CollectRulesVisitor();
        RuleExecutor executor = new RuleExecutor(visitor);
        executor.execute(ruleSet, ruleSelection);
        return visitor;
    }

    /**
     * Prints all missing rule ids.
     * 
     * @param visitor
     *            The visitor.
     */
    private boolean printMissingRules(CollectRulesVisitor visitor) {
        Set<String> missingConcepts = visitor.getMissingConcepts();
        if (!missingConcepts.isEmpty()) {
            console.info("Missing concepts [" + missingConcepts.size() + "]");
            for (String missingConcept : missingConcepts) {
                console.warn(LOG_LINE_PREFIX + missingConcept);
            }
        }
        Set<String> missingConstraints = visitor.getMissingConstraints();
        if (!missingConstraints.isEmpty()) {
            console.info("Missing constraints [" + missingConstraints.size() + "]");
            for (String missingConstraint : missingConstraints) {
                console.warn(LOG_LINE_PREFIX + missingConstraint);
            }
        }
        Set<String> missingGroups = visitor.getMissingGroups();
        if (!missingGroups.isEmpty()) {
            console.info("Missing groups [" + missingGroups.size() + "]");
            for (String missingGroup : missingGroups) {
                console.warn(LOG_LINE_PREFIX + missingGroup);
            }
        }
        return missingConcepts.isEmpty() && missingConstraints.isEmpty() && missingGroups.isEmpty();
    }
}
