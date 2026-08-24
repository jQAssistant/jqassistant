package com.buschmais.jqassistant.core.report.api.model;

import java.util.List;

import com.buschmais.jqassistant.core.store.api.model.jQAssistantDescriptor;
import com.buschmais.xo.api.annotation.Abstract;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

@Abstract
@Label("jQASuppress")
public interface SuppressDescriptor extends jQAssistantDescriptor {

    @Relation("HAS_SUPPRESSION")
    List<SuppressionDescriptor> getSuppressions();

}
