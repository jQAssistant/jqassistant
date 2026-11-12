package com.buschmais.jqassistant.core.mcp.impl.tool.getschema.input;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class Filter {

    private List<List<String>> labels;

}
