package com.buschmais.jqassistant.commandline.task.mcp.tool.getschema.input;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class Include {

    boolean properties = true;

    boolean relationships = false;

}
