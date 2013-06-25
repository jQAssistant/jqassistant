package com.buschmais.jqassistant.core.analysis.api.model;

import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: dirk.mahler
 * Date: 24.06.13
 * Time: 13:07
 * To change this template use File | Settings | File Templates.
 */
public class ConstraintViolations {

    private Constraint constraint;

    private List<Map<String, Object>> violations;

    public ConstraintViolations(Constraint constraint, List<Map<String, Object>> violations) {
        this.constraint = constraint;
        this.violations = violations;
    }

    public Constraint getConstraint() {
        return constraint;
    }

    public List<Map<String, Object>> getViolations() {
        return violations;
    }

}
