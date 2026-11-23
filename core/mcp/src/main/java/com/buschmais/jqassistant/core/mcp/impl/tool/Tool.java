package com.buschmais.jqassistant.core.mcp.impl.tool;

import java.util.Map;
import java.util.Optional;

import io.modelcontextprotocol.spec.McpSchema;

public interface Tool {

    String getName();

    String getDescription();

    String getInputSchema();

    default Optional<String> getOutputSchema() {
        return Optional.empty();
    }

    /**
     * Provides the annotations (hints) describing the behavior of the tool, e.g. if it is read-only.
     *
     * @return The {@link McpSchema.ToolAnnotations}.
     */
    McpSchema.ToolAnnotations getAnnotations();

    Object execute(Map<String, Object> arguments);

}
