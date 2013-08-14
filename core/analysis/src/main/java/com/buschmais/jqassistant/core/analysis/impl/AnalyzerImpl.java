package com.buschmais.jqassistant.core.analysis.impl;

import com.buschmais.jqassistant.core.analysis.api.Analyzer;
import com.buschmais.jqassistant.core.analysis.api.AnalyzerException;
import com.buschmais.jqassistant.core.model.api.Query;
import com.buschmais.jqassistant.core.model.api.Result;
import com.buschmais.jqassistant.core.model.api.rules.*;
import com.buschmais.jqassistant.report.api.ReportWriter;
import com.buschmais.jqassistant.report.api.ReportWriterException;
import com.buschmais.jqassistant.store.api.QueryResult;
import com.buschmais.jqassistant.store.api.Store;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Implementation of the {@link com.buschmais.jqassistant.core.analysis.api.Analyzer ).
 */
public class AnalyzerImpl implements Analyzer {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnalyzerImpl.class);

    private Store store;

    private ReportWriter reportWriter;

    private Set<Concept> executedConcepts = new HashSet<>();

    private Set<Constraint> executedConstraints = new HashSet<>();

    private Set<Group> executedGroups = new HashSet<>();

    /**
     * Constructor.
     *
     * @param store The Store to use.
     */
    public AnalyzerImpl(Store store, ReportWriter reportWriter) {
        this.store = store;
        this.reportWriter = reportWriter;
    }

    @Override
    public void execute(RuleSet ruleSet) throws AnalyzerException {
        try {
            reportWriter.begin();
            try {
                executeGroups(ruleSet.getGroups().values());
                validateConstraints(ruleSet.getConstraints().values());
                applyConcepts(ruleSet.getConcepts().values());
            } finally {
                reportWriter.end();
            }
        } catch (ReportWriterException e) {
            throw new AnalyzerException("Cannot write report.", e);
        }
    }

    /**
     * Executes the given groups.
     *
     * @param groups The groups.
     * @throws ReportWriterException If the report cannot be written.
     */
    private void executeGroups(Iterable<Group> groups) throws ReportWriterException {
        for (Group group : groups) {
            executeGroup(group);
        }
    }

    /**
     * Executes the given group.
     *
     * @param group The group.
     * @throws ReportWriterException If the report cannot be written.
     */
    private void executeGroup(Group group) throws ReportWriterException {
        if (!executedGroups.contains(group)) {
            LOGGER.info("Executing group '{}'", group.getId());
            for (Group includedGroup : group.getGroups()) {
                executeGroup(includedGroup);
            }
            reportWriter.beginGroup(group);
            try {
                applyConcepts(group.getConcepts());
                validateConstraints(group.getConstraints());
                executedGroups.add(group);
            } finally {
                reportWriter.endGroup();
            }
        }
    }

    /**
     * Validates the given constraints.
     *
     * @param constraints The constraints.
     * @throws ReportWriterException If the report cannot be written.
     */
    private void validateConstraints(Iterable<Constraint> constraints) throws ReportWriterException {
        for (Constraint constraint : constraints) {
            validateConstraint(constraint);
        }
    }

    /**
     * Validates the given constraint.
     *
     * @param constraint The constraint.
     * @throws ReportWriterException If the report cannot be written.
     */
    private void validateConstraint(Constraint constraint) throws ReportWriterException {
        if (!executedConstraints.contains(constraint)) {
            for (Concept requiredConcept : constraint.getRequiredConcepts()) {
                applyConcept(requiredConcept);
            }
            LOGGER.info("Validating constraint '{}'.", constraint.getId());
            reportWriter.beginConstraint(constraint);
            try {
                reportWriter.setResult(execute(constraint));
                executedConstraints.add(constraint);
            } finally {
                reportWriter.endConstraint();
            }
        }
    }

    /**
     * Applies the given concepts.
     *
     * @param concepts The concepts.
     * @throws ReportWriterException If the report cannot be written.
     */
    private void applyConcepts(Iterable<Concept> concepts) throws ReportWriterException {
        for (Concept concept : concepts) {
            applyConcept(concept);
        }
    }

    /**
     * Applies the given concept.
     *
     * @param concept The concept.
     * @throws ReportWriterException If the report cannot be written.
     */
    private void applyConcept(Concept concept) throws ReportWriterException {
        if (!executedConcepts.contains(concept)) {
            for (Concept requiredConcept : concept.getRequiredConcepts()) {
                applyConcept(requiredConcept);
            }
            LOGGER.info("Applying concept '{}'.", concept.getId());
            reportWriter.beginConcept(concept);
            try {
                reportWriter.setResult(execute(concept));
                executedConcepts.add(concept);
            } finally {
                reportWriter.endConcept();
            }
        }
    }

    /**
     * Run the given executable and return a result which can be passed to a report writer.
     *
     * @param executable The executable.
     * @param <T>        The type of the executable.
     * @return The result.
     */
    private <T extends AbstractExecutable> Result<T> execute(T executable) {
        List<Map<String, Object>> rows = new ArrayList<>();
        QueryResult queryResult = null;
        try {
            store.beginTransaction();
            queryResult = executeQuery(executable.getQuery());
            for (QueryResult.Row row : queryResult.getRows()) {
                rows.add(row.get());
            }
            store.commitTransaction();
        } catch (RuntimeException e) {
            store.rollbackTransaction();
            LOGGER.error("Caught ex.", e);
        } finally {
            IOUtils.closeQuietly(queryResult);
        }
        return new Result<T>(executable, queryResult.getColumns(), rows);
    }

    /**
     * Execute the given query.
     *
     * @param query The query.
     * @return The query result.
     */
    private QueryResult executeQuery(Query query) {
        String cypher = query.getCypher();
        Map<String, Object> parameters = query.getParameters();
        LOGGER.debug("Executing query '{}' with parameters [{}]", cypher, parameters);
        return store.executeQuery(cypher, parameters);
    }
}
