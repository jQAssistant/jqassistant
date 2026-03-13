package com.buschmais.jqassistant.plugin.yaml2.impl.scanner.parsing;

import org.snakeyaml.engine.v2.events.Event;

public interface EventSupport<N extends Event> {
    N getEvent();
}
