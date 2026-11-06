package com.buschmais.jqassistant.commandline.task.mcp.tool.store;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.StreamSupport;

import com.buschmais.jqassistant.commandline.task.mcp.tool.Tool;
import com.buschmais.jqassistant.commandline.task.mcp.tool.store.model.Node;
import com.buschmais.jqassistant.commandline.task.mcp.tool.store.model.Relationship;
import com.buschmais.jqassistant.commandline.task.mcp.tool.store.model.SourceLocation;
import com.buschmais.jqassistant.core.report.api.ReportHelper;
import com.buschmais.jqassistant.core.report.api.model.source.FileLocation;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.xo.api.CompositeObject;
import com.buschmais.xo.api.Query;
import com.buschmais.xo.api.XOManager;
import com.buschmais.xo.neo4j.api.model.Neo4jLabel;
import com.buschmais.xo.neo4j.api.model.Neo4jNode;
import com.buschmais.xo.neo4j.api.model.Neo4jRelationship;
import com.buschmais.xo.neo4j.api.model.Neo4jRelationshipType;

import lombok.RequiredArgsConstructor;

import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.*;
import static java.util.stream.StreamSupport.stream;

@RequiredArgsConstructor
public class ExecuteCypherTool implements Tool {

    private final Store store;

    @Override
    public String getInputSchema() {
        return """
            {
              "type": "object",
              "properties": {
                "query": {
                  "type": "string",
                  "description": "The Cypher query to execute."
                },
                "parameters": {
                  "type": "object",
                  "description": "A map of key-value pairs to be used as query parameters for the Cypher query.",
                  "additionalProperties": true
                }
              },
              "required": [
                "query"
              ]
            }
            """;
    }

    @Override
    public String getName() {
        return "execute-cypher";
    }

    @Override
    public String getDescription() {
        return """
            Executes a Cypher statement with optional parameters. Returns the result enriched with source locations for nodes and relationships if available.
            """;
    }

    @Override
    public Object execute(Map<String, Object> arguments) {
        try (XOManager xoManager = store.getXOManagerFactory()
            .createXOManager()) {
            String query = (String) arguments.get("query");
            Map<String, Object> parameters = (Map<String, Object>) arguments.getOrDefault("parameters", emptyMap());
            try (Query.Result<Query.Result.CompositeRowObject> result = xoManager.createQuery(query)
                .withParameters(parameters)
                .execute()) {
                return result.asStream()
                    .map(row -> {
                        Map<String, Object> mappedRow = new LinkedHashMap<>();
                        row.getColumns()
                            .forEach(column -> mappedRow.put(column, map(row.get(column, Object.class))));
                        return mappedRow;
                    })
                    .collect(toList());
            }
        }
    }

    private static Object map(Object o) {
        if (o instanceof CompositeObject compositeObject) {
            Optional<SourceLocation> optionalSourceLocation = ReportHelper.getSourceLocation(o)
                .map(ExecuteCypherTool::mapSourceLocation);
            Object delegate = compositeObject.getDelegate();
            if (delegate instanceof Neo4jNode<? extends Neo4jLabel, ?, ?, ?> neo4jNode) {
                Iterable<? extends Neo4jLabel> labels = neo4jNode.getLabels();
                Node.NodeBuilder<?, ?> nodeBuilder = Node.builder()
                    .labels(stream(labels.spliterator(), false).map(Neo4jLabel::getName)
                        .collect(toSet()))
                    .properties(neo4jNode.getProperties());
                optionalSourceLocation.ifPresent(nodeBuilder::sourceLocation);
                return nodeBuilder.build();
            } else if (delegate instanceof Neo4jRelationship<?, ?, ?, ? extends Neo4jRelationshipType, ?> neo4jRelationship) {
                Relationship.RelationshipBuilder<?, ?> relationshipBuilder = Relationship.builder()
                    .type(neo4jRelationship.getType()
                        .getName())
                    .properties(neo4jRelationship.getProperties());
                optionalSourceLocation.ifPresent(relationshipBuilder::sourceLocation);
                return relationshipBuilder.build();
            }
        } else if (o instanceof Iterable<?> iterable) {
            return StreamSupport.stream(iterable.spliterator(), false)
                .map(ExecuteCypherTool::map)
                .collect(toList());
        } else if (o instanceof Map<?, ?> map) {
            return map.entrySet()
                .stream()
                .collect(toMap(ExecuteCypherTool::map, ExecuteCypherTool::map));
        }
        return o;
    }

    private static SourceLocation mapSourceLocation(com.buschmais.jqassistant.core.report.api.model.source.SourceLocation<?> sourceLocation) {
        SourceLocation.SourceLocationBuilder sourceLocationBuilder = SourceLocation.builder()
            .path(sourceLocation.getPath());
        if (sourceLocation instanceof FileLocation fileLocation) {
            fileLocation.getStartLine()
                .ifPresent(sourceLocationBuilder::startLine);
            fileLocation.getEndLine()
                .ifPresent(sourceLocationBuilder::endLine);
        }
        return sourceLocationBuilder.build();
    }
}
