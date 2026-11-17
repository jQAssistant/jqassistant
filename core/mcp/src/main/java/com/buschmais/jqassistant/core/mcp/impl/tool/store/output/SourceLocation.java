package com.buschmais.jqassistant.core.mcp.impl.tool.store.output;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SourceLocation {

    private String path;
    private Integer startLine;
    private Integer endLine;

}
