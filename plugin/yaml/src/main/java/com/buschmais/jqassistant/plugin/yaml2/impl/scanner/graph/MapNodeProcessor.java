package com.buschmais.jqassistant.plugin.yaml2.impl.scanner.graph;

import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.yaml2.api.model.*;
import com.buschmais.jqassistant.plugin.yaml2.impl.scanner.parsing.AliasNode;
import com.buschmais.jqassistant.plugin.yaml2.impl.scanner.parsing.BaseNode;
import com.buschmais.jqassistant.plugin.yaml2.impl.scanner.parsing.MapNode;

import org.snakeyaml.engine.v2.events.Event;

public class MapNodeProcessor implements NodeProcessor<MapNode, YMLMapDescriptor> {

    private final Store store;
    private final GraphGenerator generator;
    private final AnchorProcessor anchorProcessor;
    private final ReferenceNodeGetter refNodeGetter = new ReferenceNodeGetter();

    public MapNodeProcessor(Store store, GraphGenerator generator, AnchorProcessor anchorProcessor) {
        this.store = store;
        this.generator = generator;
        this.anchorProcessor = anchorProcessor;
    }

    @Override
    public void process(MapNode node, Callback<YMLMapDescriptor> callback, GraphGenerator.Mode mode) {
        YMLMapDescriptor mapDescriptor = store.create(YMLMapDescriptor.class);
        node.getIndex().ifPresent(mapDescriptor::setIndex);

        anchorProcessor.process(node, mapDescriptor, mode);

        node.getSimpleKeys().forEach(keyNode -> {
            YMLSimpleKeyDescriptor keyDescriptor = store.create(YMLSimpleKeyDescriptor.class);
            keyDescriptor.setName(keyNode.getKeyName());
            mapDescriptor.getKeys().add(keyDescriptor);

            Callback<YMLDescriptor> addValueDescriptorHandler = descriptor -> {
                store.addDescriptorType(descriptor, YMLValueDescriptor.class);
                keyDescriptor.setValue(descriptor);
            };

            if (keyNode.getValue().getClass().isAssignableFrom(AliasNode.class)) {
                AliasNode aliasNode = (AliasNode) keyNode.getValue();
                BaseNode<?> referencedNode = refNodeGetter.apply(aliasNode);
                generator.traverse(referencedNode, addValueDescriptorHandler, GraphGenerator.Mode.REFERENCE);
            } else {
                BaseNode<?> valueNode = keyNode.getValue();
                generator.traverse(valueNode, addValueDescriptorHandler, mode);
            }

            store.addDescriptorType(keyDescriptor.getValue(), YMLValueDescriptor.class);
        });

        node.getComplexKeys().forEach(keyNode -> {
            YMLComplexKeyDescriptor keyDescriptor = store.create(YMLComplexKeyDescriptor.class);

            mapDescriptor.getComplexKeys().add(keyDescriptor);
            // todo Check if a alias can here occour
            generator.traverse(keyNode.getKeyNode(), keyDescriptor::setKey, mode);
            generator.traverse(keyNode.getValue(), keyDescriptor::setValue, mode);

            store.addDescriptorType(keyDescriptor.getKey(), YMLComplexKeyValue.class);
            store.addDescriptorType(keyDescriptor.getValue(), YMLValueDescriptor.class);
        });

        callback.created(mapDescriptor);
    }

    @Override
    public boolean accepts(BaseNode<? extends Event> node) {
        return node.getClass().isAssignableFrom(MapNode.class);
    }
}
