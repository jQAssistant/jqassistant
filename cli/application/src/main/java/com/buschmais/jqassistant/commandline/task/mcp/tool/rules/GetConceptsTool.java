package com.buschmais.jqassistant.commandline.task.mcp.tool.rules;

import com.buschmais.jqassistant.commandline.task.mcp.tool.Tool;
import com.buschmais.jqassistant.core.rule.api.model.RuleSet;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetConceptsTool implements Tool {

    private final RuleSet ruleSet;

    @Override
    public String getInputSchema() {
        return """
            {
              "type": "object",
              "properties": {},
              "required": []
            }
            """;
    }

    @Override
    public String getName() {
        return "get-concepts";
    }

    @Override
    public String getDescription() {
        return "Get the jQAssistant concepts defined for the current project. They provide a description and an executable rule as Cypher query that enrich the graph by labels, properties and relationships.";
    }

    @Override
    public Object execute() {
        return ruleSet.getConceptBucket()
            .getAll();
    }
}
