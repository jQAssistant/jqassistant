package com.buschmais.jqassistant.plugin.yaml2.impl.scanner.graph;

import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.yaml2.api.model.YMLScalarDescriptor;
import com.buschmais.jqassistant.plugin.yaml2.impl.scanner.parsing.AbstractBaseNode;
import com.buschmais.jqassistant.plugin.yaml2.impl.scanner.parsing.SimpleKeyNode;

class SimpleKeyNodeProcessor implements NodeProcessor<SimpleKeyNode, YMLScalarDescriptor> {
    private final Store store;
    private final AnchorHandler anchorHandler;

    public SimpleKeyNodeProcessor(Store store, AnchorHandler anchorHandler) {
        this.store = store;
        this.anchorHandler = anchorHandler;
    }

    @Override
    public void process(SimpleKeyNode node, Callback<YMLScalarDescriptor> callback, GraphGenerator.Mode mode) {
        YMLScalarDescriptor scalarDescriptor = store.create(YMLScalarDescriptor.class);
        scalarDescriptor.setValue(node.getKeyName());

        anchorHandler.handleAnchor(node, scalarDescriptor, mode);

        callback.created(scalarDescriptor);
    }

    @Override
    public boolean accepts(AbstractBaseNode node) {
        return node.getClass().isAssignableFrom(SimpleKeyNode.class);
    }
}
