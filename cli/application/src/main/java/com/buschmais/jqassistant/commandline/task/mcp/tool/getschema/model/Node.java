package com.buschmais.jqassistant.commandline.task.mcp.tool.getschema.model;

import java.util.List;
import java.util.Set;

import lombok.Getter;
import lombok.Singular;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@ToString
public class Node extends AbstractPropertyContainer {

    private Set<String> labels;

    @Singular
    private List<Relationship> relationships;

}
