package com.buschmais.jqassistant.plugin.yaml2.impl.scanner.graph;

import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.yaml2.api.model.YMLScalarDescriptor;
import com.buschmais.jqassistant.plugin.yaml2.impl.scanner.parsing.AbstractBaseNode;
import com.buschmais.jqassistant.plugin.yaml2.impl.scanner.parsing.SimpleKeyNode;

public class SimpleKeyNodeProcessor implements NodeProcessor<SimpleKeyNode, YMLScalarDescriptor> {
    private final Store store;
    private final AnchorProcessor anchorProcessor;

    public SimpleKeyNodeProcessor(Store store, AnchorProcessor anchorProcessor) {
        this.store = store;
        this.anchorProcessor = anchorProcessor;
    }

    @Override
    public void process(SimpleKeyNode node, Callback<YMLScalarDescriptor> callback, GraphGenerator.Mode mode) {
        YMLScalarDescriptor scalarDescriptor = store.create(YMLScalarDescriptor.class);
        scalarDescriptor.setValue(node.getKeyName());

        anchorProcessor.process(node, scalarDescriptor, mode);

        callback.created(scalarDescriptor);
    }

    @Override
    public boolean accepts(AbstractBaseNode node) {
        return node.getClass().isAssignableFrom(SimpleKeyNode.class);
    }
}
