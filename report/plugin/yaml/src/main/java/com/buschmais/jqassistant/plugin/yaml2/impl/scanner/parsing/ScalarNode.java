package com.buschmais.jqassistant.plugin.yaml2.impl.scanner.parsing;

import java.util.Optional;

import org.snakeyaml.engine.v2.events.ScalarEvent;

public class ScalarNode extends BaseNode<ScalarEvent> {
    private Integer index;

    public ScalarNode(ScalarEvent event, int tokenIndex) {
        super(event, tokenIndex);
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public Optional<Integer> getIndex() {
        return Optional.ofNullable(index);
    }

    public String getScalarValue() {
        return getEvent().getValue();
    }

    @Override
    protected String generateTextPresentation() {
        return "=ScalarNode [" + getEvent() + "]";
    }
}
