package com.buschmais.jqassistant.core.mcp.impl.tool.store;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.UnaryOperator;

import com.buschmais.jqassistant.core.mcp.impl.tool.Tool;
import com.buschmais.jqassistant.core.mcp.impl.tool.store.input.ExecuteCypherArguments;
import com.buschmais.jqassistant.core.mcp.impl.tool.store.output.ExecuteCypherResult;
import com.buschmais.jqassistant.core.mcp.impl.tool.store.output.Node;
import com.buschmais.jqassistant.core.mcp.impl.tool.store.output.Relationship;
import com.buschmais.jqassistant.core.mcp.impl.tool.store.output.SourceLocation;
import com.buschmais.jqassistant.core.report.api.ReportHelper;
import com.buschmais.jqassistant.core.report.api.model.source.FileLocation;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.xo.api.CompositeObject;
import com.buschmais.xo.api.Query;
import com.buschmais.xo.api.XOException;
import com.buschmais.xo.api.XOManager;
import com.buschmais.xo.neo4j.api.model.Neo4jLabel;
import com.buschmais.xo.neo4j.api.model.Neo4jNode;
import com.buschmais.xo.neo4j.api.model.Neo4jRelationship;
import com.buschmais.xo.neo4j.api.model.Neo4jRelationshipType;

import io.modelcontextprotocol.json.jackson3.JacksonMcpJsonMapper;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.RequiredArgsConstructor;

import static com.buschmais.jqassistant.core.mcp.impl.tool.store.output.ExecuteCypherResult.Status.ERROR;
import static com.buschmais.jqassistant.core.mcp.impl.tool.store.output.ExecuteCypherResult.Status.SUCCESS;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.StreamSupport.stream;

@RequiredArgsConstructor
public class ExecuteCypherTool implements Tool {

    static final int DEFAULT_MAX_ROWS = 1000;

    private final Store store;

    private final JacksonMcpJsonMapper mcpJsonMapper;

    @Override
    public String getInputSchema() {
        return """
            {
              "type": "object",
              "properties": {
                "query": {
                  "type": "string",
                  "description": "The Cypher query to execute, parameters are referenced as $name."
                },
                "parameters": {
                  "type": "object",
                  "description": "An optional map of key-value pairs to be used as query parameters for the Cypher query.",
                  "additionalProperties": true
                },
                "maxRows": {
                  "type": "integer",
                  "minimum": 1,
                  "default": %d,
                  "description": "The maximum number of rows to return, further rows are omitted and indicated by 'truncated'. Prefer LIMIT within the query to restrict the result."
                }
              },
              "required": ["query"],
              "additionalProperties": false
            }
            """.formatted(DEFAULT_MAX_ROWS);
    }

