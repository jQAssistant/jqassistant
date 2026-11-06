package com.buschmais.jqassistant.commandline.task.mcp.tool;

import java.util.Map;

public interface Tool {

    String getInputSchema();

    String getName();

    String getDescription();

    Object execute(Map<String, Object> arguments);

}
