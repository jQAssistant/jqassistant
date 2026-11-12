package com.buschmais.jqassistant.core.mcp.impl.tool.rules;

import java.util.*;

import com.buschmais.jqassistant.core.mcp.impl.tool.Tool;
import com.buschmais.jqassistant.core.mcp.impl.tool.rules.input.Filter;
import com.buschmais.jqassistant.core.mcp.impl.tool.rules.input.GetConceptsArguments;
import com.buschmais.jqassistant.core.mcp.impl.tool.rules.input.Include;
import com.buschmais.jqassistant.core.mcp.impl.tool.rules.output.Concept;
import com.buschmais.jqassistant.core.mcp.impl.tool.rules.output.GetConceptsResult;
import com.buschmais.jqassistant.core.rule.api.filter.RuleFilter;
import com.buschmais.jqassistant.core.rule.api.model.Executable;
import com.buschmais.jqassistant.core.rule.api.model.RuleSet;

import io.modelcontextprotocol.json.jackson3.JacksonMcpJsonMapper;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.RequiredArgsConstructor;

import static java.util.stream.Collectors.*;

@RequiredArgsConstructor
public class GetConceptsTool implements Tool {

    private final RuleSet ruleSet;

    private final JacksonMcpJsonMapper mcpJsonMapper;

    @Override
    public String getInputSchema() {
        return """
            {
              "type": "object",
              "properties": {
                "filter": {
                  "type": "object",
                  "description": "Defines an optional filter for the concepts to return. If omitted, all available concepts are returned.",
                  "properties": {
                    "ids": {
                      "type": "array",
                      "description": "A list of concept id patterns, a concept matches if its id matches at least one of them.",
                      "items": {
                        "type": "string",
                        "description": "A concept id, may contain the wildcards '*' and '?', e.g. 'java:*' or 'spring-*:Controller'."
                      }
                    }
                  },
                  "additionalProperties": false
                },
                "include": {
                  "type": "object",
                  "description": "Defines the optional concept items to be included.",
                  "properties": {
                    "cypher": {
                      "type": "boolean",
                      "default": false,
                      "description": "If set to true include the Cypher query of each concept."
                    }
                  },
                  "additionalProperties": false
                }
              },
              "required": [],
              "additionalProperties": false
            }
            """;
    }

    @Override
    public Optional<String> getOutputSchema() {
        return Optional.of("""
            {
              "$schema": "http://json-schema.org/draft-07/schema#",
              "title": "jQAssistant Get Concepts Output",
              "description": "Schema for the output of the jQAssistant get-concepts tool, representing the available concepts.",
              "type": "object",
              "properties": {
                "concepts": {
                  "type": "array",
                  "description": "The concepts matching the filter.",
                  "items": { "$ref": "#/definitions/Concept" }
                }
              },
              "required": ["concepts"],
              "definitions": {
                "Concept": {
                  "type": "object",
                  "description": "A jQAssistant concept.",
                  "properties": {
                    "id": { "type": "string", "description": "The id of the concept." },
                    "description": { "type": ["string", "null"], "description": "The description of the concept." },
                    "providesConcepts": {
                      "type": "array",
                      "description": "The ids of the concepts this concept provides to, i.e. which are extended by this concept.",
                      "items": { "type": "string" },
                      "uniqueItems": true
                    },
                    "requiresConcepts": {
                      "type": "array",
                      "description": "The ids of the concepts which are required by this concept, may contain the wildcards '*' and '?'.",
                      "items": { "type": "string" },
                      "uniqueItems": true
                    },
                    "cypher": {
                      "type": ["string", "null"],
                      "description": "The Cypher query of the concept, only provided if requested by 'include.cypher' and the concept is executed using Cypher."
                    }
                  },
                  "required": ["id", "providesConcepts", "requiresConcepts"]
                }
              }
            }
            """);
    }

    @Override
    public McpSchema.ToolAnnotations getAnnotations() {
        return McpSchema.ToolAnnotations.builder()
            .readOnlyHint(true)
            .destructiveHint(false)
            .idempotentHint(true)
            .openWorldHint(false)
            .build();
    }

    @Override
    public String getName() {
        return "get-concepts";
    }

    @Override
    public String getDescription() {
        return """
            Get the jQAssistant concepts available for the project, including those provided by plugins. Concepts are rules that enrich the graph by labels, properties and relationships (e.g. marking test classes), each provides a description and optionally its Cypher query (see 'include.cypher').
            Note that the enrichments of a concept are only present in the graph if it has been applied by a jQAssistant analysis.""";
    }

    @Override
    public Object execute(Map<String, Object> arguments) {
        GetConceptsArguments getConceptsArguments = mcpJsonMapper.convertValue(arguments, GetConceptsArguments.class);
        Include include = getConceptsArguments.getInclude();
        boolean includeCypher = include != null && include.isCypher();
        Filter filter = getConceptsArguments.getFilter();
        Set<String> filterIds = filter != null ? filter.getIds() : null;
        Map<String, List<String>> providedConceptIdsByProvidingConceptId = getProvidedConceptIdsByProvidingConceptId();
        List<Concept> concepts = ruleSet.getConceptBucket()
            .getAll()
            .stream()
            .filter(concept -> filterIds == null || filterIds.stream()
                .anyMatch(filterId -> RuleFilter.matches(concept.getId(), filterId)))
            .map(concept -> map(concept, providedConceptIdsByProvidingConceptId, includeCypher))
            .toList();
        return GetConceptsResult.builder()
            .concepts(concepts)
            .build();
    }

    /**
     * Determines the ids of the provided concepts per providing concept id, including provides relations declared by groups.
     */
    private Map<String, List<String>> getProvidedConceptIdsByProvidingConceptId() {
        return ruleSet.getProvidedConcepts()
            .values()
            .stream()
            .flatMap(Collection::stream)
            .collect(groupingBy(com.buschmais.jqassistant.core.rule.api.model.Concept.ProvidedConcept::getProvidingConceptId,
                mapping(com.buschmais.jqassistant.core.rule.api.model.Concept.ProvidedConcept::getProvidedConceptId,
                    collectingAndThen(toCollection(TreeSet::new), ArrayList::new))));
    }

    private static Concept map(com.buschmais.jqassistant.core.rule.api.model.Concept concept, Map<String, List<String>> providedConceptIdsByProvidingConceptId,
        boolean includeCypher) {
        Concept.ConceptBuilder conceptBuilder = Concept.builder()
            .id(concept.getId())
            .description(concept.getDescription())
            .providesConcepts(new TreeSet<>(providedConceptIdsByProvidingConceptId.getOrDefault(concept.getId(), List.of())))
            .requiresConcepts(new TreeSet<>(concept.getRequiresConcepts()
                .keySet()));
        if (includeCypher) {
            Executable<?> executable = concept.getExecutable();
            if (executable != null && "cypher".equals(executable.getLanguage())) {
                conceptBuilder.cypher((String) executable.getSource());
            }
        }
        return conceptBuilder.build();
    }
}
