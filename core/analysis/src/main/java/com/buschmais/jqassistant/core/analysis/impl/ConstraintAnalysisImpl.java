package com.buschmais.jqassistant.core.analysis.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.core.analysis.api.ConstraintAnalysis;
import com.buschmais.jqassistant.core.analysis.schema.v1.ConceptType;
import com.buschmais.jqassistant.core.analysis.schema.v1.ConstraintGroupType;
import com.buschmais.jqassistant.core.analysis.schema.v1.ConstraintType;
import com.buschmais.jqassistant.core.analysis.schema.v1.QueryDefinitionType;
import com.buschmais.jqassistant.core.analysis.schema.v1.ReferenceableType;
import com.buschmais.jqassistant.core.analysis.schema.v1.Rules;
import com.buschmais.jqassistant.store.api.Store;

public class ConstraintAnalysisImpl implements ConstraintAnalysis {

	private final Map<String, QueryDefinitionType> queryDefinitions = new HashMap<String, QueryDefinitionType>();
	private final Map<String, ConceptType> concepts = new HashMap<String, ConceptType>();
	private final Map<String, ConstraintType> constraints = new HashMap<String, ConstraintType>();
	private final Map<String, ConstraintGroupType> constraintGroups = new HashMap<String, ConstraintGroupType>();

	@Override
	public void validateConstraints(Store store, List<Rules> rules) {
		for (Rules rule : rules) {
			cacheReferencables(rule.getQueryDefinition(), queryDefinitions);
			cacheReferencables(rule.getConcept(), concepts);
			cacheReferencables(rule.getConstraint(), constraints);
			cacheReferencables(rule.getConstraintGroup(), constraintGroups);
		}
		for (ConceptType concept : concepts.values()) {
		}
	}

	private <T extends ReferenceableType> void cacheReferencables(List<T> list,
			Map<String, T> map) {
		for (T t : list) {
			map.put(t.getId(), t);
		}
	}
}
