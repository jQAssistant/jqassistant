package com.buschmais.jqassistant.plugin.yaml2.impl.scanner.graph;

import java.util.Collection;
import java.util.TreeSet;

import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.yaml2.api.model.*;
import com.buschmais.jqassistant.plugin.yaml2.impl.scanner.parsing.*;

import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.toCollection;

class MapNodeProcessor implements NodeProcessor<MapNode, YMLMapDescriptor> {

    private final Store store;
    private final GraphGenerator generator;
    private final AnchorHandler anchorHandler;
    private final AliasLinker aliasLinker;

    public MapNodeProcessor(Store store, GraphGenerator generator, AnchorHandler anchorHandler,
                            AliasLinker aliasLinker) {
        this.store = store;
        this.generator = generator;
        this.anchorHandler = anchorHandler;
        this.aliasLinker = aliasLinker;
    }

    @Override
    public void process(MapNode node, Callback<YMLMapDescriptor> callback, GraphGenerator.Mode mode) {
        YMLMapDescriptor mapDescriptor = store.create(YMLMapDescriptor.class);
        node.getIndex().ifPresent(mapDescriptor::setIndex);

        anchorHandler.handleAnchor(node, mapDescriptor, mode);

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
                aliasLinker.linkToAnchor(aliasKeyNode, simpleKeyDescriptor);

                BaseNode<?> value = aliasKeyNode.getValue();
                Callback<YMLDescriptor> callback = simpleKeyDescriptor::setValue;

                if (value instanceof AliasNode) {
                    generator.traverse(value, callback, mode);
                } else {
                    generator.traverse(aliasKeyNode.getValue(), callback, mode);
                }
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
            anchorHandler.handleAnchor(keyNode, keyDescriptor, mode);

            Callback<YMLDescriptor> addValueDescriptorCallback = descriptor -> {
                store.addDescriptorType(descriptor, YMLValueDescriptor.class);
                keyDescriptor.setValue(descriptor);
            };

            if (keyNode.getValue().getClass().isAssignableFrom(AliasNode.class)) {
                AliasNode aliasNode = (AliasNode) keyNode.getValue();

                Callback<YMLDescriptor> createReferenceCallback = descriptor -> {
                    addValueDescriptorCallback.created(descriptor);
                    aliasLinker.linkToAnchor(aliasNode, descriptor);
                };

                generator.traverse(aliasNode, createReferenceCallback, GraphGenerator.Mode.REFERENCE);
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
            anchorHandler.handleAnchor(this.keyNode.getKeyNode(), keyDescriptor.getKey(), mode);
            generator.traverse(this.keyNode.getValue(), addValueKeyDescriptorCallback, mode);
        }

        @Override
        int getTokenIndex() {
            return keyNode.getTokenIndex();
        }
    }


}
