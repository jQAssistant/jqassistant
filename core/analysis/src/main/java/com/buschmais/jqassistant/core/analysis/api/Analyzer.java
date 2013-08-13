package com.buschmais.jqassistant.core.analysis.api;

import com.buschmais.jqassistant.core.model.api.rules.Concept;
import com.buschmais.jqassistant.core.model.api.rules.Constraint;
import com.buschmais.jqassistant.core.model.api.rules.AnalysisGroup;
import com.buschmais.jqassistant.report.api.ReportWriterException;

/**
 * Defines the interface for the constraint analyzer.
 */
public interface Analyzer {

    /**
     * Executes the given analysis groups.
     *
     * @param analysisGroups The analysis groups.
     * @throws ReportWriterException If the report cannot be written.
     */
    void executeAnalysisGroups(Iterable<AnalysisGroup> analysisGroups) throws ReportWriterException;

    /**
     * Execute the given analysis group.
     *
     * @param analysisGroup The analysis group.
     * @throws ReportWriterException If the report cannot be written.
     */
    void executeAnalysisGroup(AnalysisGroup analysisGroup) throws ReportWriterException;


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
