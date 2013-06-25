package com.buschmais.jqassistant.core.analysis.impl;

import com.buschmais.jqassistant.core.analysis.api.ConstraintAnalyzer;
import com.buschmais.jqassistant.core.analysis.api.model.*;
import com.buschmais.jqassistant.store.api.Store;
import com.buschmais.jqassistant.store.api.model.QueryResult;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ConstraintAnalyzerImpl implements ConstraintAnalyzer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConstraintAnalyzerImpl.class);

    private Store store;

    private Set<Concept> executedConcepts = new HashSet<Concept>();

    private Set<Constraint> executedConstraints = new HashSet<Constraint>();

    private Set<ConstraintGroup> executedConstraintGroups = new HashSet<ConstraintGroup>();

    private List<ConstraintViolations> constraintViolations = new ArrayList<ConstraintViolations>();

    public ConstraintAnalyzerImpl(Store store) {
        this.store = store;
    }

    @Override
    public List<ConstraintViolations> validateConstraints(Iterable<ConstraintGroup> constraintGroups) {
        for (ConstraintGroup constraintGroup : constraintGroups) {
            validateConstraintGroup(constraintGroup);
        }
        return constraintViolations;
    }

    private void validateConstraintGroup(ConstraintGroup constraintGroup) {
        if (!executedConstraintGroups.contains(constraintGroup)) {
            LOGGER.info("Executing constraint group '{}'", constraintGroup.getId());
            for (ConstraintGroup includedConstraintGroup : constraintGroup.getConstraintGroups()) {
                validateConstraintGroup(includedConstraintGroup);
            }
            for (Constraint constraint : constraintGroup.getConstraints()) {
                validateConstraint(constraint);
            }
            executedConstraintGroups.add(constraintGroup);
        }
    }

    private void validateConstraint(Constraint constraint) {
        if (!executedConstraints.contains(constraint)) {
            for (Concept requiredConcept : constraint.getRequiredConcepts()) {
                applyConcept(requiredConcept);
            }
            LOGGER.info("Validating constraint '{}'", constraint.getId());
            QueryResult queryResult = null;
            List<Map<String, Object>> violations = new ArrayList<Map<String, Object>>();
            try {
                queryResult = executeQuery(constraint.getQuery());
                for (Map<String, Object> row : queryResult.getRows()) {
                    violations.add(row);
                }
            } finally {
                IOUtils.closeQuietly(queryResult);
            }
            if (!violations.isEmpty()) {
                LOGGER.warn("Found {} violations for constraint '{}'.", violations.size(), constraint.getId());
                this.constraintViolations.add(new ConstraintViolations(constraint, violations));
            }
            executedConstraints.add(constraint);
        }
    }

    private void applyConcept(Concept concept) {
        if (!executedConcepts.contains(concept)) {
            for (Concept requiredConcept : concept.getRequiredConcepts()) {
                applyConcept(requiredConcept);
            }
            LOGGER.info("Applying concept '{}'.", concept.getId());
            QueryResult queryResult = null;
            store.beginTransaction();
            try {
                queryResult = executeQuery(concept.getQuery());
            } finally {
                IOUtils.closeQuietly(queryResult);
                store.endTransaction();
            }
            executedConcepts.add(concept);
        }
    }

    private QueryResult executeQuery(Query query) {
        String cypher = query.getCypher();
        Map<String, Object> parameters = query.getParameters();
        LOGGER.debug("Executing query '{}' with parameters [{}]", cypher, parameters);
        return store.executeQuery(cypher, parameters);
    }
}
