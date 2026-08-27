package com.buschmais.jqassistant.core.report.api.model;

import java.util.List;

import com.buschmais.jqassistant.core.store.api.model.jQAssistantDescriptor;
import com.buschmais.xo.api.annotation.Abstract;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

/**
 * Label that can be added to nodes in rule results to suppress a row (e.g. constraint) if any of the suppressions returned by #getSuppressions() is applicable.
 * <p>
 * The label usually is added by scanners, e.g. Java classes, methods, etc., but can be provided by concepts as well.
 */
@Abstract
@Label("jQASuppress")
public interface SuppressDescriptor extends jQAssistantDescriptor {

    @Relation("HAS_SUPPRESSION")
    List<SuppressionDescriptor> getSuppressions();

}
