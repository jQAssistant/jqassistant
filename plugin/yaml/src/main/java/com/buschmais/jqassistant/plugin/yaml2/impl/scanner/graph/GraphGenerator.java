package com.buschmais.jqassistant.plugin.yaml2.impl.scanner.graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.yaml2.api.model.YMLDescriptor;
import com.buschmais.jqassistant.plugin.yaml2.api.model.YMLDocumentDescriptor;
import com.buschmais.jqassistant.plugin.yaml2.impl.scanner.parsing.BaseNode;
import com.buschmais.jqassistant.plugin.yaml2.impl.scanner.parsing.StreamNode;

import org.snakeyaml.engine.v2.events.Event;

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
        AliasProcessor aliasProcessor = new AliasProcessor(store, anchorCache);
        AnchorProcessor anchorProcessor = new AnchorProcessor(store, anchorCache);
        DocumentNodeProcessor documentNodeProcessor = new DocumentNodeProcessor(store, this);
        SequenceNodeProcessor sequenceNodeProcessor = new SequenceNodeProcessor(store, this, anchorProcessor, aliasProcessor);
        ScalarNodeProcessor scalarNodeProcessor = new ScalarNodeProcessor(store, anchorProcessor);
        MapNodeProcessor mapNodeProcessor = new MapNodeProcessor(store, this, anchorProcessor, aliasProcessor);

        this.processors = Arrays.asList(documentNodeProcessor, sequenceNodeProcessor,
                                        mapNodeProcessor, scalarNodeProcessor);
    }

    public Collection<YMLDocumentDescriptor> generate(StreamNode root) {
        ArrayList<YMLDocumentDescriptor> result = new ArrayList<>(1);

        root.getDocuments().forEach(documentNode -> {
            Callback<YMLDocumentDescriptor> callback = descriptor -> result.add((YMLDocumentDescriptor) descriptor);

            traverse(documentNode, callback, Mode.STANDARD);
        });

        return result;
    }

    void traverse(BaseNode<? extends Event> node, Callback<? extends YMLDescriptor> callback, Mode mode) {
        processors.stream()
                  .filter(h -> h.accepts(node))
                  .findFirst()
                  .ifPresent(h -> h.process(node, callback, mode));
    }


}
