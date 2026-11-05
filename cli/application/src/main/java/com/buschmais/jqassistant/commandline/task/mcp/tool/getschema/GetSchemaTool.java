package com.buschmais.jqassistant.commandline.task.mcp.tool.getschema;

import java.util.*;

import com.buschmais.jqassistant.commandline.task.mcp.tool.Tool;
import com.buschmais.jqassistant.commandline.task.mcp.tool.getschema.model.NodeMetadata;
import com.buschmais.jqassistant.commandline.task.mcp.tool.getschema.model.PropertyMetadata;
import com.buschmais.jqassistant.commandline.task.mcp.tool.getschema.model.RelationshipMetadata;
import com.buschmais.jqassistant.commandline.task.mcp.tool.getschema.model.Schema;
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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

import static com.buschmais.xo.api.metadata.type.RelationTypeMetadata.Direction.FROM;
import static java.util.Optional.empty;

@Slf4j
@RequiredArgsConstructor
public class GetSchemaTool implements Tool {

    private final Store store;

    @Override
    public String getInputSchema() {
        return """
            {
              "type": "object",
              "properties": {
                "labelFilter": {
                  "type" : "array",
                  "description" : "The list of label filters. Only node schemas providing all given labels are returned.",
                  "items" : {
                    "type" : "array",
                    "description" : "A label filter describing a set of labels applied to a node.",
                    "items" : {
                      "type": "string",
                      "description": "A label"
                    }
                  }
                },
                "includeProperties": {
                  "type": "boolean",
                  "description": "If set to true include all the properties for the node."
                },
                "includeRelationships": {
                  "type": "boolean",
                  "description": "If set to true include all outgoing relationships for the nodes."
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
        return "Get the schema of the Neo4j database created by jQAssistant. It consists of nodes with their properties and relationships that can be used for creating Cypher queries.";
    }

    public Schema execute(Map<String, Object> arguments) {
        List<List<String>> labelFilters = (List<List<String>>) arguments.get("labelFilter");
        Boolean includeProperties = (Boolean) arguments.getOrDefault("includeProperties", Boolean.FALSE);
        Boolean includeRelationships = (Boolean) arguments.getOrDefault("includeRelationships", Boolean.FALSE);
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
                    schemaBuilder.nodeMetadata(nodeBuilder.build());
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
