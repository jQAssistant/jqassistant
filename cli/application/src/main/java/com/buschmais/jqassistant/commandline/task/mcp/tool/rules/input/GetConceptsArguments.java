package com.buschmais.jqassistant.commandline.task.mcp.tool.rules.input;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class GetConceptsArguments {

    private Filter filter;

    private Include include;

}
