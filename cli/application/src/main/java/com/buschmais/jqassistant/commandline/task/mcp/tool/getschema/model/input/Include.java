package com.buschmais.jqassistant.commandline.task.mcp.tool.getschema.model.input;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class Include {

    boolean properties = false;

    boolean relationships = false;

}
