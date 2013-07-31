package com.buschmais.jqassistant.core.analysis.impl;

import com.buschmais.jqassistant.core.analysis.api.ConstraintAnalyzer;
import com.buschmais.jqassistant.core.model.api.Query;
import com.buschmais.jqassistant.core.model.api.Result;
import com.buschmais.jqassistant.core.model.api.rules.AbstractExecutable;
import com.buschmais.jqassistant.core.model.api.rules.Concept;
import com.buschmais.jqassistant.core.model.api.rules.Constraint;
import com.buschmais.jqassistant.core.model.api.rules.ConstraintGroup;
import com.buschmais.jqassistant.report.api.ReportWriter;
import com.buschmais.jqassistant.report.api.ReportWriterException;
import com.buschmais.jqassistant.store.api.QueryResult;
import com.buschmais.jqassistant.store.api.Store;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Implementation of the {@link ConstraintAnalyzer).
 */
public class ConstraintAnalyzerImpl implements ConstraintAnalyzer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConstraintAnalyzerImpl.class);

    private Store store;

    private ReportWriter reportWriter;

    private Set<Concept> executedConcepts = new HashSet<Concept>();

    private Set<Constraint> executedConstraints = new HashSet<Constraint>();

    private Set<ConstraintGroup> executedConstraintGroups = new HashSet<ConstraintGroup>();


    /**
     * Constructor.
     *
     * @param store The Store to use.
     */
    public ConstraintAnalyzerImpl(Store store, ReportWriter reportWriter) {
        this.store = store;
        this.reportWriter = reportWriter;
    }

    @Override
    public void validateConstraints(Iterable<ConstraintGroup> constraintGroups) throws ReportWriterException {
        reportWriter.begin();
        try {
            for (ConstraintGroup constraintGroup : constraintGroups) {
                validateConstraintGroup(constraintGroup);
            }
        } finally {
            reportWriter.end();
        }
    }

    private void validateConstraintGroup(ConstraintGroup constraintGroup) throws ReportWriterException {
        if (!executedConstraintGroups.contains(constraintGroup)) {
            LOGGER.info("Executing constraint group '{}'", constraintGroup.getId());
            for (ConstraintGroup includedConstraintGroup : constraintGroup.getConstraintGroups()) {
                validateConstraintGroup(includedConstraintGroup);
            }
            reportWriter.beginConstraintGroup(constraintGroup);
            try {
                for (Constraint constraint : constraintGroup.getConstraints()) {
                    validateConstraint(constraint);
                }
                executedConstraintGroups.add(constraintGroup);
            } finally {
                reportWriter.endConstraintGroup();
            }
        }
    }

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

    private void applyConcept(Concept concept) throws ReportWriterException {
        if (!executedConcepts.contains(concept)) {
            for (Concept requiredConcept : concept.getRequiredConcepts()) {
                applyConcept(requiredConcept);
            }
            LOGGER.info("Applying concept '{}'.", concept.getId());
            reportWriter.beginConcept(concept);
            try {
                store.beginTransaction();
                try {
                    reportWriter.setResult(execute(concept));
                } finally {
                    store.endTransaction();
                }
                executedConcepts.add(concept);
            } finally {
                reportWriter.endConcept();
            }
        }
    }

    private <T extends AbstractExecutable> Result<T> execute(T executable) {
        List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
        QueryResult queryResult = null;
        try {
            queryResult = executeQuery(executable.getQuery());
            for (QueryResult.Row row : queryResult.getRows()) {
                rows.add(row.get());
            }
        } finally {
            IOUtils.closeQuietly(queryResult);
        }
        return new Result<T>(executable, queryResult.getColumns(), rows);
    }

    private QueryResult executeQuery(Query query) {
        String cypher = query.getCypher();
        Map<String, Object> parameters = query.getParameters();
        LOGGER.debug("Executing query '{}' with parameters [{}]", cypher, parameters);
        return store.executeQuery(cypher, parameters);
    }
}
