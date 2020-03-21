package com.buschmais.jqassistant.plugin.yaml2.impl.scanner.graph;

import com.buschmais.jqassistant.plugin.yaml2.api.model.YMLDescriptor;
import com.buschmais.jqassistant.plugin.yaml2.impl.scanner.parsing.BaseNode;

import org.snakeyaml.engine.v2.events.Event;

public interface NodeProcessor<N extends BaseNode<? extends Event>, D extends YMLDescriptor> extends NodeProcessorPredicate {
    void process(N node, Callback<D> callback, GraphGenerator.Mode mode);
}
