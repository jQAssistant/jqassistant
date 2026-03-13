package com.buschmais.jqassistant.plugin.yaml2.impl.scanner.parsing;

import org.snakeyaml.engine.v2.events.ScalarEvent;

public class SimpleKeyNode extends KeyNode<ScalarEvent> {

    private ScalarNode keyNode;

    public SimpleKeyNode(ScalarNode node, int tokenIndex) {
        super(node.getEvent(), tokenIndex);
        keyNode = node;
    }

    @Deprecated
    public SimpleKeyNode(ScalarEvent event, int o) {
        super(event, o);
    }

    public ScalarNode getKey() {
        return keyNode;
    }

    public String getKeyName() {
        return getEvent().getValue();
    }

    @Override
    protected String generateTextPresentation() {
        return "=SimpleKeyNode [" + getEvent() + "]";
    }

}
