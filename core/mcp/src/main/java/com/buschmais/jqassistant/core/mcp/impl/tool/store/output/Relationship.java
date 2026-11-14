package com.buschmais.jqassistant.core.mcp.impl.tool.store.output;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@ToString
public class Relationship extends AbstractPropertyContainer {

    private String type;

    @Override
    public String getKind() {
        return "relationship";
    }

}
