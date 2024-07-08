package com.buschmais.jqassistant.core.analysis.api.model;

import com.buschmais.jqassistant.core.rule.api.model.Severity;
import com.buschmais.jqassistant.core.store.api.model.jQAssistantDescriptor;
import com.buschmais.xo.api.annotation.Abstract;
import com.buschmais.xo.neo4j.api.annotation.Label;

@Label("Rule")
@Abstract
public interface RuleDescriptor extends jQAssistantDescriptor {

    String getId();

    void setId(String id);

    Severity getSeverity();

    void setSeverity(Severity severity);

    Severity getEffectiveSeverity();

    void setEffectiveSeverity(Severity severity);
}
