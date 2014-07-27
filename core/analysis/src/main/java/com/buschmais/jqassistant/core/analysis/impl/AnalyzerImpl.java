package com.buschmais.jqassistant.core.analysis.impl;

import static com.buschmais.xo.api.Query.Result.CompositeRowObject;

import java.util.*;

import com.buschmais.jqassistant.core.analysis.api.*;
import com.buschmais.jqassistant.core.analysis.api.rule.*;
import com.buschmais.jqassistant.core.analysis.impl.store.descriptor.ConceptDescriptor;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.xo.api.XOException;

/**
 * Implementation of the {@link Analyzer}.
 */
public class AnalyzerImpl implements Analyzer {

    private Store store;

    private AnalysisListener reportWriter;

    private Console console;

    private Set<Constraint> executedConstraints = new HashSet<>();

    private Set<Group> executedGroups = new HashSet<>();

    /**
     * Constructor.
     * 
     * @param store
     *            The Store to use.
     */
    public AnalyzerImpl(Store store, AnalysisListener reportWriter, Console console) {
        this.store = store;
        this.reportWriter = reportWriter;
        this.console = console;
    }

    @Override
    public void execute(RuleSet ruleSet) throws AnalysisException {
        try {
            reportWriter.begin();
            try {
                executeGroups(ruleSet.getGroups().values());
                validateConstraints(ruleSet.getConstraints().values());
                applyConcepts(ruleSet.getConcepts().values());
            } finally {
                reportWriter.end();
            }
        } catch (AnalysisListenerException e) {
            throw new AnalysisException("Cannot write report.", e);
        }
    }

    /**
     * Executes the given groups.
     * 
     * @param groups
     *            The groups.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisListenerException
     *             If the report cannot be written.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
     *             If the groups cannot be executed.
     */
    private void executeGroups(Iterable<Group> groups) throws AnalysisListenerException, AnalysisException {
        for (Group group : groups) {
            executeGroup(group);
        }
    }

    /**
     * Executes the given group.
     * 
     * @param group
     *            The group.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisListenerException
     *             If the report cannot be written.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
     *             If the group cannot be executed.
     */
    private void executeGroup(Group group) throws AnalysisListenerException, AnalysisException {
        if (!executedGroups.contains(group)) {
            console.info("Executing group '" + group.getId() + "'");
            for (Group includedGroup : group.getGroups()) {
                executeGroup(includedGroup);
            }
            store.beginTransaction();
            reportWriter.beginGroup(group);
            store.commitTransaction();
            applyConcepts(group.getConcepts());
            validateConstraints(group.getConstraints());
            executedGroups.add(group);
            store.beginTransaction();
            reportWriter.endGroup();
            store.commitTransaction();
        }
    }

    /**
     * Validates the given constraints.
     * 
     * @param constraints
     *            The constraints.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisListenerException
     *             If the report cannot be written.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
     *             If the constraints cannot be validated.
     */
    private void validateConstraints(Iterable<Constraint> constraints) throws AnalysisListenerException, AnalysisException {
        for (Constraint constraint : constraints) {
            validateConstraint(constraint);
        }
    }

    /**
     * Validates the given constraint.
     * 
     * @param constraint
     *            The constraint.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisListenerException
     *             If the report cannot be written.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
     *             If the constraint cannot be validated.
     */
    private void validateConstraint(Constraint constraint) throws AnalysisListenerException, AnalysisException {
        if (!executedConstraints.contains(constraint)) {
            for (Concept requiredConcept : constraint.getRequiresConcepts()) {
                applyConcept(requiredConcept);
            }
            console.info("Validating constraint '" + constraint.getId() + "' with severity: '"+ constraint.getSeverity() + "'.");
            try {
                store.beginTransaction();
                reportWriter.beginConstraint(constraint);
                reportWriter.setResult(execute(constraint));
                executedConstraints.add(constraint);
                reportWriter.endConstraint();
                store.commitTransaction();
            } catch (XOException e) {
                store.rollbackTransaction();
                throw new AnalysisException("Cannot validate constraint " + constraint.getId(), e);
            }
        }
    }

    /**
     * Applies the given concepts.
     * 
     * @param concepts
     *            The concepts.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisListenerException
     *             If the report cannot be written.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
     *             If the concepts cannot be applied.
     */
    private void applyConcepts(Iterable<Concept> concepts) throws AnalysisListenerException, AnalysisException {
        for (Concept concept : concepts) {
            applyConcept(concept);
        }
    }

    /**
     * Applies the given concept.
     * 
     * @param concept
     *            The concept.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisListenerException
     *             If the report cannot be written.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
     *             If the concept cannot be applied.
     */
    private void applyConcept(Concept concept) throws AnalysisListenerException, AnalysisException {
        for (Concept requiredConcept : concept.getRequiresConcepts()) {
            applyConcept(requiredConcept);
        }
        try {
            store.beginTransaction();
            ConceptDescriptor conceptDescriptor = store.find(ConceptDescriptor.class, concept.getId());
            if (conceptDescriptor == null) {
                console.info("Applying concept '" + concept.getId() + "'.");
                reportWriter.beginConcept(concept);
                reportWriter.setResult(execute(concept));
                conceptDescriptor = store.create(ConceptDescriptor.class);
                conceptDescriptor.setId(concept.getId());
                reportWriter.endConcept();
            }
            store.commitTransaction();
        } catch (XOException e) {
            store.rollbackTransaction();
            throw new AnalysisException("Cannot apply concept " + concept.getId(), e);
        }
    }

    /**
     * Run the given executable and return a result which can be passed to a
     * report writer.
     * 
     * @param executable
     *            The executable.
     * @param <T>
     *            The types of the executable.
     * @return The result.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
     *             If query execution fails.
     */
    private <T extends AbstractRule> Result<T> execute(T executable) throws AnalysisException {
        List<Map<String, Object>> rows = new ArrayList<>();
        try (com.buschmais.xo.api.Query.Result<CompositeRowObject> compositeRowObjects = executeQuery(executable.getQuery())) {
            List<String> columns = null;
            for (CompositeRowObject rowObject : compositeRowObjects) {
                if (columns == null) {
                    columns = new ArrayList<>(rowObject.getColumns());
                }
                Map<String, Object> row = new HashMap<>();
                for (String columnName : columns) {
                    row.put(columnName, rowObject.get(columnName, Object.class));
                }
                rows.add(row);
            }
            return new Result<T>(executable, columns, rows);
        } catch (Exception e) {
            throw new AnalysisException("Cannot execute query.", e);
        }
    }

    /**
     * Execute the given query.
     * 
     * @param query
     *            The query.
     * @return The query result.
     */
    private com.buschmais.xo.api.Query.Result<CompositeRowObject> executeQuery(Query query) {
        String cypher = query.getCypher();
        Map<String, Object> parameters = query.getParameters();
        console.debug("Executing query '" + cypher + "' with parameters [" + parameters + "]");
        return store.executeQuery(cypher, parameters);
    }
}
