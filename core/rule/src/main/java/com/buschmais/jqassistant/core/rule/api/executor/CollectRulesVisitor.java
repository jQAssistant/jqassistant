package com.buschmais.jqassistant.core.rule.api.executor;

import java.util.*;

import com.buschmais.jqassistant.core.rule.api.model.*;

public class CollectRulesVisitor extends AbstractRuleVisitor<Boolean> {

    private static final Comparator<Rule> RULE_COMPARATOR = Comparator.comparing(Rule::getId);

    private Map<Concept, Severity> concepts = new TreeMap<>(RULE_COMPARATOR);
    private Map<Constraint, Severity> constraints = new TreeMap<>(RULE_COMPARATOR);
    private Set<Group> groups = new TreeSet<>(RULE_COMPARATOR);

    @Override
    public Boolean visitConcept(Concept concept, Severity effectiveSeverity, Map<Map.Entry<Concept, Boolean>, Boolean> requiredConceptResults,
        Map<Concept, Boolean> providingConceptResults) {
        concepts.put(concept, effectiveSeverity);
        return true;
    }

    @Override
    public Boolean visitConstraint(Constraint constraint, Severity effectiveSeverity, Map<Map.Entry<Concept, Boolean>, Boolean> requiredConceptResults) {
        constraints.put(constraint, effectiveSeverity);
        return true;
    }

    @Override
    public void beforeGroup(Group group, Severity effectiveSeverity) {
        groups.add(group);
    }

    public Map<Concept, Severity> getConcepts() {
        return concepts;
    }

    public Map<Constraint, Severity> getConstraints() {
        return constraints;
    }

    public Set<Group> getGroups() {
        return groups;
    }

}
