package com.buschmais.jqassistant.core.analysis.impl;

import com.buschmais.jqassistant.core.analysis.api.*;
import com.buschmais.jqassistant.core.analysis.api.model.Concept;
import com.buschmais.jqassistant.core.analysis.api.model.Constraint;
import com.buschmais.jqassistant.core.analysis.api.model.ConstraintGroup;
import com.buschmais.jqassistant.core.analysis.schema.v1.*;
import com.buschmais.jqassistant.store.api.Store;

import java.util.*;

public class ConstraintAnalysisImpl implements ConstraintAnalyzer {

    private final Map<String, Concept> concepts = new HashMap<String, Concept>();
    private final Map<String, Constraint> constraints = new HashMap<String, Constraint>();
    private final Map<String, ConstraintGroup> constraintGroups = new HashMap<String, ConstraintGroup>();

    @Override
    public void validateConstraints(Store store, List<ConstraintGroup> constraintGroups) {

    }
}
