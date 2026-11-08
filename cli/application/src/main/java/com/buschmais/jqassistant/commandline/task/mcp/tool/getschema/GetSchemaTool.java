package com.buschmais.jqassistant.commandline.task.mcp.tool.getschema;

import java.util.*;

import com.buschmais.jqassistant.commandline.task.mcp.tool.Tool;
import com.buschmais.jqassistant.commandline.task.mcp.tool.getschema.input.Filter;
import com.buschmais.jqassistant.commandline.task.mcp.tool.getschema.input.GetSchemaArguments;
import com.buschmais.jqassistant.commandline.task.mcp.tool.getschema.input.Include;
import com.buschmais.jqassistant.commandline.task.mcp.tool.getschema.output.NodeMetadata;
import com.buschmais.jqassistant.commandline.task.mcp.tool.getschema.output.PropertyMetadata;
import com.buschmais.jqassistant.commandline.task.mcp.tool.getschema.output.RelationshipMetadata;
import com.buschmais.jqassistant.commandline.task.mcp.tool.getschema.output.GetSchemaResult;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.xo.api.metadata.MetadataProvider;
import com.buschmais.xo.api.metadata.method.AbstractEntityRelationMethodMetadata;
import com.buschmais.xo.api.metadata.method.AbstractRelationPropertyMethodMetadata;
import com.buschmais.xo.api.metadata.method.MethodMetadata;
import com.buschmais.xo.api.metadata.method.PrimitivePropertyMethodMetadata;
import com.buschmais.xo.api.metadata.reflection.AnnotatedMethod;
import com.buschmais.xo.api.metadata.reflection.GetPropertyMethod;
import com.buschmais.xo.api.metadata.type.EntityTypeMetadata;
import com.buschmais.xo.api.metadata.type.RelationTypeMetadata;
import com.buschmais.xo.api.metadata.type.TypeMetadata;
import com.buschmais.xo.neo4j.api.model.Neo4jLabel;
import com.buschmais.xo.neo4j.api.model.Neo4jRelationshipType;

import io.modelcontextprotocol.json.jackson3.JacksonMcpJsonMapper;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

import static com.buschmais.xo.api.metadata.type.RelationTypeMetadata.Direction.FROM;
import static java.util.Optional.empty;

@Slf4j
@RequiredArgsConstructor
public class GetSchemaTool implements Tool {

    private final Store store;

    private final JacksonMcpJsonMapper mcpJsonMapper;

