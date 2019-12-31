package com.buschmais.jqassistant.plugin.yaml2.impl.scanner.parsing;

import org.snakeyaml.engine.v2.common.Anchor;
import org.snakeyaml.engine.v2.events.ScalarEvent;

public class ScalarNode extends ParseNode<ScalarEvent> {
    private int index;

    public ScalarNode(ScalarEvent event) {
        super(event);
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public String getScalarValue() {
        return getEvent().getValue();
    }

    public boolean hasAnchor() {
        return getEvent().getAnchor().isPresent();
    }


    public String getAnchor() {
        Anchor anchor = getEvent().getAnchor()
                                  .orElseThrow(() -> new IllegalStateException("No anchor present"));
        return anchor.getAnchor();
    }
}
