package com.buschmais.jqassistant.plugin.yaml2.impl.scanner.graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.yaml2.api.model.YMLDescriptor;
import com.buschmais.jqassistant.plugin.yaml2.api.model.YMLDocumentDescriptor;
import com.buschmais.jqassistant.plugin.yaml2.impl.scanner.parsing.AbstractBaseNode;
import com.buschmais.jqassistant.plugin.yaml2.impl.scanner.parsing.StreamNode;

import static java.lang.String.format;

public class GraphGenerator {
    private final List<NodeProcessor> processors;

    public enum Mode {
        STANDARD(true),
        REFERENCE(false);

        private final boolean inStandardMode;

        Mode(boolean mode) {
            inStandardMode = mode;
        }

        public boolean isInReferenceMode() {
            return !inStandardMode;
        }

        public boolean isInStandardMode() {
            return inStandardMode;
        }
    }


    public GraphGenerator(Store store) {
        AnchorCache anchorCache = new AnchorCache();
        AliasLinker aliasLinker = new AliasLinker(store, anchorCache);

        AliasNodeProcessor aliasNodeProcessor = new AliasNodeProcessor(store, this, anchorCache);
        AnchorHandler anchorHandler = new AnchorHandler(store, anchorCache);
        DocumentNodeProcessor documentNodeProcessor = new DocumentNodeProcessor(store, this);
        SequenceNodeProcessor sequenceNodeProcessor = new SequenceNodeProcessor(store, this, anchorHandler, aliasLinker);
        ScalarNodeProcessor scalarNodeProcessor = new ScalarNodeProcessor(store, anchorHandler);
        SimpleKeyNodeProcessor simpleKeyNodeProcessor = new SimpleKeyNodeProcessor(store, anchorHandler);
        MapNodeProcessor mapNodeProcessor = new MapNodeProcessor(store, this, anchorHandler, aliasLinker);

       this.processors = Arrays.asList(aliasNodeProcessor, documentNodeProcessor,
                                       mapNodeProcessor, sequenceNodeProcessor,
                                       scalarNodeProcessor, simpleKeyNodeProcessor);
    }

    public Collection<YMLDocumentDescriptor> generate(StreamNode root) {
        ArrayList<YMLDocumentDescriptor> result = new ArrayList<>(1);
        Callback<YMLDocumentDescriptor> callback = result::add;

        root.getDocuments().forEach(documentNode -> traverse(documentNode, callback, Mode.STANDARD));

        return result;
    }

    void traverse(AbstractBaseNode node, Callback<? extends YMLDescriptor> callback, Mode mode) {
        Supplier<IllegalStateException> exceptionSupplier = () -> {
            String message = format("Failed to find processor for node class '%s'",
                                    node.getClass().getCanonicalName());
            return new IllegalStateException(message);
        };

        processors.stream()
                  .filter(h -> h.accepts(node))
                  .findFirst()
                  .orElseThrow(exceptionSupplier)
                  .process(node, callback, mode);
    }


}
