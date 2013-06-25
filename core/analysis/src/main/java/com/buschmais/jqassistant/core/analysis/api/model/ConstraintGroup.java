package com.buschmais.jqassistant.core.analysis.api.model;

import java.util.HashSet;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: dimahler
 * Date: 6/21/13
 * Time: 3:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class ConstraintGroup {

    private String id;

    private Set<Constraint> constraints = new HashSet<Constraint>();

    private Set<ConstraintGroup> constraintGroups = new HashSet<ConstraintGroup>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Set<Constraint> getConstraints() {
        return constraints;
    }

    public void setConstraints(Set<Constraint> constraints) {
        this.constraints = constraints;
    }

    public Set<ConstraintGroup> getConstraintGroups() {
        return constraintGroups;
    }

    public void setConstraintGroups(Set<ConstraintGroup> constraintGroups) {
        this.constraintGroups = constraintGroups;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConstraintGroup that = (ConstraintGroup) o;
        if (!id.equals(that.id)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
