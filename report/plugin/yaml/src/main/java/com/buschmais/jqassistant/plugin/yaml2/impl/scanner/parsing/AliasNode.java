package com.buschmais.jqassistant.plugin.yaml2.impl.scanner.parsing;

import java.util.Objects;
import java.util.Optional;

import org.snakeyaml.engine.v2.events.AliasEvent;

public class AliasNode extends BaseNode<AliasEvent>
    implements NodeWithAliasName {
    private Integer index;
    private BaseNode<?> aliasedNode;

    public AliasNode(AliasEvent event, int o) {
        super(event, o);
    }

    public Optional<Integer> getIndex() {
        return Optional.ofNullable(index);
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public void setAliasedNode(BaseNode<?> node) {
        this.aliasedNode = Objects.requireNonNull(node);
    }

    public String getAnchorName() {
        return getEvent().getAlias().getValue();
    }

    public BaseNode<?> getAliasedNode() {
        return aliasedNode;
    }

    @Override
    protected String generateTextPresentation() {
        return "=AliasNode [" + getEvent() + "]";
    }

}
