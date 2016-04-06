package com.buschmais.jqassistant.core.store.api.model;

import java.util.Collection;

import com.buschmais.xo.api.CompositeObject;

/**
 * Defines a graph with nodes, relationships and sub graphs.
 */
public interface SubGraph {
    /**
     * The id of the graph.
     *
     * @return The id.
     */
    Long getId();

    /**
     * Return the nodes of the graph.
     *
     * @return The nodes of the graph.
     */
    Collection<CompositeObject> getNodes();

    /**
     * Return the relationships of the graph.
     *
     * @return The relationships of the graph.
     */
    Collection<CompositeObject> getRelationships();

    /**
     * Return the sub graphs of the graph.
     *
     * @return Return the sub graphs of the graph.
     */
    Collection<SubGraph> getSubGraphs();

    /**
     * The parent of the sub graph.
     *
     * @return The parent of the sub graph, may be <code>null</code>.
     */
    CompositeObject getParentNode();

}
