package com.buschmais.jqassistant.plugin.yaml2.impl.scanner.parsing;

import java.util.HashMap;
import java.util.Optional;

public class ReferenceMap {
    private HashMap<String, BaseNode> references = new HashMap<>();


    public boolean hasAnchor(String anchor) {
        return references.containsKey(anchor);
    }

    public Optional<BaseNode<?>> getAnchor(String anchor) {
        return Optional.ofNullable(references.get(anchor));
    }

    public void addAnchor(String anchor, BaseNode<?> node) {
        references.put(anchor, node);
    }
}
