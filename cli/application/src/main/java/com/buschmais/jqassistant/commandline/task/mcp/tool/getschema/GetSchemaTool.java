package com.buschmais.jqassistant.commandline.task.mcp.tool.getschema;

import java.util.*;

import com.buschmais.jqassistant.commandline.task.mcp.tool.Tool;
import com.buschmais.jqassistant.commandline.task.mcp.tool.getschema.model.*;
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
import com.buschmais.xo.neo4j.spi.metadata.NodeMetadata;
import com.buschmais.xo.neo4j.spi.metadata.PropertyMetadata;
import com.buschmais.xo.neo4j.spi.metadata.RelationshipMetadata;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@RequiredArgsConstructor
public class GetSchemaTool implements Tool {

    private final Store store;

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
        return "get-schema";
    }

    @Override
    public String getDescription() {
        return "Get the schema of the Neo4j database created by jQAssistant. It consists of nodes with their properties and relationships that can be used for creating Cypher queries.";
    }

    public Schema execute() {
        MetadataProvider<NodeMetadata<Neo4jLabel>, Neo4jLabel, RelationshipMetadata<Neo4jRelationshipType>, Neo4jRelationshipType> metadataProvider = store.getXOManagerFactory()
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
                Node.NodeBuilder<?, ?> nodeBuilder = Node.builder()
                    .labels(nodeEntry.getValue());
                for (MethodMetadata<?, ?> property : properties.get(nodeEntry.getKey())) {
                    if (property instanceof PrimitivePropertyMethodMetadata<?>) {
                        primitiveProperty((PrimitivePropertyMethodMetadata<?>) property, nodeBuilder);
                    } else if (property instanceof AbstractEntityRelationMethodMetadata<?>) {
                        entityRelationProperty((AbstractEntityRelationMethodMetadata<?>) property, labels, nodeBuilder);
                    } else if (property instanceof AbstractRelationPropertyMethodMetadata<?>) {
                        relationTypeProperty((AbstractRelationPropertyMethodMetadata<?>) property, labels, nodeBuilder);
                    }

                }
                schemaBuilder.node(nodeBuilder.build());
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
        MetadataProvider<NodeMetadata<Neo4jLabel>, Neo4jLabel, RelationshipMetadata<Neo4jRelationshipType>, Neo4jRelationshipType> metadataProvider,
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
                        EntityTypeMetadata<NodeMetadata<Neo4jLabel>> entityMetadata = metadataProvider.getEntityMetadata(currentTypeMetadata.getAnnotatedType()
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

    private static void entityRelationProperty(AbstractEntityRelationMethodMetadata<?> abstractEntityRelationMethodMetadata, Map<Class<?>, Set<String>> labels,
        Node.NodeBuilder<?, ?> nodeBuilder) {
        if (RelationTypeMetadata.Direction.FROM.equals(abstractEntityRelationMethodMetadata.getDirection())) {
            RelationTypeMetadata<?> relationTypeMetadata = abstractEntityRelationMethodMetadata.getRelationshipMetadata();
            RelationshipMetadata<?> relationshipMetadata = (RelationshipMetadata<?>) relationTypeMetadata.getDatastoreMetadata();
            Neo4jRelationshipType neo4jRelationshipType = relationshipMetadata.getDiscriminator();
            nodeBuilder.relationship(Relationship.builder()
                .type(neo4jRelationshipType.getName())
                .targetLabels(labels.get(abstractEntityRelationMethodMetadata.getElementType()))
                .build());
        }
    }

    private static void relationTypeProperty(AbstractRelationPropertyMethodMetadata<?> abstractRelationPropertyMethodMetadata,
        Map<Class<?>, Set<String>> labels, Node.NodeBuilder<?, ?> nodeBuilder) {
        if (RelationTypeMetadata.Direction.FROM.equals(abstractRelationPropertyMethodMetadata.getDirection())) {
            RelationTypeMetadata<?> relationTypeMetadata = abstractRelationPropertyMethodMetadata.getRelationshipMetadata();
            RelationshipMetadata<?> relationshipMetadata = (RelationshipMetadata<?>) relationTypeMetadata.getDatastoreMetadata();
            Neo4jRelationshipType neo4jRelationshipType = relationshipMetadata.getDiscriminator();
            Relationship.RelationshipBuilder<?, ?> relationshipBuilder = Relationship.builder()
                .type(neo4jRelationshipType.getName())
                .targetLabels(labels.get(relationTypeMetadata.getToType()));
            for (MethodMetadata<?, ?> methodMetadata : relationTypeMetadata.getProperties()) {
                if (methodMetadata instanceof PrimitivePropertyMethodMetadata<?>) {
                    primitiveProperty((PrimitivePropertyMethodMetadata<?>) methodMetadata, relationshipBuilder);
                }
            }
            nodeBuilder.relationship(relationshipBuilder.build());
        }
    }

    private static void primitiveProperty(PrimitivePropertyMethodMetadata<?> primitivePropertyMethodMetadata,
        AbstractPropertyContainer.AbstractPropertyContainerBuilder<?, ?> builder) {
        AnnotatedMethod annotatedMethod = primitivePropertyMethodMetadata.getAnnotatedMethod();
        if (annotatedMethod instanceof GetPropertyMethod) {
            Class<?> returnType = annotatedMethod.getAnnotatedElement()
                .getReturnType();
            Property.Type.of(returnType)
                .ifPresent(type -> {
                    builder.property(Property.builder()
                        .name(((PropertyMetadata) primitivePropertyMethodMetadata.getDatastoreMetadata()).getName())
                        .type(type)
                        .build());
                });
        }
    }
}
