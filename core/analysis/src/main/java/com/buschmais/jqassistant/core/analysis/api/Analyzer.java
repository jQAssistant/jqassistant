package com.buschmais.jqassistant.core.analysis.api;

import com.buschmais.jqassistant.core.model.api.rules.Concept;
import com.buschmais.jqassistant.core.model.api.rules.Constraint;
import com.buschmais.jqassistant.core.model.api.rules.Group;
import com.buschmais.jqassistant.report.api.ReportWriterException;

/**
 * Defines the interface for the constraint analyzer.
 */
public interface Analyzer {

    /**
     * Executes the given groups.
     *
     * @param groups The groups.
     * @throws ReportWriterException If the report cannot be written.
     */
    void executeGroups(Iterable<Group> groups) throws ReportWriterException;

    /**
     * Execute the given group.
     *
     * @param group The group.
     * @throws ReportWriterException If the report cannot be written.
     */
    void executeGroup(Group group) throws ReportWriterException;


    /**
     * Validates the given constraints.
     *
     * @param constraints The constraints.
     * @throws ReportWriterException If the report cannot be written.
     */
    void validateConstraints(Iterable<Constraint> constraints) throws ReportWriterException;

    /**
     * Validates the given constraint.
     *
     * @param constraint The constraint.
     * @throws ReportWriterException If the report cannot be written.
     */
    void validateConstraint(Constraint constraint) throws ReportWriterException;

    /**
     * Applies given concepts.
     *
     * @param concepts The concept.
     * @throws ReportWriterException If the report cannot be written.
     */
    void applyConcepts(Iterable<Concept> concepts) throws ReportWriterException;

    /**
     * Applies given concept.
     *
     * @param concept The concept.
     * @throws ReportWriterException If the report cannot be written.
     */
    void applyConcept(Concept concept) throws ReportWriterException;
}
