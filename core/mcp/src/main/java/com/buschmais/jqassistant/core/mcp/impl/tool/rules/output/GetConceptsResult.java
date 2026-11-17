package com.buschmais.jqassistant.core.mcp.impl.tool.rules.output;

import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class GetConceptsResult {

    private List<Concept> concepts;

}
