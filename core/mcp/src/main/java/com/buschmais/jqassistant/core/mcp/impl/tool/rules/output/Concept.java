package com.buschmais.jqassistant.core.mcp.impl.tool.rules.output;

import java.util.Set;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class Concept {

    private String id;

    private String description;

    private Set<String> providesConcepts;

    private Set<String> requiresConcepts;

    private String cypher;

}
