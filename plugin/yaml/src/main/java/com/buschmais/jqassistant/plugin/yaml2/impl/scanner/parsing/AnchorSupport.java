package com.buschmais.jqassistant.plugin.yaml2.impl.scanner.parsing;


import java.util.Optional;

import org.snakeyaml.engine.v2.events.NodeEvent;

public interface AnchorSupport<N extends NodeEvent>
    extends EventSupport<N> {
    default Optional<String> getAnchor() {
        return getEvent().getAnchor().map(anchor -> anchor.getAnchor());
    }


}
