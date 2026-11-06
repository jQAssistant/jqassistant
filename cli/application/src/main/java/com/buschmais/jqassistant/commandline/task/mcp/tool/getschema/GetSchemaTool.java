package com.buschmais.jqassistant.commandline.task.mcp.tool.getschema;

import java.util.*;

import com.buschmais.jqassistant.commandline.task.mcp.tool.Tool;
import com.buschmais.jqassistant.commandline.task.mcp.tool.getschema.model.input.Filter;
import com.buschmais.jqassistant.commandline.task.mcp.tool.getschema.model.input.GetSchema;
import com.buschmais.jqassistant.commandline.task.mcp.tool.getschema.model.input.Include;
import com.buschmais.jqassistant.commandline.task.mcp.tool.getschema.model.output.NodeMetadata;
import com.buschmais.jqassistant.commandline.task.mcp.tool.getschema.model.output.PropertyMetadata;
import com.buschmais.jqassistant.commandline.task.mcp.tool.getschema.model.output.RelationshipMetadata;
import com.buschmais.jqassistant.commandline.task.mcp.tool.getschema.model.output.Schema;
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
                  "type" : "object",
                  "description" : "Defines the optional filter to apply for nodes.",
                  "properties" : {
                    "labels" : {
                      "type" : "array",
                      "description" : "The list of label filters.",
                      "items": {
                        "type" : "array",
                        "description" : "A label filter consisting of a list of labels.",
                        "items": {
                          "type": "string",
                          "description" : "A label."
                        }
                      }
                    }
                  }
                },
                "include": {
                  "type" : "object",
                  "description" : "Defines the optional schema items to be included.",
                  "properties": {
                    "properties": {
                      "type" : "boolean",
                      "description": "If set to true include all the properties for a node."
                    },
                    "relationships": {
                      "type" : "boolean",
                      "description": "If set to true include all outgoing relationships for a nodes."
                    }
                  }
                }
              },
              "required": []
            }
            """;
    }

    @Override
    public String getName() {
        return "get-schema";
    }

    @Override
    public String getDescription() {
        return """
        Get the schema of the jQAssistant Neo4j database describing nodes and their labels as created by scanners.
        Properties and relationships can optionally be included in the result per node.
        A filter may be used to filter for specific labels.
        """;
    }

    public Schema execute(Map<String, Object> arguments) {
        GetSchema getSchema = mcpJsonMapper.convertValue(arguments, GetSchema.class);
        Filter filter = getSchema.getFilter();
        List<List<String>> labelFilters = filter != null ? filter.getLabels() : null;
        Include include = getSchema.getInclude();
        boolean includeProperties = include != null && include.isProperties();
        boolean includeRelationships = include != null && include.isRelationships();
        MetadataProvider<com.buschmais.xo.neo4j.spi.metadata.NodeMetadata<Neo4jLabel>, Neo4jLabel, com.buschmais.xo.neo4j.spi.metadata.RelationshipMetadata<Neo4jRelationshipType>, Neo4jRelationshipType> metadataProvider = store.getXOManagerFactory()
            .getMetadataProvider();
        Map<Class<?>, TypeMetadata> registeredMetadata = metadataProvider.getRegisteredMetadata();

        // labels per entity interface
        Map<Class<?>, Set<String>> labels = new HashMap<>();
        Map<Class<?>, Set<MethodMetadata<?, ?>>> properties = new HashMap<>();
        aggregateTypeHierarchies(registeredMetadata, metadataProvider, properties, labels);
        // properties and relationships per entity interface
        Schema.SchemaBuilder schemaBuilder = Schema.builder();
        for (Map.Entry<Class<?>, Set<String>> nodeEntry : labels.entrySet()) {
            TypeMetadata typeMetadata = registeredMetadata.get(nodeEntry.getKey());
            if (typeMetadata instanceof EntityTypeMetadata && !((EntityTypeMetadata<?>) typeMetadata).isAbstract()) {
                Set<String> nodeLabels = nodeEntry.getValue();
                List<PropertyMetadata> propertyMetadata = new ArrayList<>();
                List<RelationshipMetadata> relationshipMetadata = new ArrayList<>();
                if (labelFilters == null || labelFilters.stream()
                    .anyMatch(labelFilter -> nodeLabels.containsAll(labelFilter))) {
                    NodeMetadata.NodeMetadataBuilder<?, ?> nodeBuilder = NodeMetadata.builder()
                        .labels(nodeLabels);
                    for (MethodMetadata<?, ?> property : properties.get(nodeEntry.getKey())) {
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
        Schema schema = schemaBuilder.build();
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
