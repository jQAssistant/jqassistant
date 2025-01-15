package com.buschmais.jqassistant.core.rule.api;

import java.util.Set;

import com.buschmais.jqassistant.core.rule.api.configuration.Rule;
import com.buschmais.jqassistant.core.rule.api.executor.CollectRulesVisitor;
import com.buschmais.jqassistant.core.rule.api.executor.RuleSetExecutor;
import com.buschmais.jqassistant.core.rule.api.model.*;

import lombok.extern.slf4j.Slf4j;

/**
 * Provides utility functionality for creating reports.
 */
@Slf4j
public class RuleHelper {

    public static final String LOG_LINE_PREFIX = "  \"";

    /**
     * Logs the given rule set on level info.
     *
     * @param ruleSet
     *     The rule set.
     */
    public void printRuleSet(RuleSet ruleSet, Rule configuration) throws RuleException {
        RuleSelection ruleSelection = RuleSelection.builder()
            .conceptIds(ruleSet.getConceptBucket()
                .getIds())
            .constraintIds(ruleSet.getConstraintBucket()
                .getIds())
            .groupIds(ruleSet.getGroupsBucket()
                .getIds())
            .build();
        printRuleSet(ruleSet, ruleSelection, configuration);
    }

    /**
     * @param ruleSet The {@link RuleSet}
     * @param ruleSelection The {@link RuleSelection}
     * @throws RuleException If there's a problem.
     */
    public void printRuleSet(RuleSet ruleSet, RuleSelection ruleSelection, Rule configuration) throws RuleException {
        CollectRulesVisitor visitor = getAllRules(ruleSet, ruleSelection, configuration);
        printValidRules(visitor);
        printMissingRules(visitor);
    }

    /**
     * Prints all valid rules.
     *
     * @param visitor
     *     The visitor.
     */
    private void printValidRules(CollectRulesVisitor visitor) {
        log.info("Groups [" + visitor.getGroups()
            .size() + "]");
        for (Group group : visitor.getGroups()) {
            log.info(LOG_LINE_PREFIX + group.getId() + "\"");
        }
        log.info("Constraints [" + visitor.getConstraints()
            .size() + "]");
        for (Constraint constraint : visitor.getConstraints()
            .keySet()) {
            log.info(LOG_LINE_PREFIX + constraint.getId() + "\" - " + constraint.getDescription());
        }
        log.info("Concepts [" + visitor.getConcepts()
            .size() + "]");
        for (Concept concept : visitor.getConcepts()
            .keySet()) {
            log.info(LOG_LINE_PREFIX + concept.getId() + "\" - " + concept.getDescription());
        }
    }

    /**
     * Determines all rules.
     *
     * @param ruleSet
     *     The rule set.
     * @param configuration
     *     The {@link com.buschmais.jqassistant.core.rule.api.configuration.Rule} configuration.
     * @return The visitor with all valid and missing rules.
     * @throws RuleException
     *     If the rules cannot be evaluated.
     */
    public CollectRulesVisitor getAllRules(RuleSet ruleSet, RuleSelection ruleSelection, Rule configuration) throws RuleException {
        CollectRulesVisitor visitor = new CollectRulesVisitor();
        RuleSetExecutor<Boolean> executor = new RuleSetExecutor<>(visitor, configuration);
        executor.execute(ruleSet, ruleSelection);
        return visitor;
    }

    /**
     * Prints all missing rule ids.
     *
     * @param visitor
     *     The visitor.
     */
    private void printMissingRules(CollectRulesVisitor visitor) {
        Set<String> missingConcepts = visitor.getMissingConcepts();
        if (!missingConcepts.isEmpty()) {
            log.info("Missing concepts [{}]", missingConcepts.size());
            for (String missingConcept : missingConcepts) {
                log.warn(LOG_LINE_PREFIX + "{}", missingConcept);
            }
        }
        Set<String> missingConstraints = visitor.getMissingConstraints();
        if (!missingConstraints.isEmpty()) {
            log.info("Missing constraints [{}]", missingConstraints.size());
            for (String missingConstraint : missingConstraints) {
                log.warn(LOG_LINE_PREFIX + "{}", missingConstraint);
            }
        }
        Set<String> missingGroups = visitor.getMissingGroups();
        if (!missingGroups.isEmpty()) {
            log.info("Missing groups [{}]", missingGroups.size());
            for (String missingGroup : missingGroups) {
                log.warn(LOG_LINE_PREFIX + "{}", missingGroup);
            }
        }
    }
}
