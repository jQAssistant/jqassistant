package com.buschmais.jqassistant.core.analysis.api;

import com.buschmais.jqassistant.core.model.api.rules.Concept;
import com.buschmais.jqassistant.core.model.api.rules.Constraint;
import com.buschmais.jqassistant.core.model.api.rules.ConstraintGroup;
import com.buschmais.jqassistant.report.api.ReportWriterException;

/**
 * Defines the interface for the constraint analyzer.
 */
public interface Analyzer {

    /**
     * Validates the constraints which are referred to in the given constraint groups.
     *
     * @param constraintGroups The constraint groups.
     * @throws ReportWriterException If the report cannot be written.
     */
    void validateConstraintGroups(Iterable<ConstraintGroup> constraintGroups) throws ReportWriterException;

    /**
     * Validates the constraints which are referred to in the given constraint group.
     *
     * @param constraintGroup The constraint group.
     * @throws ReportWriterException If the report cannot be written.
     */
    void validateConstraintGroup(ConstraintGroup constraintGroup) throws ReportWriterException;


    /**
     * Validates the given constraint.
     *
     * @param constraint The constraint.
     * @throws ReportWriterException If the report cannot be written.
     */
    void validateConstraint(Constraint constraint) throws ReportWriterException;

    /**
     * Applies given concept.
     *
     * @param concept The concept.
     * @throws ReportWriterException If the report cannot be written.
     */
    void applyConcept(Concept concept) throws ReportWriterException;
}
