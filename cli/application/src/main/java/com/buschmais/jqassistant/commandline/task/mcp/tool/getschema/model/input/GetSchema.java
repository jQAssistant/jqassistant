package com.buschmais.jqassistant.commandline.task.mcp.tool.getschema.model.input;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class GetSchema {

    private Filter filter;

    private Include include;

}
