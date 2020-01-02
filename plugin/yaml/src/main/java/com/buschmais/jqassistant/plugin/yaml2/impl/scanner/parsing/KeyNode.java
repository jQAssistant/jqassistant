package com.buschmais.jqassistant.plugin.yaml2.impl.scanner.parsing;

import org.snakeyaml.engine.v2.events.ScalarEvent;

// todo A key is actual also a node which can have an anchor
public class KeyNode extends BaseNode<ScalarEvent> {

    private BaseNode<?> valueNode;
    private String keyName;

    public KeyNode(ScalarEvent event) {
        super(event);
    }

    public void setValue(BaseNode<?> node) {
        this.valueNode = node;
    }

    public BaseNode<?> getValue() {
        return valueNode;
    }

    public String getKeyName() {
        return keyName;
    }

    public void setKeyName(String name) {
        this.keyName = name;
    }
}
