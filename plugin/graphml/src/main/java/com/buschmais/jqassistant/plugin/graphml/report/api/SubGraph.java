package com.buschmais.jqassistant.plugin.graphml.report.api;

import java.util.Collection;

import com.buschmais.xo.api.CompositeObject;

public interface SubGraph {

    Long getId();

    CompositeObject getParentNode();

    Collection<CompositeObject> getNodes();

    Collection<CompositeObject> getRelationships();

    Collection<SubGraph> getSubGraphs();

}
