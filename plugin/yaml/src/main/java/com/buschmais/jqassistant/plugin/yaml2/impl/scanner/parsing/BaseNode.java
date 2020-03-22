package com.buschmais.jqassistant.plugin.yaml2.impl.scanner.parsing;

import org.snakeyaml.engine.v2.events.NodeEvent;

public abstract class BaseNode<T extends NodeEvent>
    extends AbstractBaseNode
    implements AnchorSupport<T>, EventSupport<T>
{
    private final T event;

    public BaseNode(T event, int tokenIndex) {
        super(tokenIndex);
        this.event = event;
    }

    @Override
    public T getEvent() {
        return event;
    }
}
