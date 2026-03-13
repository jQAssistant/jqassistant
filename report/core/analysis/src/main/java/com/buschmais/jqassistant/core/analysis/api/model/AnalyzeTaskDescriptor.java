package com.buschmais.jqassistant.core.analysis.api.model;

import com.buschmais.jqassistant.core.store.api.model.TaskDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;

@Label("Analyze")
public interface AnalyzeTaskDescriptor extends TaskDescriptor, RuleGroupTemplate {
}
