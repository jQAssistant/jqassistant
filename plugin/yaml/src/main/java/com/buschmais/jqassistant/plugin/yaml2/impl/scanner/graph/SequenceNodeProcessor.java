package com.buschmais.jqassistant.plugin.yaml2.impl.scanner.graph;

import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.yaml2.api.model.*;
import com.buschmais.jqassistant.plugin.yaml2.impl.scanner.parsing.AbstractBaseNode;
import com.buschmais.jqassistant.plugin.yaml2.impl.scanner.parsing.AliasNode;
import com.buschmais.jqassistant.plugin.yaml2.impl.scanner.parsing.SequenceNode;

import static com.buschmais.jqassistant.plugin.yaml2.impl.scanner.graph.GraphGenerator.Mode.REFERENCE;

class SequenceNodeProcessor implements NodeProcessor<SequenceNode, YMLSequenceDescriptor> {
    private final Store store;
    private final GraphGenerator generator;
    private final AliasLinker aliasLinker;
    private final AnchorHandler anchorHandler;

    private static Comparator<YMLDescriptor> INDEX_COMPERATOR = (lhs, rhs) -> {
        Integer lhsIndex = ((YMLIndexable) lhs).getIndex();
        Integer rhsIndex = ((YMLIndexable) rhs).getIndex();
        return Integer.compare(lhsIndex, rhsIndex);
    };

    private final Consumer<YMLDescriptor> addItemDescriptorHandler;

    public SequenceNodeProcessor(Store store, GraphGenerator generator, AnchorHandler anchorHandler,
                                 AliasLinker aliasLinker) {
        this.store = store;
        this.generator = generator;
        this.anchorHandler = anchorHandler;
        this.aliasLinker = aliasLinker;
        this.addItemDescriptorHandler = descriptor -> store.addDescriptorType(descriptor, YMLItemDescriptor.class);
    }

    @Override
    public void process(SequenceNode node, Callback<YMLSequenceDescriptor> callback, GraphGenerator.Mode mode) {
        YMLSequenceDescriptor sequenceDescriptor = store.create(YMLSequenceDescriptor.class);
        node.getIndex().ifPresent(sequenceDescriptor::setIndex);

        Callback<YMLSequenceDescriptor> callbackForSequence = descriptor -> {
            addItemDescriptorHandler.accept(descriptor);
            sequenceDescriptor.getSequences().add(descriptor);
        };

        Callback<YMLMapDescriptor> callbackForMap = descriptor -> {
            addItemDescriptorHandler.accept(descriptor);
            sequenceDescriptor.getMaps().add(descriptor);
        };

        anchorHandler.handleAnchor(node, sequenceDescriptor, mode);

        node.getScalars().forEach(scalarNode -> {
            Callback<YMLScalarDescriptor> callbackForScalar = descriptor -> {
                sequenceDescriptor.getScalars().add(descriptor);
                store.addDescriptorType(descriptor, YMLItemDescriptor.class);
                addItemDescriptorHandler.accept(descriptor);
            };

            generator.traverse(scalarNode, callbackForScalar, mode);
        });

        node.getMaps().forEach(mapNode -> generator.traverse(mapNode, callbackForMap, mode));
        node.getSequences().forEach(seqNode -> generator.traverse(seqNode, callbackForSequence, mode));
        node.getAliases().forEach(aliasNode -> generator.traverse(aliasNode, callbackForAliasNode(sequenceDescriptor, aliasNode), REFERENCE));

        Optional<? extends YMLDescriptor> first = findFirstSequenceItem(sequenceDescriptor);
        Optional<? extends YMLDescriptor> last = findLastSequenceItem(sequenceDescriptor);

        last.ifPresent(descriptor -> store.addDescriptorType(descriptor, YMLLastDescriptor.class));
        first.ifPresent(descriptor -> store.addDescriptorType(descriptor, YMLFirstDescriptor.class));

        callback.created(sequenceDescriptor);
    }

    private Callback<YMLDescriptor> callbackForAliasNode(YMLSequenceDescriptor sequenceDescriptor, AliasNode aliasNode) {
        return descriptor -> {
            YMLIndexable scalarDescriptor = (YMLIndexable) descriptor;
            aliasNode.getIndex().ifPresent(scalarDescriptor::setIndex);
            addItemDescriptorHandler.accept(descriptor);

            if (descriptor instanceof YMLSequenceDescriptor) {
                sequenceDescriptor.getSequences().add((YMLSequenceDescriptor) descriptor);
            } else if (descriptor instanceof YMLMapDescriptor) {
                sequenceDescriptor.getMaps().add((YMLMapDescriptor) descriptor);
            } else if (descriptor instanceof YMLScalarDescriptor) {
                sequenceDescriptor.getScalars().add((YMLScalarDescriptor) descriptor);
            } else {
                String message = "Unsupported descriptor type";
                throw new IllegalStateException(message);
            }

            aliasLinker.linkToAnchor(aliasNode, descriptor);
        };
    }

    @Override
    public boolean accepts(AbstractBaseNode node) {
        return node.getClass().isAssignableFrom(SequenceNode.class);
    }

    private Optional<? extends YMLDescriptor> findFirstSequenceItem(YMLSequenceDescriptor sequenceDescriptor) {
        return Stream.of(sequenceDescriptor.getScalars(),
                         sequenceDescriptor.getSequences(),
                         sequenceDescriptor.getMaps())
                     .flatMap(Collection::stream)
                     .min(INDEX_COMPERATOR);
    }

    private Optional<? extends YMLDescriptor> findLastSequenceItem(YMLSequenceDescriptor sequenceDescriptor) {
        return Stream.of(sequenceDescriptor.getScalars(),
                         sequenceDescriptor.getSequences(),
                         sequenceDescriptor.getMaps())
                     .flatMap(Collection::stream)
                     .max(INDEX_COMPERATOR);
    }

}
