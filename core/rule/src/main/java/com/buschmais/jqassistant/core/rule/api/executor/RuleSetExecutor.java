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
                applyConcepts(ruleSet, conceptPattern, null);
            }
            for (String groupPattern : ruleSelection.getGroupIds()) {
                executeGroups(ruleSet, groupPattern, ruleSelection.getExcludeConstraintIds(), null);
            }
            for (String constraintPattern : ruleSelection.getConstraintIds()) {
                validateConstraints(ruleSet, constraintPattern, ruleSelection.getExcludeConstraintIds(), null);
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
     * @param excludedConstraintIds
     *     The {@link Set} of constraint ids that shall be excluded from execution.
     * @param overridingSeverity
     *     The severity.
     */
    private void executeGroup(RuleSet ruleSet, Group group, Set<String> excludedConstraintIds, Severity overridingSeverity) throws RuleException {
        if (!executedGroups.contains(group)) {
            Severity groupSeverity = getEffectiveSeverity(overridingSeverity, group.getSeverity());
            ruleVisitor.beforeGroup(group, groupSeverity);
            for (Map.Entry<String, Severity> conceptEntry : group.getConcepts()
                .entrySet()) {
                applyConcepts(ruleSet, conceptEntry.getKey(), getEffectiveSeverity(conceptEntry.getValue(), groupSeverity));
            }
            for (Map.Entry<String, Severity> groupEntry : group.getGroups()
                .entrySet()) {
                Severity effectiveSeverity = getEffectiveSeverity(groupEntry.getValue(), groupSeverity);
                executeGroups(ruleSet, groupEntry.getKey(), excludedConstraintIds, effectiveSeverity);
            }
            Map<String, Severity> constraints = group.getConstraints();
            for (Map.Entry<String, Severity> constraintEntry : constraints.entrySet()) {
                Severity effectiveSeverity = getEffectiveSeverity(constraintEntry.getValue(), groupSeverity);
                validateConstraints(ruleSet, constraintEntry.getKey(), excludedConstraintIds, effectiveSeverity);
            }
            ruleVisitor.afterGroup(group);
            executedGroups.add(group);
        }
    }

    private void applyConcepts(RuleSet ruleSet, String conceptPattern, Severity overriddenSeverity) throws RuleException {
        List<Concept> matchingConcepts = ruleSet.getConceptBucket()
            .match(conceptPattern);
        if (matchingConcepts.isEmpty()) {
            LOGGER.warn("Could not find concepts matching to '{}'.", conceptPattern);
        } else {
            for (Concept matchingConcept : matchingConcepts) {
                applyConcept(ruleSet, matchingConcept, overriddenSeverity, new LinkedHashSet<>());
            }
        }
    }

    private void executeGroups(RuleSet ruleSet, String groupPattern, Set<String> excludedConstraintIds, Severity overridingSeverity) throws RuleException {
        List<Group> matchingGroups = ruleSet.getGroupsBucket()
            .match(groupPattern);
        if (matchingGroups.isEmpty()) {
            LOGGER.warn("Could not find groups matching to '{}'.", groupPattern);
        } else {
            for (Group matchingGroup : matchingGroups) {
                executeGroup(ruleSet, matchingGroup, excludedConstraintIds, overridingSeverity);
            }
        }
    }

    private void validateConstraints(RuleSet ruleSet, String constraintPattern, Set<String> excludedConstraintIds, Severity overriddenSeverity)
        throws RuleException {
        List<Constraint> matchingConstraints = ruleSet.getConstraintBucket()
            .match(constraintPattern);
        if (matchingConstraints.isEmpty()) {
            LOGGER.warn("Could not find constraints matching to '{}'.", constraintPattern);
        } else {
            for (Constraint matchingConstraint : matchingConstraints) {
                String constraintId = matchingConstraint.getId();
                if (excludedConstraintIds.contains(constraintId)) {
                    log.info("Skipping excluded constraint '{}'.", constraintId);
                } else {
                    validateConstraint(ruleSet, matchingConstraint, overriddenSeverity);
                }
            }
        }
    }

    /**
     * Determines the effective severity.
     *
     * @return The effective severity.
     */
    private Severity getEffectiveSeverity(Severity... severities) {
        for (Severity severity : severities) {
            if (severity != null) {
                return severity;
            }
        }
        return null;
    }

    /**
     * Validates the given constraint.
     *
     * @param ruleSet
     *     The {@link RuleSet}.
     * @param constraint
     *     The constraint.
     * @param overriddenSeverity
     *     The {@link Severity} to override the default rule severity.
     * @throws RuleException
     *     If the constraint cannot be validated.
     */
    private void validateConstraint(RuleSet ruleSet, Constraint constraint, Severity overriddenSeverity) throws RuleException {
        if (!executedConstraints.contains(constraint)) {
            Severity effectiveSeverity = getEffectiveSeverity(overriddenSeverity, constraint.getSeverity());
            Map<Map.Entry<Concept, Boolean>, R> requiredConceptResults = applyRequiredConcepts(ruleSet, constraint, new LinkedHashSet<>());
            if (requiredConceptsAreSuccessful(requiredConceptResults)) {
                checkDeprecation(constraint);
                ruleVisitor.visitConstraint(constraint, effectiveSeverity, requiredConceptResults);
            } else {
                ruleVisitor.skipConstraint(constraint, effectiveSeverity, requiredConceptResults);
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
     * @param executionStack
     *     The {@link Concept}s currently being executed while resolving
     *     required {@link Concept}s.
     * @throws RuleException
     *     If the concept cannot be applied.
     */
    private R applyConcept(RuleSet ruleSet, Concept concept, Severity overriddenSeverity, Set<Concept> executionStack) throws RuleException {
        R result = executedConcepts.get(concept);
        if (result == null) {
            executionStack.add(concept);
            Severity effectiveSeverity = getEffectiveSeverity(overriddenSeverity, concept.getSeverity());
            Map<Map.Entry<Concept, Boolean>, R> requiredConceptResults = applyAllRequiredConcepts(ruleSet, concept, executionStack);
            if (requiredConceptsAreSuccessful(requiredConceptResults)) {
                Map<Concept, R> providedConceptResults = applyProvidingConcepts(ruleSet, concept, executionStack);
                checkDeprecation(concept);
                result = ruleVisitor.visitConcept(concept, effectiveSeverity, requiredConceptResults, providedConceptResults);
            } else {
                ruleVisitor.skipConcept(concept, effectiveSeverity, requiredConceptResults);
            }
            executionStack.remove(concept);
            executedConcepts.put(concept, result);
        }
        return result;
    }

    private boolean requiredConceptsAreSuccessful(Map<Map.Entry<Concept, Boolean>, R> requiredConceptResults) {
        return requiredConceptResults.entrySet()
            .stream()
            .allMatch(entry -> {
                Boolean isOptional = entry.getKey()
                    .getValue();
                return ((isOptional == null ? configuration.requiredConceptsAreOptionalByDefault() : isOptional) || ruleVisitor.isSuccess(entry.getValue()));
            });
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
            R result = applyConcept(ruleSet, providingConcept, null, stack);
            results.put(providingConcept, result);
        }
        return results;
    }

    /**
     * Applies all concepts required by a given concept (including required concepts of provided concepts)
     */
    private Map<Map.Entry<Concept, Boolean>, R> applyAllRequiredConcepts(RuleSet ruleSet, Concept concept, Set<Concept> stack) throws RuleException {
        Map<Map.Entry<Concept, Boolean>, R> requiredConcepts = new HashMap<>();
        Set<String> conceptIds = ruleSet.getConceptBucket()
            .getIds();
        for (String providedConceptId : ruleSet.getProvidingConcepts()
            .getOrDefault(concept.getId(), emptySet())) {
            if (conceptIds.contains(providedConceptId)) {
                Concept providedConcept = ruleSet.getConceptBucket()
                    .getById(providedConceptId);
                requiredConcepts.putAll(applyAllRequiredConcepts(ruleSet, providedConcept, stack));
            } else {
                log.warn("Cannot resolve provided concept '{}' (provided by concept '{}').", providedConceptId, concept.getId());
            }
        }
        requiredConcepts.putAll(applyRequiredConcepts(ruleSet, concept, stack));
        return requiredConcepts;
    }

    /**
     * Applies the concepts required by a concept.
     */
    private Map<Map.Entry<Concept, Boolean>, R> applyRequiredConcepts(RuleSet ruleSet, ExecutableRule<?> rule, Set<Concept> stack) throws RuleException {
        Map<Map.Entry<Concept, Boolean>, R> requiredConcepts = new HashMap<>();
        for (Map.Entry<String, Boolean> entry : rule.getRequiresConcepts()
            .entrySet()) {
            for (Concept requiredConcept : ruleSet.getConceptBucket()
                .match(entry.getKey())) {
                if (!stack.contains(requiredConcept)) {
                    R conceptResult = applyConcept(ruleSet, requiredConcept, null, stack);
                    requiredConcepts.put(new AbstractMap.SimpleEntry<>(requiredConcept, entry.getValue()), conceptResult);
                }
            }
        }
        return requiredConcepts;
    }

    private void checkDeprecation(ExecutableRule<?> executableRule) {
        String deprecation = executableRule.getDeprecation();
        if (deprecation != null) {
            log.warn("Rule '{}' is deprecated: {} ({})", executableRule.getId(), executableRule.getDeprecation(), executableRule.getSource()
                .getId());
        }
    }
}
