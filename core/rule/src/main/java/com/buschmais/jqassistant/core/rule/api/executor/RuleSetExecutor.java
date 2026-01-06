package com.buschmais.jqassistant.core.rule.api.executor;

import java.util.*;

import com.buschmais.jqassistant.core.rule.api.configuration.Rule;
import com.buschmais.jqassistant.core.rule.api.model.*;
import com.buschmais.jqassistant.core.rule.api.model.Concept.Activation;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

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
        // determine all explicitly required concepts (transitively), they are needed to identify providing concepts which are explictly required
        ActivatedConceptsVisitor activatedConceptsVisitor = new ActivatedConceptsVisitor();
        new RuleSetExecutor<>(activatedConceptsVisitor, configuration).execute(ruleSet, ruleSelection, emptySet());
        Set<String> userActivatedConcepts = activatedConceptsVisitor.getActivatedConcepts();
        log.debug("Executing with activated concepts: {}", userActivatedConcepts);
        execute(ruleSet, ruleSelection, userActivatedConcepts);
    }

    private void execute(RuleSet ruleSet, RuleSelection ruleSelection, Set<String> activatedConcepts) throws RuleException {
        this.ruleVisitor.beforeRules(ruleSelection);
        try {
            for (String conceptPattern : ruleSelection.getConceptIds()) {
                List<Concept> concepts = applyConcepts(ruleSet, conceptPattern, null, activatedConcepts);
                this.ruleVisitor.includeConcepts(concepts);
            }
            for (String groupPattern : ruleSelection.getGroupIds()) {
                List<Group> groups = executeGroups(ruleSet, groupPattern, ruleSelection.getExcludeConstraintIds(), null, activatedConcepts);
                this.ruleVisitor.includeGroups(groups);
            }
            for (String constraintPattern : ruleSelection.getConstraintIds()) {
                List<Constraint> constraints = validateConstraints(ruleSet, constraintPattern, ruleSelection.getExcludeConstraintIds(), null,
                    activatedConcepts);
                this.ruleVisitor.includeConstraints(constraints);
            }
        } finally {
            this.ruleVisitor.afterRules();
        }
        if (executedConcepts.isEmpty() && executedConstraints.isEmpty()) {
            log.warn("No concepts or constraints were executed.");
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
     * @param overriddenSeverity
     *     The {@link Severity} to override the default rule severity.
     * @param activatedConcepts
     *     The activated concepts
     */
    private void executeGroup(RuleSet ruleSet, Group group, Set<String> excludedConstraintIds, Severity overriddenSeverity, Set<String> activatedConcepts)
        throws RuleException {
        if (ruleSet.getGroupsBucket()
            .isOverridden(group.getId())) {
            Group overridingGroup = (Group) ruleSet.getGroupsBucket()
                .getOverridingRule(group);
            log.info("The group '{}' is overridden by '{}'.", group.getId(), overridingGroup.getId());
            ruleVisitor.overrideGroup(group, overridingGroup);
            executeGroup(ruleSet, (Group) ruleSet.getGroupsBucket()
                .getOverridingRule(group), excludedConstraintIds, overriddenSeverity, activatedConcepts);
        } else if (!executedGroups.contains(group)) {
            Severity groupSeverity = getEffectiveSeverity(overriddenSeverity, group.getSeverity());
            ruleVisitor.beforeGroup(group, groupSeverity);
            for (Map.Entry<String, Severity> conceptEntry : group.getConcepts()
                .entrySet()) {
                List<Concept> concepts = applyConcepts(ruleSet, conceptEntry.getKey(), getEffectiveSeverity(conceptEntry.getValue(), groupSeverity),
                    activatedConcepts);
                this.ruleVisitor.includeConcepts(group, concepts);
            }
            for (Map.Entry<String, Severity> groupEntry : group.getGroups()
                .entrySet()) {
                Severity effectiveSeverity = getEffectiveSeverity(groupEntry.getValue(), groupSeverity);
                List<Group> groups = executeGroups(ruleSet, groupEntry.getKey(), excludedConstraintIds, effectiveSeverity, activatedConcepts);
                this.ruleVisitor.includeGroups(group, groups);
            }
            for (Map.Entry<String, Severity> constraintEntry : group.getConstraints()
                .entrySet()) {
                Severity effectiveSeverity = getEffectiveSeverity(constraintEntry.getValue(), groupSeverity);
                List<Constraint> constraints = validateConstraints(ruleSet, constraintEntry.getKey(), excludedConstraintIds, effectiveSeverity,
                    activatedConcepts);
                this.ruleVisitor.includeConstraints(group, constraints);
            }
            ruleVisitor.afterGroup(group);
            executedGroups.add(group);
        }
    }

    private List<Concept> applyConcepts(RuleSet ruleSet, String conceptPattern, Severity overriddenSeverity, Set<String> activatedConcepts)
        throws RuleException {
        List<Concept> matchingConcepts = ruleSet.getConceptBucket()
            .match(conceptPattern);
        if (matchingConcepts.isEmpty()) {
            LOGGER.warn("Could not find concepts matching to '{}'.", conceptPattern);
        } else {
            for (Concept matchingConcept : matchingConcepts) {
                applyConcept(ruleSet, matchingConcept, overriddenSeverity, activatedConcepts, new LinkedHashSet<>());
            }
        }
        return matchingConcepts;
    }

    private List<Group> executeGroups(RuleSet ruleSet, String groupPattern, Set<String> excludedConstraintIds, Severity overridingSeverity,
        Set<String> activatedConcepts) throws RuleException {
        List<Group> matchingGroups = ruleSet.getGroupsBucket()
            .match(groupPattern);
        if (matchingGroups.isEmpty()) {
            LOGGER.warn("Could not find groups matching to '{}'.", groupPattern);
        } else {
            for (Group matchingGroup : matchingGroups) {
                executeGroup(ruleSet, matchingGroup, excludedConstraintIds, overridingSeverity, activatedConcepts);
            }
        }
        return matchingGroups;
    }

    private List<Constraint> validateConstraints(RuleSet ruleSet, String constraintPattern, Set<String> excludedConstraintIds, Severity overriddenSeverity,
        Set<String> activatedConcepts) throws RuleException {
        List<Constraint> matchingConstraints = ruleSet.getConstraintBucket()
            .match(constraintPattern)
            .stream()
            .filter(constraint -> !isExcludedConstraint(excludedConstraintIds, constraint))
            .collect(toList());
        if (matchingConstraints.isEmpty()) {
            LOGGER.warn("Could not find constraints matching to '{}'.", constraintPattern);
        } else {
            for (Constraint matchingConstraint : matchingConstraints) {
                validateConstraint(ruleSet, matchingConstraint, overriddenSeverity, activatedConcepts);
            }
        }
        return matchingConstraints;
    }

    private static boolean isExcludedConstraint(Set<String> excludedConstraintIds, Constraint constraint) {
        if (excludedConstraintIds.contains(constraint.getId())) {
            log.info("Skipping excluded constraint '{}'.", constraint.getId());
            return true;
        } else {
            return false;
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
     * @param activatedConcepts
     *     The activated concepts
     * @throws RuleException
     *     If the constraint cannot be validated.
     */
    private void validateConstraint(RuleSet ruleSet, Constraint constraint, Severity overriddenSeverity, Set<String> activatedConcepts) throws RuleException {
        Severity effectiveSeverity = getEffectiveSeverity(overriddenSeverity, constraint.getSeverity());
        if (ruleSet.getConstraintBucket()
            .isOverridden(constraint.getId())) {
            Constraint overridingConstraint = (Constraint) ruleSet.getConstraintBucket()
                .getOverridingRule(constraint);
            log.info("The constraint '{}' is overridden by '{}'", constraint.getId(), overridingConstraint.getId());
            ruleVisitor.overrideConstraint(constraint, overridingConstraint);
            ruleVisitor.skipConstraint(constraint, effectiveSeverity, emptyMap());
            validateConstraint(ruleSet, overridingConstraint, overriddenSeverity, activatedConcepts);
        } else if (!executedConstraints.contains(constraint)) {
            Map<Map.Entry<Concept, Boolean>, R> requiredConceptResults = applyRequiredConcepts(ruleSet, constraint, activatedConcepts, new LinkedHashSet<>());
            if (requiredConceptsAreSuccessful(requiredConceptResults)) {
                checkDeprecation(constraint);
                ruleVisitor.visitConstraint(constraint, effectiveSeverity, requiredConceptResults);
            } else {
                ruleVisitor.skipConstraint(constraint, effectiveSeverity, requiredConceptResults);
            }
            this.ruleVisitor.requireConcepts(constraint, requiredConceptResults.keySet()
                .stream()
                .map(Map.Entry::getKey)
                .collect(toSet()));
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
     * @param activatedConcepts
     *     The activated concepts
     * @param executionStack
     *     The {@link Concept}s currently being executed while resolving
     *     required {@link Concept}s.
     * @throws RuleException
     *     If the concept cannot be applied.
     */
    private R applyConcept(RuleSet ruleSet, Concept concept, Severity overriddenSeverity, Set<String> activatedConcepts, Set<Concept> executionStack)
        throws RuleException {
        Severity effectiveSeverity = getEffectiveSeverity(overriddenSeverity, concept.getSeverity());
        if (ruleSet.getConceptBucket()
            .isOverridden(concept.getId())) {
            Concept overridingConcept = (Concept) ruleSet.getConceptBucket()
                .getOverridingRule(concept);
            checkOverridesProvides(concept, overridingConcept);
            log.info("The concept '{}' is overridden by '{}'", concept.getId(), overridingConcept.getId());
            ruleVisitor.overrideConcept(concept, overridingConcept);
            ruleVisitor.skipConcept(concept, effectiveSeverity, emptyMap());
            return applyConcept(ruleSet, overridingConcept, overriddenSeverity, activatedConcepts, executionStack);
        }
        R result = executedConcepts.get(concept);
        if (result == null) {
            executionStack.add(concept);

            Map<Map.Entry<Concept, Boolean>, R> requiredConceptResults = applyAllRequiredConcepts(ruleSet, concept, activatedConcepts, executionStack);
            if (requiredConceptsAreSuccessful(requiredConceptResults)) {
                this.ruleVisitor.requireConcepts(concept, requiredConceptResults.keySet()
                    .stream()
                    .map(Map.Entry::getKey)
                    .collect(toSet()));
                Map<Concept, R> providingConceptResults = applyProvidingConcepts(ruleSet, concept, overriddenSeverity, activatedConcepts, executionStack);
                this.ruleVisitor.provideConcept(concept, providingConceptResults.keySet());
                checkDeprecation(concept);
                result = ruleVisitor.visitConcept(concept, effectiveSeverity, requiredConceptResults, providingConceptResults);
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
     * @param overriddenSeverity
     *     The {@link Severity} to override the default rule severity.
     * @param activatedConcepts
     *     The activated concepts.
     * @param stack
     *     The current execution stack.
     * @throws RuleException
     *     If execution fails.
     */
    private Map<Concept, R> applyProvidingConcepts(RuleSet ruleSet, Concept concept, Severity overriddenSeverity, Set<String> activatedConcepts,
        Set<Concept> stack) throws RuleException {
        Map<Concept, R> results = new LinkedHashMap<>();
        Severity providedSeverity = concept.getSeverity();
        for (Concept.ProvidedConcept providedConcept : ruleSet.getProvidedConcepts()
            .getOrDefault(concept.getId(), emptySet())) {
            Concept providingConcept = ruleSet.getConceptBucket()
                .getById(providedConcept.getProvidingConceptId());
            if (isProvidingConceptActivated(providingConcept, providedConcept.getActivation(), activatedConcepts)) {
                Severity providingSeverity = providingConcept.getSeverity();
                // use overridden severity or highest default severity of provided and providing concept
                Severity effectiveSeverity = getEffectiveSeverity(overriddenSeverity,
                    providedSeverity.getLevel() < providingSeverity.getLevel() ? providedSeverity : providingSeverity);
                R result = applyConcept(ruleSet, providingConcept, effectiveSeverity, activatedConcepts, stack);
                results.put(providingConcept, result);
            }
        }
        return results;
    }

    private static boolean isProvidingConceptActivated(Concept providingConcept, Activation activation, Set<String> activatedConcepts) throws RuleException {
        switch (activation) {
        case IF_AVAILABLE:
            return true;
        case IF_REQUIRED:
            return activatedConcepts.contains(providingConcept.getId());
        default:
            throw new RuleException("Unknown activation strategy " + activation + " for providing concept + " + providingConcept.getId());
        }
    }

    /**
     * Applies all concepts required by a given concept (including required concepts of provided concepts)
     */
    private Map<Map.Entry<Concept, Boolean>, R> applyAllRequiredConcepts(RuleSet ruleSet, Concept concept, Set<String> activatedConcepts, Set<Concept> stack)
        throws RuleException {
        Map<Map.Entry<Concept, Boolean>, R> requiredConcepts = new HashMap<>();
        Set<String> conceptIds = ruleSet.getConceptBucket()
            .getIds();
        for (String providedConceptId : ruleSet.getProvidingConceptIds()
            .getOrDefault(concept.getId(), emptySet())) {
            if (conceptIds.contains(providedConceptId)) {
                Concept providedConcept = ruleSet.getConceptBucket()
                    .getById(providedConceptId);
                requiredConcepts.putAll(applyAllRequiredConcepts(ruleSet, providedConcept, activatedConcepts, stack));
            } else {
                log.warn("Cannot resolve provided concept '{}' (provided by concept '{}').", providedConceptId, concept.getId());
            }
        }
        requiredConcepts.putAll(applyRequiredConcepts(ruleSet, concept, activatedConcepts, stack));
        return requiredConcepts;
    }

    /**
     * Applies the concepts required by a concept.
     */
    private Map<Map.Entry<Concept, Boolean>, R> applyRequiredConcepts(RuleSet ruleSet, ExecutableRule<?> rule, Set<String> activatedConcepts,
        Set<Concept> stack) throws RuleException {
        Map<Map.Entry<Concept, Boolean>, R> requiredConcepts = new HashMap<>();
        for (Map.Entry<String, Boolean> entry : rule.getRequiresConcepts()
            .entrySet()) {
            for (Concept requiredConcept : ruleSet.getConceptBucket()
                .match(entry.getKey())) {
                if (!stack.contains(requiredConcept)) {
                    R conceptResult = applyConcept(ruleSet, requiredConcept, null, activatedConcepts, stack);
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

    /**
     * Checks if an overriding concept provides the same concepts as the overridden concept.
     */
    private void checkOverridesProvides(Concept overridden, Concept overriding) throws RuleException {
        Set<String> overridingConceptsProvides = overriding.getProvidedConcepts()
            .stream()
            .map(Concept.ProvidedConcept::getProvidedConceptId)
            .collect(toSet());
        Set<String> overriddenConceptsProvides = overridden.getProvidedConcepts()
            .stream()
            .map(Concept.ProvidedConcept::getProvidedConceptId)
            .collect(toSet());
        if (!overridingConceptsProvides.containsAll(overriddenConceptsProvides)) {
            throw new RuleException(String.format("Overriding concept '%s' does not have the same ProvidedConcepts as the overridden concept '%s' ",
                overriding.getProvidedConcepts(), overridden.getId()));
        }
    }

    @Getter
    @ToString
    private static class ActivatedConceptsVisitor extends AbstractRuleVisitor<Boolean> {

        private final Set<String> activatedConcepts = new HashSet<>();

        @Override
        public Boolean visitConcept(Concept concept, Severity effectiveSeverity, Map<Map.Entry<Concept, Boolean>, Boolean> requiredConceptResults,
            Map<Concept, Boolean> providingConceptResults) {
            activatedConcepts.add(concept.getId());
            return true;
        }

        @Override
        public Boolean visitConstraint(Constraint constraint, Severity effectiveSeverity, Map<Map.Entry<Concept, Boolean>, Boolean> requiredConceptResults) {
            return true;
        }
    }
}
