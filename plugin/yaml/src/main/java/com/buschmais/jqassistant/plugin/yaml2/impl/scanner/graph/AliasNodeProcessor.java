package com.buschmais.jqassistant.plugin.yaml2.impl.scanner.graph;

import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.yaml2.api.model.YMLDescriptor;
import com.buschmais.jqassistant.plugin.yaml2.impl.scanner.parsing.AbstractBaseNode;
import com.buschmais.jqassistant.plugin.yaml2.impl.scanner.parsing.AliasNode;
import com.buschmais.jqassistant.plugin.yaml2.impl.scanner.parsing.BaseNode;

import static java.util.Optional.ofNullable;

public class AliasNodeProcessor implements NodeProcessor<AliasNode, YMLDescriptor> {

    private final Store store;
    private final AnchorCache anchorCache;
    private GraphGenerator generator;

    public AliasNodeProcessor(Store store, GraphGenerator generator, AnchorCache anchorCache) {
        this.store = store;
        this.anchorCache = anchorCache;
        this.generator = generator;
    }

    @Override
    public void process(AliasNode node, Callback<YMLDescriptor> callback, GraphGenerator.Mode mode) {
        BaseNode<?> aliasedNode = ofNullable(node.getAliasedNode()).orElseThrow(() -> {
            String anchor = node.getAnchorName();
            String message = String.format("Anchor '%s' not found in document", anchor);
            return new GraphGenerationFailedException(message);
        });

        generator.traverse(aliasedNode, callback, mode);
    }

    @Override
    public boolean accepts(AbstractBaseNode node) {
        return node.getClass().isAssignableFrom(AliasNode.class);
    }
}
