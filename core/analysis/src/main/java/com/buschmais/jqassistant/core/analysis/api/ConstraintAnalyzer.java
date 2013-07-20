package com.buschmais.jqassistant.core.analysis.api;

import com.buschmais.jqassistant.core.analysis.api.model.Concept;
import com.buschmais.jqassistant.core.analysis.api.model.Constraint;
import com.buschmais.jqassistant.core.analysis.api.model.ConstraintGroup;
import com.buschmais.jqassistant.core.analysis.api.model.Result;

import java.util.List;

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

    /**
     * Returns the result of the applied concepts.
     *
     * @return The result of the applied concepts.
     */
    List<Result<Concept>> getConceptResults();

    /**
     * Returns the violations result of the validated constraints.
     *
     * @return The violations result of the validated constraints.
     */
    List<Result<Constraint>> getConstraintViolations();

}