    @Override
    public Optional<String> getOutputSchema() {
        return Optional.of("""
            {
              "$schema": "http://json-schema.org/draft-07/schema#",
              "title": "jQAssistant Execute Cypher Output",
              "description": "Schema for the output of the jQAssistant execute-cypher tool, representing the status and the result rows of a Cypher query.",
              "type": "object",
              "properties": {
                "status": {
                  "type": "string",
                  "enum": ["SUCCESS", "ERROR"],
                  "description": "The status of the query execution."
                },
                "rowCount": {
                  "type": "integer",
                  "minimum": 0,
                  "description": "The number of returned rows, 0 if the status is ERROR."
                },
                "truncated": {
                  "type": "boolean",
                  "description": "True if the query returned more than 'maxRows' rows and the result has been truncated."
                },
                "rows": {
                  "type": ["array", "null"],
                  "description": "The result rows of the Cypher query if the status is SUCCESS, each row maps column names to values.",
                  "items": {
                    "type": "object",
                    "additionalProperties": { "$ref": "#/definitions/CypherValue" }
                  }
                },
                "errorMessage": {
                  "type": ["string", "null"],
                  "description": "The error message if the status is ERROR."
                }
              },
              "required": ["status", "rowCount", "truncated"],
              "if": {
                "properties": { "status": { "const": "SUCCESS" } }
              },
              "then": {
                "properties": { "rows": { "type": "array" } },
                "required": ["rows"]
              },
              "else": {
                "properties": { "errorMessage": { "type": "string" } },
                "required": ["errorMessage"]
              },
              "definitions": {
                "CypherValue": {
                  "description": "A value returned by a Cypher query.",
                  "anyOf": [
                    { "$ref": "#/definitions/Node" },
                    { "$ref": "#/definitions/Relationship" },
                    { "$ref": "#/definitions/Map" },
                    { "$ref": "#/definitions/List" },
                    { "$ref": "#/definitions/Primitive" }
                  ]
                },
                "Node": {
                  "type": "object",
                  "description": "A node of the graph.",
                  "properties": {
                    "kind": { "const": "node", "description": "Discriminator identifying the value as a node." },
                    "labels": { "type": "array", "description": "The labels of the node.", "items": { "type": "string" }, "uniqueItems": true },
                    "properties": { "$ref": "#/definitions/Properties" },
                    "sourceLocation": { "$ref": "#/definitions/SourceLocation" }
                  },
                  "required": ["kind", "labels", "properties"]
                },
                "Relationship": {
                  "type": "object",
                  "description": "A relationship of the graph.",
                  "properties": {
                    "kind": { "const": "relationship", "description": "Discriminator identifying the value as a relationship." },
                    "type": { "type": "string", "description": "The type of the relationship." },
                    "properties": { "$ref": "#/definitions/Properties" },
                    "sourceLocation": { "$ref": "#/definitions/SourceLocation" }
                  },
                  "required": ["kind", "type", "properties"]
                },
                "Properties": {
                  "type": "object",
                  "description": "The properties of a node or relationship, values are primitives or arrays of primitives.",
                  "additionalProperties": {
                    "anyOf": [
                      { "$ref": "#/definitions/Primitive" },
                      { "type": "array", "items": { "$ref": "#/definitions/Primitive" } }
                    ]
                  }
                },
                "Map": {
                  "type": "object",
                  "description": "A Cypher map.",
                  "additionalProperties": { "$ref": "#/definitions/CypherValue" }
                },
                "List": {
                  "type": "array",
                  "description": "A Cypher list.",
                  "items": { "$ref": "#/definitions/CypherValue" }
                },
                "Primitive": {
                  "description": "A primitive value, values without a JSON representation (e.g. temporal or spatial values) are provided as strings.",
                  "type": ["string", "number", "boolean", "null"]
                },
                "SourceLocation": {
                  "type": "object",
                  "description": "The location of the source (e.g. file) a node or relationship has been created from.",
                  "properties": {
                    "path": { "type": "string", "description": "The path of the source." },
                    "startLine": { "type": "integer", "description": "The start line within the source, if available." },
                    "endLine": { "type": "integer", "description": "The end line within the source, if available." }
                  },
                  "required": ["path"]
                }
              }
            }
            """);
    }

    @Override
    public McpSchema.ToolAnnotations getAnnotations() {
        return McpSchema.ToolAnnotations.builder()
            .readOnlyHint(false)
            .destructiveHint(true)
            .idempotentHint(false)
            .openWorldHint(false)
            .build();
    }

    @Override
    public String getName() {
        return "execute-cypher";
    }

    @Override
    public String getDescription() {
        return """
            Executes a Cypher statement with optional parameters (referenced as $name) against the jQAssistant Neo4j database.
            Returns the result rows enriched with source locations for nodes and relationships if available. The number of returned rows is limited by 'maxRows' (default: %d), use LIMIT within the query to restrict large results.
            Use the tools get-schema and get-concepts to discover the available labels, properties and relationships.
            WARNING: Statements are not restricted to read-only access, i.e. writing clauses (e.g. CREATE, MERGE, SET, DELETE) modify the graph. Prefer read-only statements unless a modification is explicitly requested.""".formatted(DEFAULT_MAX_ROWS);
    }

    @Override
    public Object execute(Map<String, Object> arguments) {
        ExecuteCypherArguments executeCypherArguments = mcpJsonMapper.convertValue(arguments, ExecuteCypherArguments.class);
        String query = executeCypherArguments.getQuery();
        Map<String, Object> parameters = executeCypherArguments.getParameters();
        int maxRows = executeCypherArguments.getMaxRows() != null ? executeCypherArguments.getMaxRows() : DEFAULT_MAX_ROWS;
        try (XOManager xoManager = store.getXOManagerFactory()
            .createXOManager()) {
            try (Query.Result<Query.Result.CompositeRowObject> result = xoManager.createQuery(query)
                .withParameters(parameters != null ? parameters : emptyMap())
                .execute()) {
                // fetch one additional row to detect if the result exceeds the limit
                List<Map<String, Object>> rows = result.asStream()
                    .limit(maxRows + 1L)
                    .map(row -> {
                        Map<String, Object> mappedRow = new LinkedHashMap<>();
                        row.getColumns()
                            .forEach(column -> mappedRow.put(column, map(row.get(column, Object.class))));
                        return mappedRow;
                    })
                    .collect(toList());
                boolean truncated = rows.size() > maxRows;
                if (truncated) {
                    rows = rows.subList(0, maxRows);
                }
                return ExecuteCypherResult.builder()
                    .status(SUCCESS)
                    .rowCount(rows.size())
                    .truncated(truncated)
                    .rows(rows)
                    .build();
            } catch (XOException e) {
                return ExecuteCypherResult.builder()
                    .status(ERROR)
                    .errorMessage(e.getMessage() != null ? e.getMessage() : e.getClass()
                        .getName())
                    .build();
            }
        }
    }