    @Override
    public String getInputSchema() {
        return """
            {
              "type": "object",
              "properties": {
                "filter": {
                  "type": "object",
                  "description": "Defines an optional filter for the node types to return. If omitted, all node types are returned.",
                  "properties": {
                    "labels": {
                      "type": "array",
                      "description": "A list of label filters. A node type matches if it carries all labels of at least one label filter, i.e. label filters are combined by OR, the labels within a label filter by AND.",
                      "items": {
                        "type": "array",
                        "description": "A label filter consisting of labels which must all be present on a node type.",
                        "minItems": 1,
                        "items": {
                          "type": "string",
                          "description": "A label, e.g. 'Java' or 'Type'."
                        }
                      }
                    }
                  },
                  "additionalProperties": false
                },
                "include": {
                  "type": "object",
                  "description": "Defines the optional schema items to be included. If omitted, properties are included and relationships are excluded.",
                  "properties": {
                    "properties": {
                      "type": "boolean",
                      "default": true,
                      "description": "If set to true include the properties of each node type."
                    },
                    "relationships": {
                      "type": "boolean",
                      "default": false,
                      "description": "If set to true include the outgoing relationships of each node type."
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
              "title": "jQAssistant Get Schema Output",
              "description": "Schema for the output of the jQAssistant get-schema tool, representing the graph model created by the scanner plugins.",
              "type": "object",
              "properties": {
                "nodes": {
                  "type": "array",
                  "description": "The node types (i.e. label combinations) known to the scanner plugins and matching the filter.",
                  "items": { "$ref": "#/definitions/Node" }
                }
              },
              "required": ["nodes"],
              "definitions": {
                "Node": {
                  "type": "object",
                  "description": "A node type of the graph model.",
                  "properties": {
                    "labels": {
                      "type": "array",
                      "description": "The labels of the node type.",
                      "items": { "type": "string" },
                      "uniqueItems": true
                    },
                    "properties": {
                      "type": ["array", "null"],
                      "description": "The properties of the node type, only provided if requested by 'include.properties'.",
                      "items": { "$ref": "#/definitions/Property" }
                    },
                    "relationships": {
                      "type": ["array", "null"],
                      "description": "The outgoing relationships of the node type, only provided if requested by 'include.relationships'.",
                      "items": { "$ref": "#/definitions/Relationship" }
                    }
                  },
                  "required": ["labels"]
                },
                "Relationship": {
                  "type": "object",
                  "description": "An outgoing relationship of a node type.",
                  "properties": {
                    "type": { "type": "string", "description": "The type of the relationship, e.g. 'CONTAINS' or 'DECLARES'." },
                    "targetLabels": {
                      "type": "array",
                      "description": "The labels of the target node type of the relationship.",
                      "items": { "type": "string" },
                      "uniqueItems": true
                    },
                    "properties": {
                      "type": ["array", "null"],
                      "description": "The properties of the relationship, if any are defined.",
                      "items": { "$ref": "#/definitions/Property" }
                    }
                  },
                  "required": ["type", "targetLabels"]
                },
                "Property": {
                  "type": "object",
                  "description": "A property of a node or relationship type.",
                  "properties": {
                    "name": { "type": "string", "description": "The name of the property." },
                    "type": {
                      "type": "string",
                      "enum": ["STRING", "INTEGER", "FLOAT", "DATETIME", "BOOLEAN"],
                      "description": "The data type of the property."
                    }
                  },
                  "required": ["name", "type"]
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
        return "get-schema";
    }

    @Override
    public String getDescription() {
        return """
            Get the schema of the jQAssistant Neo4j database describing node types by their labels and optionally their properties (included by default) and outgoing relationships (excluded by default).
            The schema covers the graph model as created by the scanner plugins, labels and relationships added by concepts are not contained (use the tool get-concepts for them).""";
    }

    public GetSchemaResult execute(Map<String, Object> arguments) {
        GetSchemaArguments getSchemaArguments = mcpJsonMapper.convertValue(arguments, GetSchemaArguments.class);
        Filter filter = getSchemaArguments.getFilter();
        List<List<String>> labelFilters = filter != null ? filter.getLabels() : null;
        Include include = getSchemaArguments.getInclude() != null ? getSchemaArguments.getInclude() : new Include();
        boolean includeProperties = include.isProperties();
        boolean includeRelationships = include.isRelationships();
        MetadataProvider<com.buschmais.xo.neo4j.spi.metadata.NodeMetadata<Neo4jLabel>, Neo4jLabel, com.buschmais.xo.neo4j.spi.metadata.RelationshipMetadata<Neo4jRelationshipType>, Neo4jRelationshipType> metadataProvider = store.getXOManagerFactory()
            .getMetadataProvider();
        Map<Class<?>, TypeMetadata> registeredMetadata = metadataProvider.getRegisteredMetadata();

        // labels per entity interface
        Map<Class<?>, Set<String>> labels = new HashMap<>();
        Map<Class<?>, Set<MethodMetadata<?, ?>>> properties = new HashMap<>();
        aggregateTypeHierarchies(registeredMetadata, metadataProvider, properties, labels);
        // properties and relationships per entity interface
        GetSchemaResult.GetSchemaResultBuilder schemaBuilder = GetSchemaResult.builder();
        for (Map.Entry<Class<?>, Set<String>> nodeEntry : labels.entrySet()) {
            TypeMetadata typeMetadata = registeredMetadata.get(nodeEntry.getKey());
            if (typeMetadata instanceof EntityTypeMetadata && !((EntityTypeMetadata<?>) typeMetadata).isAbstract()) {
                Set<String> nodeLabels = nodeEntry.getValue();
                List<PropertyMetadata> propertyMetadata = new ArrayList<>();
                List<RelationshipMetadata> relationshipMetadata = new ArrayList<>();
                if (labelFilters == null || labelFilters.stream()
                    .anyMatch(nodeLabels::containsAll)) {
                    NodeMetadata.NodeMetadataBuilder<?, ?> nodeBuilder = NodeMetadata.builder()
                        .labels(nodeLabels);
                    for (MethodMetadata<?, ?> property : properties.get(nodeEntry.getKey())) {
                        // TODO duplicates from inheritance hierarchies?
                        if (includeProperties && property instanceof PrimitivePropertyMethodMetadata<?> primitivePropertyMethodMetadata) {
                            primitiveProperty(primitivePropertyMethodMetadata).ifPresent(propertyMetadata::add);
                        } else if (includeRelationships) {
                            if (property instanceof AbstractEntityRelationMethodMetadata<?> abstractEntityRelationMethodMetadata) {
                                if (FROM.equals(abstractEntityRelationMethodMetadata.getDirection())) {
                                    relationshipMetadata.add(entityRelationProperty(abstractEntityRelationMethodMetadata, labels));
                                }
                            } else if (property instanceof AbstractRelationPropertyMethodMetadata<?> abstractRelationPropertyMethodMetadata) {
                                if (FROM.equals(abstractRelationPropertyMethodMetadata.getDirection())) {
                                    relationshipMetadata.add(relationTypeProperty(abstractRelationPropertyMethodMetadata, labels));
                                }
                            }
                        }
                    }
                    if (includeProperties) {
                        nodeBuilder.properties(propertyMetadata);
                    }
                    if (includeRelationships) {
                        nodeBuilder.relationships(relationshipMetadata);
                    }
                    schemaBuilder.node(nodeBuilder.build());
                }
            }
        }
        GetSchemaResult schema = schemaBuilder.build();
        if (log.isInfoEnabled()) {
            log.info("Schema: {}", new ObjectMapper().writerWithDefaultPrettyPrinter()
                .writeValueAsString(schemaBuilder.build()));
        }
        return schema;
    }

    private static void aggregateTypeHierarchies(Map<Class<?>, TypeMetadata> registeredMetadata,
        MetadataProvider<com.buschmais.xo.neo4j.spi.metadata.NodeMetadata<Neo4jLabel>, Neo4jLabel, com.buschmais.xo.neo4j.spi.metadata.RelationshipMetadata<Neo4jRelationshipType>, Neo4jRelationshipType> metadataProvider,
        Map<Class<?>, Set<MethodMetadata<?, ?>>> properties, Map<Class<?>, Set<String>> labels) {
        for (Map.Entry<Class<?>, TypeMetadata> typeMetadataEntry : registeredMetadata.entrySet()) {
            TypeMetadata typeMetadata = typeMetadataEntry.getValue();
            if (typeMetadata instanceof EntityTypeMetadata) {
                Class<?> entityInterface = typeMetadataEntry.getKey();
                Deque<TypeMetadata> metadataStack = new ArrayDeque<>();
                metadataStack.add(typeMetadata);
                while (!metadataStack.isEmpty()) {
                    TypeMetadata currentTypeMetadata = metadataStack.pop();
                    properties.computeIfAbsent(entityInterface, k -> new HashSet<>())
                        .addAll(currentTypeMetadata.getProperties());
                    if (currentTypeMetadata instanceof EntityTypeMetadata) {
                        EntityTypeMetadata<com.buschmais.xo.neo4j.spi.metadata.NodeMetadata<Neo4jLabel>> entityMetadata = metadataProvider.getEntityMetadata(
                            currentTypeMetadata.getAnnotatedType()
                                .getAnnotatedElement());
                        Neo4jLabel discriminator = entityMetadata.getDatastoreMetadata()
                            .getDiscriminator();
                        if (discriminator != null) {
                            labels.computeIfAbsent(entityInterface, k -> new LinkedHashSet<>())
                                .add(discriminator.getName());
                        }
                        metadataStack.addAll(entityMetadata.getSuperTypes());
                    }
                }
            }
        }
    }

    private static RelationshipMetadata entityRelationProperty(AbstractEntityRelationMethodMetadata<?> abstractEntityRelationMethodMetadata,
        Map<Class<?>, Set<String>> labels) {
        RelationTypeMetadata<?> relationTypeMetadata = abstractEntityRelationMethodMetadata.getRelationshipMetadata();
        com.buschmais.xo.neo4j.spi.metadata.RelationshipMetadata<?> relationshipMetadata = (com.buschmais.xo.neo4j.spi.metadata.RelationshipMetadata<?>) relationTypeMetadata.getDatastoreMetadata();
        Neo4jRelationshipType neo4jRelationshipType = relationshipMetadata.getDiscriminator();
        return RelationshipMetadata.builder()
            .type(neo4jRelationshipType.getName())
            .targetLabels(labels.get(abstractEntityRelationMethodMetadata.getElementType()))
            .build();
    }

    private static RelationshipMetadata relationTypeProperty(AbstractRelationPropertyMethodMetadata<?> abstractRelationPropertyMethodMetadata,
        Map<Class<?>, Set<String>> labels) {
        RelationTypeMetadata<?> relationTypeMetadata = abstractRelationPropertyMethodMetadata.getRelationshipMetadata();
        com.buschmais.xo.neo4j.spi.metadata.RelationshipMetadata<?> relationshipMetadata = (com.buschmais.xo.neo4j.spi.metadata.RelationshipMetadata<?>) relationTypeMetadata.getDatastoreMetadata();
        Neo4jRelationshipType neo4jRelationshipType = relationshipMetadata.getDiscriminator();
        RelationshipMetadata.RelationshipMetadataBuilder<?, ?> relationshipBuilder = RelationshipMetadata.builder()
            .type(neo4jRelationshipType.getName())
            .targetLabels(labels.get(relationTypeMetadata.getToType()));
        List<PropertyMetadata> properties = new ArrayList<>();
        for (MethodMetadata<?, ?> methodMetadata : relationTypeMetadata.getProperties()) {
            if (methodMetadata instanceof PrimitivePropertyMethodMetadata<?>) {
                primitiveProperty((PrimitivePropertyMethodMetadata<?>) methodMetadata).ifPresent(properties::add);
            }
        }
        relationshipBuilder.properties(properties);
        return relationshipBuilder.build();
    }

    private static Optional<PropertyMetadata> primitiveProperty(PrimitivePropertyMethodMetadata<?> primitivePropertyMethodMetadata) {
        AnnotatedMethod annotatedMethod = primitivePropertyMethodMetadata.getAnnotatedMethod();
        if (annotatedMethod instanceof GetPropertyMethod) {
            Class<?> returnType = annotatedMethod.getAnnotatedElement()
                .getReturnType();
            return PropertyMetadata.Type.of(returnType)
                .map(type -> PropertyMetadata.builder()
                    .name(((com.buschmais.xo.neo4j.spi.metadata.PropertyMetadata) primitivePropertyMethodMetadata.getDatastoreMetadata()).getName())
                    .type(type)
                    .build());
        }
        return empty();
    }
}
