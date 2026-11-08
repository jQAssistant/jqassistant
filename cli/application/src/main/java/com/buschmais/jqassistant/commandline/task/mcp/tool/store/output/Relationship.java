package com.buschmais.jqassistant.commandline.task.mcp.tool.store.output;

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
