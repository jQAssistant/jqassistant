package com.buschmais.jqassistant.commandline.task.mcp.tool;

public interface Tool {

    String getInputSchema();

    String getName();

    String getDescription();

    Object execute();

}
