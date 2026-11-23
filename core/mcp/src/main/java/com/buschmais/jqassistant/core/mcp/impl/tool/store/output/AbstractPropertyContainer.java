package com.buschmais.jqassistant.core.mcp.impl.tool.store.output;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class AbstractPropertyContainer {

    private Map<String, Object> properties;

    private SourceLocation sourceLocation;

    /**
     * Discriminator to distinguish nodes and relationships from plain Cypher maps.
     *
     * @return The kind.
     */
    public abstract String getKind();

}
