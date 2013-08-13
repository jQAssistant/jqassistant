package com.buschmais.jqassistant.core.model.api.rules;

import java.util.HashSet;
import java.util.Set;

/**
 * Defines a constraint group.
 */
public class AnalysisGroup {

    private String id;

    private Set<Concept> concepts = new HashSet<>();

    private Set<Constraint> constraints = new HashSet<Constraint>();

    private Set<AnalysisGroup> analysisGroups = new HashSet<AnalysisGroup>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Set<Concept> getConcepts() {
        return concepts;
    }

    public void setConcepts(Set<Concept> concepts) {
        this.concepts = concepts;
    }

    public Set<Constraint> getConstraints() {
        return constraints;
    }

    public void setConstraints(Set<Constraint> constraints) {
        this.constraints = constraints;
    }

    public Set<AnalysisGroup> getAnalysisGroups() {
        return analysisGroups;
    }

    public void setAnalysisGroups(Set<AnalysisGroup> analysisGroups) {
        this.analysisGroups = analysisGroups;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnalysisGroup that = (AnalysisGroup) o;
        if (!id.equals(that.id)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Constraint Group " + id;
    }


}
