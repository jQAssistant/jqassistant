package com.buschmais.jqassistant.commandline.task.mcp.tool.getschema.output;

import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.ToString;

@Builder
@Getter
@ToString
public class GetSchemaResult {

    @Singular
    private List<NodeMetadata> nodes;

}
