package com.buschmais.jqassistant.plugin.yaml2.impl.scanner.graph;

import java.util.function.Function;

import com.buschmais.jqassistant.plugin.yaml2.impl.scanner.parsing.AliasNode;
import com.buschmais.jqassistant.plugin.yaml2.impl.scanner.parsing.BaseNode;

import org.snakeyaml.engine.v2.events.Event;
import org.snakeyaml.engine.v2.events.NodeEvent;

import static java.util.Optional.ofNullable;

public class ReferenceNodeGetter implements Function<AliasNode, BaseNode<? extends NodeEvent>> {
    @Override
    public BaseNode<? extends Event> apply(AliasNode aliasNode) {
        return ofNullable(aliasNode.getAliasedNode()).orElseThrow(() -> {
            String anchor = aliasNode.getAnchorName();
            String message = String.format("Anchor '%s' not found in document", anchor);
            return new GraphGenerationFailedException(message);
        });
    }
}
