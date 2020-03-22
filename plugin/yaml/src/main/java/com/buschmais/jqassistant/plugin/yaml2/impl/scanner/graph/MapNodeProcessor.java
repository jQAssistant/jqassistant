package com.buschmais.jqassistant.plugin.yaml2.impl.scanner.graph;

import java.util.Collection;
import java.util.TreeSet;

import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.yaml2.api.model.*;
import com.buschmais.jqassistant.plugin.yaml2.impl.scanner.parsing.*;

import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.toCollection;

public class MapNodeProcessor implements NodeProcessor<MapNode, YMLMapDescriptor> {

    private final Store store;
    private final GraphGenerator generator;
    private final AnchorProcessor anchorProcessor;
    private final ReferenceNodeGetter refNodeGetter = new ReferenceNodeGetter();
    private final AliasProcessor aliasProcessor;

    public MapNodeProcessor(Store store, GraphGenerator generator, AnchorProcessor anchorProcessor,
                            AliasProcessor aliasProcessor) {
        this.store = store;
        this.generator = generator;
        this.anchorProcessor = anchorProcessor;
        this.aliasProcessor = aliasProcessor;
    }

    @Override
    public void process(MapNode node, Callback<YMLMapDescriptor> callback, GraphGenerator.Mode mode) {
        YMLMapDescriptor mapDescriptor = store.create(YMLMapDescriptor.class);
        node.getIndex().ifPresent(mapDescriptor::setIndex);

        anchorProcessor.process(node, mapDescriptor, mode);

        Collection<AbstractTask> tasks = new TreeSet<>(comparingInt(AbstractTask::getTokenIndex));
        node.getSimpleKeys().stream().map(keyNode -> new SimpleKeyTask(keyNode, mapDescriptor, mode)).collect(toCollection(() -> tasks));
        node.getComplexKeys().stream().map(keyNode -> new ComplexKeyTask(keyNode, mapDescriptor, mode)).collect(toCollection(() -> tasks));
        node.getAliasKeys().stream().map(keyNode -> new AliasKeyTask(keyNode, mapDescriptor, mode)).collect(toCollection(() -> tasks));

        tasks.forEach(AbstractTask::run);

        callback.created(mapDescriptor);
    }

    @Override
    public boolean accepts(AbstractBaseNode node) {
        return node.getClass().isAssignableFrom(MapNode.class);
    }

    private static abstract class AbstractTask {

        abstract int getTokenIndex();

        abstract void run();
    }

    private class AliasKeyTask extends AbstractTask {
        private final YMLMapDescriptor mapDescriptor;
        private final AliasKeyNode aliasKeyNode;
        private final GraphGenerator.Mode mode;

        public AliasKeyTask(AliasKeyNode node, YMLMapDescriptor mapDescriptor, GraphGenerator.Mode mode) {
            this.aliasKeyNode = node;
            this.mapDescriptor = mapDescriptor;
            this.mode = mode;
        }

        @Override
        int getTokenIndex() {
            return aliasKeyNode.getTokenIndex();
        }

        @Override
        void run() {
            if (aliasKeyNode.getKey() instanceof ScalarNode) {
                ScalarNode scalarNode = (ScalarNode) aliasKeyNode.getKey();
                YMLSimpleKeyDescriptor simpleKeyDescriptor = store.create(YMLSimpleKeyDescriptor.class);
                simpleKeyDescriptor.setName(scalarNode.getScalarValue());
                store.addDescriptorType(simpleKeyDescriptor, YMLAliasDescriptor.class);
                mapDescriptor.getKeys().add(simpleKeyDescriptor);
                aliasProcessor.createReferenceEdge(aliasKeyNode, simpleKeyDescriptor);

                BaseNode<?> value = aliasKeyNode.getValue();
                Callback<YMLDescriptor> callback = simpleKeyDescriptor::setValue;

                BaseNode<?> traverseRoot = value instanceof AliasNode
                                           ? refNodeGetter.apply((AliasNode) value)
                                           : aliasKeyNode.getValue();

                generator.traverse(traverseRoot, callback, mode);
            } else {
                String message = "Key of alias key node is not a scalar node";
                throw new IllegalStateException(message);
            }
        }
    }

    private class SimpleKeyTask extends AbstractTask {
        private final SimpleKeyNode keyNode;
        private final YMLMapDescriptor mapDescriptor;
        private final GraphGenerator.Mode mode;

        public SimpleKeyTask(SimpleKeyNode node, YMLMapDescriptor mapDescriptor, GraphGenerator.Mode mode) {
            this.keyNode = node;
            this.mapDescriptor = mapDescriptor;
            this.mode = mode;
        }

        @Override
        int getTokenIndex() {
            return keyNode.getTokenIndex();
        }

        @Override
        void run() {
            YMLSimpleKeyDescriptor keyDescriptor = store.create(YMLSimpleKeyDescriptor.class);
            keyDescriptor.setName(keyNode.getKeyName());
            mapDescriptor.getKeys().add(keyDescriptor);
            anchorProcessor.process(keyNode, keyDescriptor, mode);
            Callback<YMLDescriptor> addValueDescriptorCallback = descriptor -> {
                store.addDescriptorType(descriptor, YMLValueDescriptor.class);
                keyDescriptor.setValue(descriptor);
            };

            if (keyNode.getValue().getClass().isAssignableFrom(AliasNode.class)) {
                AliasNode aliasNode = (AliasNode) keyNode.getValue();
                BaseNode<?> referencedNode = refNodeGetter.apply(aliasNode);
                Callback<YMLDescriptor> createReferenceCallback = descriptor -> {
                    addValueDescriptorCallback.created(descriptor);
                    aliasProcessor.createReferenceEdge(aliasNode, descriptor);
                };

                generator.traverse(referencedNode, createReferenceCallback, GraphGenerator.Mode.REFERENCE);
            } else {
                AbstractBaseNode valueNode = keyNode.getValue();
                generator.traverse(valueNode, addValueDescriptorCallback, mode);
            }

            store.addDescriptorType(keyDescriptor.getValue(), YMLValueDescriptor.class);
        }

    }


    private class ComplexKeyTask extends AbstractTask {
        private final ComplexKeyNode keyNode;
        private final YMLMapDescriptor mapDescriptor;
        private final GraphGenerator.Mode mode;

        public ComplexKeyTask(ComplexKeyNode node, YMLMapDescriptor mapDescriptor, GraphGenerator.Mode mode) {
            this.keyNode = node;
            this.mapDescriptor = mapDescriptor;
            this.mode = mode;
        }

        @Override
        void run() {
            YMLComplexKeyDescriptor keyDescriptor = store.create(YMLComplexKeyDescriptor.class);

            mapDescriptor.getComplexKeys().add(keyDescriptor);
            Callback<YMLDescriptor> addComplexKeyDescriptorCallback = key -> {
                keyDescriptor.setKey(key);
                store.addDescriptorType(keyDescriptor.getKey(), YMLComplexKeyValue.class);
            };
            Callback<YMLDescriptor> addValueKeyDescriptorCallback = descriptor -> {
                keyDescriptor.setValue(descriptor);
                store.addDescriptorType(keyDescriptor.getValue(), YMLValueDescriptor.class);
            };

            generator.traverse(this.keyNode.getKeyNode(), addComplexKeyDescriptorCallback, mode);
            anchorProcessor.process(this.keyNode.getKeyNode(), keyDescriptor.getKey(), mode);
            generator.traverse(this.keyNode.getValue(), addValueKeyDescriptorCallback, mode);
        }

        @Override
        int getTokenIndex() {
            return keyNode.getTokenIndex();
        }
    }


}
