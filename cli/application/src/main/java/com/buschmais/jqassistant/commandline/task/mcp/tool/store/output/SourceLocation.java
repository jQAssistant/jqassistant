package com.buschmais.jqassistant.commandline.task.mcp.tool.store.output;

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
