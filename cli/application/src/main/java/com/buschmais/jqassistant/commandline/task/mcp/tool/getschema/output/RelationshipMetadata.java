package com.buschmais.jqassistant.commandline.task.mcp.tool.getschema.output;

import java.util.Set;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@ToString
public class RelationshipMetadata extends AbstractPropertyContainerMetadata {

    private String type;

    private Set<String> targetLabels;

}
