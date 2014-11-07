package com.buschmais.jqassistant.core.analysis.api.rule.visitor;

import java.util.*;

import com.buschmais.jqassistant.core.analysis.api.AnalysisException;
import com.buschmais.jqassistant.core.analysis.api.rule.*;

public class CollectRulesVisitor extends AbstractRuleVisitor {

    private static final Comparator<Rule> RULE_COMPARATOR = new Comparator<Rule>() {
        @Override
        public int compare(Rule o1, Rule o2) {
            return o1.getId().compareTo(o2.getId());
        }
    };

    private Map<Concept, Severity> concepts = new TreeMap<>(RULE_COMPARATOR);
    private Map<Constraint, Severity> constraints = new TreeMap<>(RULE_COMPARATOR);
    private Set<Group> groups = new TreeSet<>(RULE_COMPARATOR);

    private Set<String> missingConcepts = new TreeSet<>();
    private Set<String> missingConstraints = new TreeSet<>();
    private Set<String> missingGroups = new TreeSet<>();

    @Override
    public void visitConcept(Concept concept, Severity severity) throws AnalysisException {
        concepts.put(concept, severity);
    }

    @Override
    public void visitConstraint(Constraint constraint, Severity severity) throws AnalysisException {
        constraints.put(constraint, severity);
    }

    @Override
    public void beforeGroup(Group group) throws AnalysisException {
        groups.add(group);
    }

    @Override
    public boolean missingConcept(String id) {
        missingConcepts.add(id);
        return true;
    }

    @Override
    public boolean missingConstraint(String id) {
        missingConstraints.add(id);
        return true;
    }

    @Override
    public boolean missingGroup(String id) {
        missingGroups.add(id);
        return true;
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

    public Set<String> getMissingConcepts() {
        return missingConcepts;
    }

    public Set<String> getMissingConstraints() {
        return missingConstraints;
    }

    public Set<String> getMissingGroups() {
        return missingGroups;
    }
}