    /**
     * Maps a value returned by a Cypher query to a JSON compatible representation.
     * <p>
     * Paths are provided by XO as lists of alternating nodes and relationships and are therefore covered by the {@link Iterable} mapping.
     *
     * @param value
     *     The value.
     * @return The mapped value.
     */
    private static Object map(Object value) {
        if (value instanceof CompositeObject compositeObject) {
            return mapCompositeObject(compositeObject);
        } else if (value instanceof Iterable<?> iterable) {
            return stream(iterable.spliterator(), false).map(ExecuteCypherTool::map)
                .collect(toList());
        } else if (value != null && value.getClass()
            .isArray()) {
            return mapArray(value, ExecuteCypherTool::map);
        } else if (value instanceof Map<?, ?> map) {
            Map<String, Object> mappedMap = new LinkedHashMap<>();
            map.forEach((key, entryValue) -> mappedMap.put(String.valueOf(key), map(entryValue)));
            return mappedMap;
        }
        return mapPrimitive(value);
    }

    private static Object mapCompositeObject(CompositeObject compositeObject) {
        Optional<SourceLocation> optionalSourceLocation = ReportHelper.getSourceLocation(compositeObject)
            .map(ExecuteCypherTool::mapSourceLocation);
        Object delegate = compositeObject.getDelegate();
        if (delegate instanceof Neo4jNode<? extends Neo4jLabel, ?, ?, ?> neo4jNode) {
            Iterable<? extends Neo4jLabel> labels = neo4jNode.getLabels();
            Node.NodeBuilder<?, ?> nodeBuilder = Node.builder()
                .labels(stream(labels.spliterator(), false).map(Neo4jLabel::getName)
                    .collect(toSet()))
                .properties(mapProperties(neo4jNode.getProperties()));
            optionalSourceLocation.ifPresent(nodeBuilder::sourceLocation);
            return nodeBuilder.build();
        } else if (delegate instanceof Neo4jRelationship<?, ?, ?, ? extends Neo4jRelationshipType, ?> neo4jRelationship) {
            Relationship.RelationshipBuilder<?, ?> relationshipBuilder = Relationship.builder()
                .type(neo4jRelationship.getType()
                    .getName())
                .properties(mapProperties(neo4jRelationship.getProperties()));
            optionalSourceLocation.ifPresent(relationshipBuilder::sourceLocation);
            return relationshipBuilder.build();
        }
        // neither a node nor a relationship, do not expose the internal proxy
        return String.valueOf(delegate);
    }

    /**
     * Maps the properties of a node or relationship, values are either primitives or arrays of primitives.
     */
    private static Map<String, Object> mapProperties(Map<String, Object> properties) {
        Map<String, Object> mappedProperties = new LinkedHashMap<>();
        properties.forEach((key, value) -> mappedProperties.put(key, value != null && value.getClass()
            .isArray() ? mapArray(value, ExecuteCypherTool::mapPrimitive) : mapPrimitive(value)));
        return mappedProperties;
    }

    /**
     * Maps an array (including arrays of primitive types) to a list.
     */
    private static List<Object> mapArray(Object array, UnaryOperator<Object> elementMapper) {
        int length = Array.getLength(array);
        List<Object> list = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            list.add(elementMapper.apply(Array.get(array, i)));
        }
        return list;
    }

    /**
     * Maps a primitive value: JSON types are passed through, all other values (e.g. temporal or spatial values) are represented by their string
     * representation.
     */
    private static Object mapPrimitive(Object value) {
        if (value == null || value instanceof String || value instanceof Number || value instanceof Boolean) {
            return value;
        }
        return value.toString();
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
