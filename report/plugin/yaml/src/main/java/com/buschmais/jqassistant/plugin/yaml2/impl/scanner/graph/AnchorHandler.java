package com.buschmais.jqassistant.plugin.yaml2.impl.scanner.graph;

import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.yaml2.api.model.YMLAnchorDescriptor;
import com.buschmais.jqassistant.plugin.yaml2.api.model.YMLDescriptor;
import com.buschmais.jqassistant.plugin.yaml2.impl.scanner.parsing.AnchorSupport;

import org.snakeyaml.engine.v2.events.NodeEvent;

class AnchorHandler {

    private final Store store;
    private AnchorCache anchorCache;

    public AnchorHandler(Store store, AnchorCache cache) {
        this.store = store;
        this.anchorCache = cache;
    }

    public void handleAnchor(AnchorSupport<? extends NodeEvent> node, YMLDescriptor descriptor, GraphGenerator.Mode mode) {
        boolean createAnchor = mode.isInStandardMode() && node.getAnchor().isPresent();

        if (createAnchor) {
            String anchor = node.getAnchor().get();
            YMLAnchorDescriptor ymlAnchorDescriptor = store.addDescriptorType(descriptor, YMLAnchorDescriptor.class);
            ymlAnchorDescriptor.setAnchorName(anchor);

            anchorCache.addAlias(anchor, ymlAnchorDescriptor);
        }
    }
}
