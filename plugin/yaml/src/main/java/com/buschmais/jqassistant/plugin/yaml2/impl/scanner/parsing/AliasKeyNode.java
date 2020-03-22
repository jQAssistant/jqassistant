package com.buschmais.jqassistant.plugin.yaml2.impl.scanner.parsing;

import org.snakeyaml.engine.v2.events.AliasEvent;
import org.snakeyaml.engine.v2.events.NodeEvent;

public class AliasKeyNode extends KeyNode<AliasEvent>
    implements NodeWithAliasName {

    private final BaseNode<? extends NodeEvent> referencedNode;

    public AliasKeyNode(AliasEvent event, int tokenIndex,
                        BaseNode<? extends NodeEvent> refNode) {
        super(event, tokenIndex);
        referencedNode = refNode;
    }

    public BaseNode<? extends NodeEvent> getKey() {
        return referencedNode;
    }

    public String getAnchorName() {
        return getEvent().getAlias().getValue();
    }

    @Override
    protected String generateTextPresentation() {
        return "=AliasKeyNode [" + getEvent() + "]";
    }


}
