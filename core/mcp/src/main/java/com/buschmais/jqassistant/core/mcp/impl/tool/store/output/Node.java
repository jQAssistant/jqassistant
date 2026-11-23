package com.buschmais.jqassistant.core.mcp.impl.tool.store.output;

import java.util.Set;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@ToString
public class Node extends AbstractPropertyContainer {

    private Set<String> labels;

    @Override
    public String getKind() {
        return "node";
    }

}
