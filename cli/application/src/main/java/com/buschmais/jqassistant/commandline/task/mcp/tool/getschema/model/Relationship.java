package com.buschmais.jqassistant.commandline.task.mcp.tool.getschema.model;

import java.util.Set;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@ToString
public class Relationship extends AbstractPropertyContainer {

    private String type;

    private Set<String> targetLabels;

}
