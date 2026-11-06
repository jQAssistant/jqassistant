package com.buschmais.jqassistant.commandline.task.mcp.tool.store.model;

import java.util.Map;

import lombok.Getter;
import lombok.Singular;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@ToString
public abstract class AbstractPropertyContainer {

    @Singular
    private Map<String, Object> properties;

    private SourceLocation sourceLocation;

}
