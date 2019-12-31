package com.buschmais.jqassistant.plugin.yaml2.impl.scanner.parsing;

import java.util.HashMap;

public class ReferenceMap {
    private HashMap<String, ParseNode> references = new HashMap<>();


    public boolean hasAnchor(String anchor) {
        return references.containsKey(anchor);
    }

    public ParseNode<?> getAnchor(String anchor) {
        return references.get(anchor);
    }

    public void addAnchor(String anchor, ParseNode<?> node) {
        references.put(anchor, node);
    }
}
