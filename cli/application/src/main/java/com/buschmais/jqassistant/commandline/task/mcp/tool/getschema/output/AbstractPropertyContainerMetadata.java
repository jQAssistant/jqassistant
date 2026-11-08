package com.buschmais.jqassistant.commandline.task.mcp.tool.getschema.output;

import java.util.List;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@ToString
public abstract class AbstractPropertyContainerMetadata {

    private List<PropertyMetadata> properties;

}
