package com.buschmais.jqassistant.core.analysis.api;

import com.buschmais.jqassistant.core.analysis.api.model.ConstraintGroup;
import com.buschmais.jqassistant.store.api.Store;

import java.util.List;

public interface ConstraintAnalyzer {

    void validateConstraints(Store store, List<ConstraintGroup> constraintGroups);

}
