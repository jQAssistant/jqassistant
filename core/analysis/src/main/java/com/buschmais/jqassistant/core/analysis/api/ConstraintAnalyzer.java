package com.buschmais.jqassistant.core.analysis.api;

import com.buschmais.jqassistant.core.model.api.ConstraintGroup;

/**
 * Defines the interface for the constraint analyzer.
 */
public interface ConstraintAnalyzer {

    /**
     * Validates the constraints which are referred to in the given constraint groups.
     *
     * @param constraintGroups The constraint groups.
     */
    void validateConstraints(Iterable<ConstraintGroup> constraintGroups);

}
