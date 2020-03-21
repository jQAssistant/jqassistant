package com.buschmais.jqassistant.plugin.yaml2.impl.scanner.graph;

import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.yaml2.api.model.*;
import com.buschmais.jqassistant.plugin.yaml2.impl.scanner.parsing.BaseNode;
import com.buschmais.jqassistant.plugin.yaml2.impl.scanner.parsing.SequenceNode;

import org.snakeyaml.engine.v2.events.Event;

public class SequenceNodeProcessor implements NodeProcessor<SequenceNode, YMLSequenceDescriptor> {

    private final Store store;
    private final GraphGenerator generator;
    private AnchorProcessor anchorProcessor;
    private ReferenceNodeGetter refNodeGetter = new ReferenceNodeGetter();

    private Comparator<YMLDescriptor> indexComparator = (lhs, rhs) -> {
        Integer lhsIndex = ((YMLIndexable) lhs).getIndex();
        Integer rhsIndex = ((YMLIndexable) rhs).getIndex();
        return Integer.compare(lhsIndex, rhsIndex);
    };
    private Consumer<YMLDescriptor> addItemDescriptorHandler;

    public SequenceNodeProcessor(Store store, GraphGenerator generator, AnchorProcessor anchorProcessor) {
        this.store = store;
        this.generator = generator;
        this.anchorProcessor = anchorProcessor;
    }

    @Override
    public void process(SequenceNode node, Callback<YMLSequenceDescriptor> callback, GraphGenerator.Mode mode) {
        YMLSequenceDescriptor sequenceDescriptor = store.create(YMLSequenceDescriptor.class);
        node.getIndex().ifPresent(sequenceDescriptor::setIndex);

        anchorProcessor.process(node, sequenceDescriptor, mode);

        addItemDescriptorHandler = descriptor -> store.addDescriptorType(descriptor, YMLItemDescriptor.class);

        node.getScalars().forEach(scalarNode -> {
            Callback<YMLScalarDescriptor> callbackForScalar = descriptor -> {
                sequenceDescriptor.getScalars().add(descriptor);
                store.addDescriptorType(descriptor, YMLItemDescriptor.class);
                addItemDescriptorHandler.accept(descriptor);
            };

            generator.traverse(scalarNode, callbackForScalar, mode);
        });

        node.getMaps().forEach(mapNode -> {
            Callback<YMLMapDescriptor> callbackForMap = descriptor -> {
                addItemDescriptorHandler.accept(descriptor);
                sequenceDescriptor.getMaps().add(descriptor);
            };
            generator.traverse(mapNode, callbackForMap, mode);
        });

        node.getSequences().forEach(seqNode -> {
            Callback<YMLSequenceDescriptor> callbackForSequence = descriptor -> {
                addItemDescriptorHandler.accept(descriptor);
                sequenceDescriptor.getSequences().add(descriptor);
            };

            generator.traverse(seqNode, callbackForSequence, mode);
        });


        node.getAliases().forEach(aliasNode -> {
            Callback<YMLDescriptor> callbackForAlias = descriptor -> {
                YMLIndexable scalarDescriptor = (YMLIndexable) descriptor;
                aliasNode.getIndex().ifPresent(scalarDescriptor::setIndex);
                addItemDescriptorHandler.accept(descriptor);

                if (descriptor instanceof YMLSequenceDescriptor) {
                    sequenceDescriptor.getSequences().add((YMLSequenceDescriptor) descriptor);
                } else if (descriptor instanceof YMLMapDescriptor) {
                    sequenceDescriptor.getMaps().add((YMLMapDescriptor) descriptor);
                } else if (descriptor instanceof YMLScalarDescriptor) {
                    sequenceDescriptor.getScalars().add((YMLScalarDescriptor) descriptor);
                }
            };

            BaseNode<?> referencedNode = refNodeGetter.apply(aliasNode);

            generator.traverse(referencedNode, callbackForAlias, GraphGenerator.Mode.REFERENCE);
        });

        Optional<? extends YMLDescriptor> first = findFirstSequenceItem(sequenceDescriptor);
        Optional<? extends YMLDescriptor> last = findLastSequenceItem(sequenceDescriptor);

        last.ifPresent(descriptor -> store.addDescriptorType(descriptor, YMLLastDescriptor.class));
        first.ifPresent(descriptor -> store.addDescriptorType(descriptor, YMLFirstDescriptor.class));

        callback.created(sequenceDescriptor);
    }

    @Override
    public boolean accepts(BaseNode<? extends Event> node) {
        return node.getClass().isAssignableFrom(SequenceNode.class);
    }

    private Optional<? extends YMLDescriptor> findFirstSequenceItem(YMLSequenceDescriptor sequenceDescriptor) {
        return Stream.of(sequenceDescriptor.getScalars(),
                         sequenceDescriptor.getSequences(),
                         sequenceDescriptor.getMaps())
                     .flatMap(Collection::stream)
                     .min(indexComparator);
    }

    private Optional<? extends YMLDescriptor> findLastSequenceItem(YMLSequenceDescriptor sequenceDescriptor) {
        return Stream.of(sequenceDescriptor.getScalars(),
                         sequenceDescriptor.getSequences(),
                         sequenceDescriptor.getMaps())
                     .flatMap(Collection::stream)
                     .max(indexComparator);
    }

}
