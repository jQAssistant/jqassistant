package com.buschmais.jqassistant.plugin.yaml2.impl.scanner.parsing;

import org.snakeyaml.engine.v2.events.ScalarEvent;

// todo A key is actual also a node which can have an anchor
public class SimpleKeyNode extends KeyNode {

    private String keyName;

    public SimpleKeyNode(ScalarEvent event) {
        super(event);
    }

    public String getKeyName() {
        return keyName;
    }

    public void setKeyName(String name) {
        this.keyName = name;
    }
}
