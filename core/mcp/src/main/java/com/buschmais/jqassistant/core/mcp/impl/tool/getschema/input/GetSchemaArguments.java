package com.buschmais.jqassistant.core.mcp.impl.tool.getschema.input;

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
