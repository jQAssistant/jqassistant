package com.buschmais.jqassistant.core.analysis.api;

import java.util.List;

import com.buschmais.jqassistant.core.analysis.schema.v1.Rules;
import com.buschmais.jqassistant.store.api.Store;

public interface ConstraintAnalysis {

	void validateConstraints(Store store, List<Rules> rules);

}
