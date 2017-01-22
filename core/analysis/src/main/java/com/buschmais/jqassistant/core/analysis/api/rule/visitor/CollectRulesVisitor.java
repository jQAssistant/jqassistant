package com.buschmais.jqassistant.core.analysis.api.rule.visitor;

import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.buschmais.jqassistant.core.analysis.api.AnalysisException;
import com.buschmais.jqassistant.core.analysis.api.rule.AbstractRuleVisitor;
import com.buschmais.jqassistant.core.analysis.api.rule.Concept;
import com.buschmais.jqassistant.core.analysis.api.rule.Constraint;
import com.buschmais.jqassistant.core.analysis.api.rule.Group;
import com.buschmais.jqassistant.core.analysis.api.rule.Rule;
import com.buschmais.jqassistant.core.analysis.api.rule.Severity;

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
    public boolean visitConcept(Concept concept, Severity effectiveSeverity) throws AnalysisException {
        concepts.put(concept, effectiveSeverity);
        return true;
    }

    @Override
    public void  visitConstraint(Constraint constraint, Severity effectiveSeverity) throws AnalysisException {
        constraints.put(constraint, effectiveSeverity);
    }

    @Override
    public void beforeGroup(Group group, Severity effectiveSeverity) throws AnalysisException {
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
