package com.buschmais.jqassistant.core.analysis.api;

import com.buschmais.jqassistant.core.analysis.api.model.ConstraintGroup;
import com.buschmais.jqassistant.core.analysis.api.model.ConstraintViolations;
import com.buschmais.jqassistant.store.api.Store;

import java.util.List;

public interface ConstraintAnalyzer {

    List<ConstraintViolations> validateConstraints(Iterable<ConstraintGroup> constraintGroups);

}
