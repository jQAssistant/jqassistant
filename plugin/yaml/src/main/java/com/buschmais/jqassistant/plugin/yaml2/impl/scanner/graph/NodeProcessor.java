package com.buschmais.jqassistant.plugin.yaml2.impl.scanner.graph;

import com.buschmais.jqassistant.plugin.yaml2.api.model.YMLDescriptor;
import com.buschmais.jqassistant.plugin.yaml2.impl.scanner.parsing.AbstractBaseNode;

interface NodeProcessor<N extends AbstractBaseNode, D extends YMLDescriptor> extends NodeProcessorPredicate {
    void process(N node, Callback<D> callback, GraphGenerator.Mode mode);
}
