package com.buschmais.jqassistant.core.mcp.impl.tool.getschema.output;

import java.util.List;
import java.util.Set;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@ToString
public class NodeMetadata extends AbstractPropertyContainerMetadata {

    private Set<String> labels;

    private List<RelationshipMetadata> relationships;

}
