package com.buschmais.jqassistant.plugin.yaml2.impl.scanner.graph;

import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.yaml2.api.model.YMLScalarDescriptor;
import com.buschmais.jqassistant.plugin.yaml2.impl.scanner.parsing.BaseNode;
import com.buschmais.jqassistant.plugin.yaml2.impl.scanner.parsing.ScalarNode;

import org.snakeyaml.engine.v2.events.Event;

public class ScalarNodeProcessor implements NodeProcessor<ScalarNode, YMLScalarDescriptor> {

    private final Store store;
    private final AnchorProcessor anchorProcessor;

    public ScalarNodeProcessor(Store store, AnchorProcessor anchorProcessor) {
        this.store = store;
        this.anchorProcessor = anchorProcessor;
    }

    @Override
    public void process(ScalarNode node, Callback<YMLScalarDescriptor> callback, GraphGenerator.Mode mode) {
        YMLScalarDescriptor scalarDescriptor = store.create(YMLScalarDescriptor.class);
        scalarDescriptor.setValue(node.getScalarValue());
        node.getIndex().ifPresent(scalarDescriptor::setIndex);

        anchorProcessor.process(node, scalarDescriptor, mode);

        callback.created(scalarDescriptor);
    }

    @Override
    public boolean accepts(BaseNode<? extends Event> node) {
        return node.getClass().isAssignableFrom(ScalarNode.class);
    }
}
