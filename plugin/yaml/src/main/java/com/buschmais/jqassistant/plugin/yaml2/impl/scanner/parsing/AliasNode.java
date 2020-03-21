package com.buschmais.jqassistant.plugin.yaml2.impl.scanner.parsing;

import java.util.Objects;
import java.util.Optional;

import org.snakeyaml.engine.v2.events.AliasEvent;

public class AliasNode extends BaseNode<AliasEvent> {
    private Integer index;
    private BaseNode<?> referencedNode;

    public AliasNode(AliasEvent event) {
        super(event);
    }

    public Optional<Integer> getIndex() {
        return Optional.ofNullable(index);
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public void setReferencedNode(BaseNode<?> node) {
        this.referencedNode = Objects.requireNonNull(node);
    }

    public String getAnchorName() {
        return getEvent().getAlias().getValue();
    }

    public BaseNode<?> getReferencedNode() {
        return referencedNode;
    }
}
