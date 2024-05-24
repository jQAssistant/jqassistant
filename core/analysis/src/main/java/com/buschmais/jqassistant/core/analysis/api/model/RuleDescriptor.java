package com.buschmais.jqassistant.core.analysis.api.model;

import com.buschmais.xo.api.annotation.Abstract;
import com.buschmais.xo.neo4j.api.annotation.Label;

@Label("Rule")
@Abstract
public interface RuleDescriptor extends jQAssistantDescriptor{
}
