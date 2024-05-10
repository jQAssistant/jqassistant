package com.buschmais.jqassistant.core.rule.api.executor;

import java.util.*;

import com.buschmais.jqassistant.core.rule.api.configuration.Rule;
import com.buschmais.jqassistant.core.rule.api.model.*;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Collections.emptySet;

/**
 * Controls execution of {@link RuleSet}s.
 * <p/>
 * Execution order is determined by
 * <ul>
 * <li>Declared dependencies between rules, i.e. required concepts</li>
 * <li>Group hierarchy: first concepts of a group, second all nested groups and
 * finally all constraints of a group.</li>
 * </ul>
 * The second rule ensures that before any constraint of a group is validated
 * all concepts and nested concepts are applied. This allows for non-explicit
 * dependencies between rules, e.g. a constraint relying on a specific label may
 * include results of concepts that are not explicitly required.
 */
@Slf4j
public class RuleSetExecutor<R> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RuleSetExecutor.class);

    private final Map<Concept, R> executedConcepts = new HashMap<>();

    private final Set<Constraint> executedConstraints = new LinkedHashSet<>();

    private final Set<Group> executedGroups = new LinkedHashSet<>();

    private final RuleVisitor<R> ruleVisitor;

    private final Rule configuration;

    public RuleSetExecutor(RuleVisitor<R> ruleVisitor, Rule configuration) {
        this.ruleVisitor = ruleVisitor;
        this.configuration = configuration;
    }

    public void execute(RuleSet ruleSet, RuleSelection ruleSelection) throws RuleException {
        this.ruleVisitor.beforeRules();
        try {
            for (String conceptPattern : ruleSelection.getConceptIds()) {
                applyConcepts(ruleSet, conceptPattern, null, null);
            }
            for (String groupPattern : ruleSelection.getGroupIds()) {
                executeGroups(ruleSet, groupPattern, null, null);
            }
            for (String constraintPattern : ruleSelection.getConstraintIds()) {
                validateConstraints(ruleSet, constraintPattern, null, null);
            }
        } finally {
            this.ruleVisitor.afterRules();
        }
    }

    /**
     * Executes the given group.
     *
     * @param ruleSet
     *     The rule set.
     * @param group
     *     The group.
     * @param parentSeverity
     *     The severity.
     */
    private void executeGroup(RuleSet ruleSet, Group group, Severity parentSeverity) throws RuleException {
        if (!executedGroups.contains(group)) {
            ruleVisitor.beforeGroup(group, getEffectiveSeverity(group, parentSeverity, parentSeverity));
            for (Map.Entry<String, Severity> conceptEntry : group.getConcepts()
                .entrySet()) {
                applyConcepts(ruleSet, conceptEntry.getKey(), parentSeverity, conceptEntry.getValue());
            }
            for (Map.Entry<String, Severity> groupEntry : group.getGroups()
                .entrySet()) {
                executeGroups(ruleSet, groupEntry.getKey(), parentSeverity, groupEntry.getValue());
            }
            Map<String, Severity> constraints = group.getConstraints();
            for (Map.Entry<String, Severity> constraintEntry : constraints.entrySet()) {
                validateConstraints(ruleSet, constraintEntry.getKey(), parentSeverity, constraintEntry.getValue());
            }
            ruleVisitor.afterGroup(group);
            executedGroups.add(group);
        }
    }

    private void applyConcepts(RuleSet ruleSet, String conceptPattern, Severity parentSeverity, Severity requestedSeverity) throws RuleException {
        List<Concept> matchingConcepts = ruleSet.getConceptBucket()
            .match(conceptPattern);
        if (matchingConcepts.isEmpty()) {
            LOGGER.warn("Could not find concepts matching to '{}'.", conceptPattern);
        } else {
            for (Concept matchingConcept : matchingConcepts) {
                applyConcept(ruleSet, matchingConcept, parentSeverity, requestedSeverity, new LinkedHashSet<>());
            }
        }
    }

    private void executeGroups(RuleSet ruleSet, String groupPattern, Severity parentSeverity, Severity requestedSeverity) throws RuleException {
        List<Group> matchingGroups = ruleSet.getGroupsBucket()
            .match(groupPattern);
        if (matchingGroups.isEmpty()) {
            LOGGER.warn("Could not find groups matching to '{}'.", groupPattern);
        } else {
            for (Group matchingGroup : matchingGroups) {
                executeGroup(ruleSet, matchingGroup, getEffectiveSeverity(matchingGroup, parentSeverity, requestedSeverity));
            }
        }
    }

    private void validateConstraints(RuleSet ruleSet, String constraintPattern, Severity parentSeverity, Severity requestedSeverity) throws RuleException {
        List<Constraint> matchingConstraints = ruleSet.getConstraintBucket()
            .match(constraintPattern);
        if (matchingConstraints.isEmpty()) {
            LOGGER.warn("Could not find constraints matching to '{}'.", constraintPattern);
        } else {
            for (Constraint matchingConstraint : matchingConstraints) {
                validateConstraint(ruleSet, matchingConstraint, parentSeverity, requestedSeverity);
            }
        }
    }

    /**
     * Determines the effective severity for a rule to be executed.
     *
     * @param rule
     *     The rule.
     * @param parentSeverity
     *     The severity inherited from the parent group.
     * @param includeSeverity
     *     The severity as specified on the rule in the parent group.
     * @return The effective severity.
     */
    private Severity getEffectiveSeverity(SeverityRule rule, Severity parentSeverity, Severity includeSeverity) {
        Severity inheritedSeverity = includeSeverity != null ? includeSeverity : parentSeverity;
        return inheritedSeverity != null ? inheritedSeverity : rule.getSeverity();
    }

    /**
     * Validates the given constraint.
     *
     * @param ruleSet
     *     The {@link RuleSet}.
     * @param constraint
     *     The constraint.
     * @param groupSeverity
     *     The {@link Severity} inherited from the parent.
     * @param includeSeverity
     *     The {@link Severity} as request as incluude inherited from the parent group.
     * @throws RuleException
     *     If the constraint cannot be validated.
     */
    private void validateConstraint(RuleSet ruleSet, Constraint constraint, Severity groupSeverity, Severity includeSeverity) throws RuleException {
        if (!executedConstraints.contains(constraint)) {
            Severity effectiveSeverity = getEffectiveSeverity(constraint, groupSeverity, includeSeverity);
            if (applyRequiredConcepts(ruleSet, constraint, new LinkedHashSet<>())) {
                checkDeprecation(constraint);
                ruleVisitor.visitConstraint(constraint, effectiveSeverity);
            } else {
                ruleVisitor.skipConstraint(constraint, effectiveSeverity);
            }
            executedConstraints.add(constraint);
        }
    }

    /**
     * Applies the given concept.
     *
     * @param ruleSet
     *     The {@link RuleSet}.
     * @param concept
     *     The concept.
     * @param groupSeverity
     *     The {@link Severity} inherited from the parent.
     * @param includeSeverity
     *     The {@link Severity} as request as incluude inherited from the parent group.
     * @param executionStack
     *     The {@link Concept}s currently being executed while resolving
     *     required {@link Concept}s.
     * @throws RuleException
     *     If the concept cannot be applied.
     */
    private R applyConcept(RuleSet ruleSet, Concept concept, Severity groupSeverity, Severity includeSeverity, Set<Concept> executionStack)
        throws RuleException {
        R result = executedConcepts.get(concept);
        if (result == null) {
            executionStack.add(concept);
            Severity effectiveSeverity = getEffectiveSeverity(concept, groupSeverity, includeSeverity);
            if (applyAllRequiredConcepts(ruleSet, concept, executionStack)) {
                Map<Concept, R> providedConceptResults = applyProvidingConcepts(ruleSet, concept, executionStack);
                checkDeprecation(concept);
                result = ruleVisitor.visitConcept(concept, effectiveSeverity, providedConceptResults);
            } else {
                ruleVisitor.skipConcept(concept, effectiveSeverity);
            }
            executionStack.remove(concept);
            executedConcepts.put(concept, result);
        }
        return result;
    }

    /**
     * Executes all concepts that provide to the given concept applying its severity.
     *
     * @param ruleSet
     *     The {@link RuleSet}.
     * @param concept
     *     The {@link Concept}.
     * @param stack
     *     The current execution stack.
     * @throws RuleException
     *     If execution fails.
     */
    private Map<Concept, R> applyProvidingConcepts(RuleSet ruleSet, Concept concept, Set<Concept> stack) throws RuleException {
        Map<Concept, R> results = new LinkedHashMap<>();
        for (String providingConceptId : ruleSet.getProvidedConcepts()
            .getOrDefault(concept.getId(), emptySet())) {
            Concept providingConcept = ruleSet.getConceptBucket()
                .getById(providingConceptId);
            R result = applyConcept(ruleSet, providingConcept, null, null, stack);
            results.put(providingConcept, result);
        }
        return results;
    }

    /**
     * Applies all concepts required by a given concept (including required concepts of provided concepts)
     */
    private boolean applyAllRequiredConcepts(RuleSet ruleSet, Concept concept, Set<Concept> stack) throws RuleException {
        boolean requiredConceptsApplied = true;
        Set<String> conceptIds = ruleSet.getConceptBucket()
            .getIds();
        for (String providedConceptId : ruleSet.getProvidingConcepts()
            .getOrDefault(concept.getId(), emptySet())) {
            if (conceptIds.contains(providedConceptId)) {
                Concept providedConcept = ruleSet.getConceptBucket()
                    .getById(providedConceptId);
                requiredConceptsApplied = requiredConceptsApplied && applyAllRequiredConcepts(ruleSet, providedConcept, stack);
            } else {
                log.warn("Cannot resolve provided concept '{}' (provided by concept '{}').", providedConceptId, concept.getId());
            }
        }
        return requiredConceptsApplied && applyRequiredConcepts(ruleSet, concept, stack);
    }

    /**
     * Applies the concepts required by a concept.
     */
    private boolean applyRequiredConcepts(RuleSet ruleSet, ExecutableRule<?> rule, Set<Concept> stack) throws RuleException {
        boolean requiredConceptsApplied = true;
        for (Map.Entry<String, Boolean> entry : rule.getRequiresConcepts()
            .entrySet()) {
            List<Concept> requiredConcepts = ruleSet.getConceptBucket()
                .match(entry.getKey());
            for (Concept requiredConcept : requiredConcepts) {
                if (!stack.contains(requiredConcept)) {
                    R conceptResult = applyConcept(ruleSet, requiredConcept, null, null, stack);
                    Boolean optional = entry.getValue();
                    if (optional == null) {
                        optional = configuration.requiredConceptsAreOptionalByDefault();
                    }
                    requiredConceptsApplied = ruleVisitor.isSuccess(conceptResult) || optional;
                }
            }
        }
        return requiredConceptsApplied;
    }

    private void checkDeprecation(ExecutableRule<?> executableRule) {
        String deprecation = executableRule.getDeprecation();
        if (deprecation != null) {
            log.warn("Rule '{}' is deprecated: {} ({})", executableRule.getId(), executableRule.getDeprecation(), executableRule.getSource()
                .getId());
        }
    }
}
