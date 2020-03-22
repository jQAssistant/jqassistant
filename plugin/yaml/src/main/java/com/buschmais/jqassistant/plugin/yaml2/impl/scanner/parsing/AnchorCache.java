package com.buschmais.jqassistant.plugin.yaml2.impl.scanner.parsing;

import java.util.HashMap;
import java.util.Optional;

import org.snakeyaml.engine.v2.events.NodeEvent;

import static java.util.Optional.*;

public class AnchorCache {
    private HashMap<String, BaseNode<? extends NodeEvent>> references = new HashMap<>();

    public boolean hasAnchor(String anchor) {
        return references.containsKey(anchor);
    }

    public Optional<BaseNode<? extends NodeEvent>> getAnchor(String anchor) {
        return ofNullable(references.get(anchor));
    }

    public void addAnchor(BaseNode<?> node) {
        String anchorName = node.getAnchor()
                                .orElseThrow(() -> new IllegalStateException("Only nodes with an anchor can be added"));

        references.put(anchorName, node);
    }
}
