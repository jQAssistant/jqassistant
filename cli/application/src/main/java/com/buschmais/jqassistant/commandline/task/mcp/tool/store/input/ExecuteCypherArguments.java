package com.buschmais.jqassistant.commandline.task.mcp.tool.store.input;

import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class ExecuteCypherArguments {

    private String query;

    private Map<String, Object> parameters;

    private Integer maxRows;

}
