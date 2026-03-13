package com.buschmais.jqassistant.plugin.yaml2.impl.scanner.graph;

import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.yaml2.api.model.YMLAliasDescriptor;
import com.buschmais.jqassistant.plugin.yaml2.api.model.YMLDescriptor;
import com.buschmais.jqassistant.plugin.yaml2.impl.scanner.parsing.NodeWithAliasName;

class AliasLinker {
    private final AnchorCache anchorCache;
    private final Store store;

    public AliasLinker(Store store, AnchorCache anchorCache) {
        this.anchorCache = anchorCache;
        this.store = store;
    }

    public void linkToAnchor(NodeWithAliasName aliasNode, YMLDescriptor descriptor) {
        YMLDescriptor anchorDescriptor = anchorCache.getTarget(aliasNode);

        YMLAliasDescriptor aliasDescriptor = store.addDescriptorType(descriptor, YMLAliasDescriptor.class, YMLAliasDescriptor.class);
        aliasDescriptor.setAnchor(anchorDescriptor);
    }
}
