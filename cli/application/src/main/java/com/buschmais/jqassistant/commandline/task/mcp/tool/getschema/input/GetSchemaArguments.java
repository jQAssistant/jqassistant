package com.buschmais.jqassistant.commandline.task.mcp.tool.getschema.input;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class GetSchemaArguments {

    private Filter filter;

    private Include include;

}
