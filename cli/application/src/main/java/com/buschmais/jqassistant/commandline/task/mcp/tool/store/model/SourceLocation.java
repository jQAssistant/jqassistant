package com.buschmais.jqassistant.commandline.task.mcp.tool.store.model;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class SourceLocation {

    private String path;
    private Integer startLine;
    private Integer endLine;

}
