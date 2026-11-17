package com.buschmais.jqassistant.core.mcp.impl.tool.getschema;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.buschmais.jqassistant.core.mcp.impl.tool.getschema.output.GetSchemaResult;
import com.buschmais.jqassistant.core.mcp.impl.tool.getschema.output.NodeMetadata;
import com.buschmais.jqassistant.core.mcp.impl.tool.getschema.output.PropertyMetadata;
import com.buschmais.jqassistant.core.mcp.impl.tool.getschema.output.RelationshipMetadata;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.xo.api.XOManagerFactory;
import com.buschmais.xo.api.annotation.Abstract;
import com.buschmais.xo.api.bootstrap.XOUnit;
import com.buschmais.xo.api.metadata.MetadataProvider;
import com.buschmais.xo.impl.metadata.MetadataProviderImpl;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;
import com.buschmais.xo.neo4j.api.annotation.Relation.Incoming;
import com.buschmais.xo.neo4j.api.annotation.Relation.Outgoing;
import com.buschmais.xo.neo4j.api.model.Neo4jLabel;
import com.buschmais.xo.neo4j.api.model.Neo4jRelationshipType;
import com.buschmais.xo.neo4j.spi.AbstractNeo4jMetadataFactory;
import com.buschmais.xo.spi.datastore.Datastore;

import io.modelcontextprotocol.json.jackson3.JacksonMcpJsonMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import static com.buschmais.jqassistant.core.mcp.impl.tool.getschema.output.PropertyMetadata.Type.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Verifies the {@link GetSchemaTool} using the XO metadata of a test model.
 */
class GetSchemaToolTest {

    private GetSchemaTool getSchemaTool;

    @BeforeEach
    void setUp() {
        Datastore<?, ?, ?, ?, ?> datastore = mock(Datastore.class);
        doReturn(new TestMetadataFactory()).when(datastore)
            .getMetadataFactory();
        MetadataProvider<?, ?, ?, ?> metadataProvider = new MetadataProviderImpl<>(
            List.of(NamedDescriptor.class, TypeDescriptor.class, TestTypeDescriptor.class, MemberDescriptor.class, DependsOnDescriptor.class),
            (Datastore) datastore, XOUnit.MappingConfiguration.builder()
            .build());
        XOManagerFactory<?, ?, ?, ?> xoManagerFactory = mock(XOManagerFactory.class);
        doReturn(metadataProvider).when(xoManagerFactory)
            .getMetadataProvider();
        Store store = mock(Store.class);
        doReturn(xoManagerFactory).when(store)
            .getXOManagerFactory();
        getSchemaTool = new GetSchemaTool(store, new JacksonMcpJsonMapper(JsonMapper.shared()));
    }

    @Test
    void toolMetadata() {
        assertThat(getSchemaTool.getName()).isEqualTo("get-schema");
        assertThat(getSchemaTool.getDescription()).isNotBlank();
        assertThat(getSchemaTool.getInputSchema()).isNotBlank();
        assertThat(getSchemaTool.getOutputSchema()).isPresent();
        assertThat(getSchemaTool.getAnnotations()
            .readOnlyHint()).isTrue();
        assertThat(getSchemaTool.getAnnotations()
            .destructiveHint()).isFalse();
        assertThat(getSchemaTool.getAnnotations()
            .idempotentHint()).isTrue();
    }

    @Test
    void concreteNodeTypesWithInheritedLabels() {
        List<NodeMetadata> nodes = execute(Map.of());

        // the abstract type "Named" is not reported
        assertThat(nodes).extracting(NodeMetadata::getLabels)
            .containsExactlyInAnyOrder(Set.of("Named", "Type"), Set.of("Named", "Type", "Test"), Set.of("Named", "Member"));
    }

    @Test
    void defaultIncludesPropertiesAndExcludesRelationships() {
        List<NodeMetadata> nodes = execute(Map.of());

        assertThat(nodes).allSatisfy(node -> {
            assertThat(node.getProperties()).isNotNull();
            assertThat(node.getRelationships()).isNull();
        });
    }

    @Test
    void properties() {
        List<NodeMetadata> nodes = execute(Map.of());

        // including inherited properties
        assertThat(getNode(nodes, "Named", "Type").getProperties()).extracting(PropertyMetadata::getName, PropertyMetadata::getType,
                PropertyMetadata::getArray)
            .containsExactlyInAnyOrder(tuple("name", STRING, null), tuple("count", INTEGER, null), tuple("visibility", STRING, null),
                tuple("created", DATETIME, null), tuple("modifiers", STRING, true));
        assertThat(getNode(nodes, "Named", "Type", "Test").getProperties()).extracting(PropertyMetadata::getName, PropertyMetadata::getType,
                PropertyMetadata::getArray)
            .containsExactlyInAnyOrder(tuple("name", STRING, null), tuple("count", INTEGER, null), tuple("visibility", STRING, null),
                tuple("created", DATETIME, null), tuple("modifiers", STRING, true), tuple("framework", STRING, null));
        assertThat(getNode(nodes, "Named", "Member").getProperties()).extracting(PropertyMetadata::getName, PropertyMetadata::getType,
                PropertyMetadata::getArray)
            .containsExactlyInAnyOrder(tuple("name", STRING, null), tuple("active", BOOLEAN, null), tuple("weight", FLOAT, null),
                tuple("size", INTEGER, null));
    }

    @Test
    void excludeProperties() {
        List<NodeMetadata> nodes = execute(Map.of("include", Map.of("properties", false)));

        assertThat(nodes).hasSize(3)
            .allSatisfy(node -> assertThat(node.getProperties()).isNull());
    }

    @Test
    void includeRelationships() {
        List<NodeMetadata> nodes = execute(Map.of("include", Map.of("relationships", true)));

        // only outgoing relationships are reported, i.e. not Member-[:DECLARES]->Type or the incoming DEPENDS_ON
        NodeMetadata type = getNode(nodes, "Named", "Type");
        assertThat(type.getProperties()).isNotEmpty();
        assertThat(type.getRelationships()).extracting(RelationshipMetadata::getType, RelationshipMetadata::getTargetLabels)
            .containsExactlyInAnyOrder(tuple("DECLARES", Set.of("Named", "Member")), tuple("DEPENDS_ON", Set.of("Named", "Type")));
        assertThat(getNode(nodes, "Named", "Member").getRelationships()).isEmpty();
    }

    @Test
    void entityRelationWithoutProperties() {
        List<NodeMetadata> nodes = execute(Map.of("include", Map.of("relationships", true)));

        assertThat(getRelationship(getNode(nodes, "Named", "Type"), "DECLARES").getProperties()).isNull();
    }

    @Test
    void relationTypeWithProperties() {
        List<NodeMetadata> nodes = execute(Map.of("include", Map.of("relationships", true)));

        assertThat(getRelationship(getNode(nodes, "Named", "Type"), "DEPENDS_ON").getProperties()).extracting(PropertyMetadata::getName,
                PropertyMetadata::getType, PropertyMetadata::getArray)
            .containsExactlyInAnyOrder(tuple("weight", INTEGER, null), tuple("scope", STRING, null), tuple("lines", INTEGER, true));
    }

    @Test
    void relationshipsWithoutProperties() {
        List<NodeMetadata> nodes = execute(Map.of("include", Map.of("properties", false, "relationships", true)));

        NodeMetadata type = getNode(nodes, "Named", "Type");
        assertThat(type.getProperties()).isNull();
        assertThat(type.getRelationships()).extracting(RelationshipMetadata::getType)
            .containsExactlyInAnyOrder("DECLARES", "DEPENDS_ON");
    }

    @Test
    void inheritedRelationships() {
        List<NodeMetadata> nodes = execute(Map.of("include", Map.of("relationships", true)));

        assertThat(getNode(nodes, "Named", "Type", "Test").getRelationships()).extracting(RelationshipMetadata::getType)
            .containsExactlyInAnyOrder("DECLARES", "DEPENDS_ON");
    }

    @Test
    void filterBySingleLabel() {
        List<NodeMetadata> nodes = execute(Map.of("filter", Map.of("labels", List.of(List.of("Type")))));

        assertThat(nodes).extracting(NodeMetadata::getLabels)
            .containsExactlyInAnyOrder(Set.of("Named", "Type"), Set.of("Named", "Type", "Test"));
    }

    @Test
    void filterByLabelsCombinedByAnd() {
        List<NodeMetadata> nodes = execute(Map.of("filter", Map.of("labels", List.of(List.of("Type", "Test")))));

        assertThat(nodes).extracting(NodeMetadata::getLabels)
            .containsExactly(Set.of("Named", "Type", "Test"));
    }

    @Test
    void filterByLabelFiltersCombinedByOr() {
        List<NodeMetadata> nodes = execute(Map.of("filter", Map.of("labels", List.of(List.of("Test"), List.of("Member")))));

        assertThat(nodes).extracting(NodeMetadata::getLabels)
            .containsExactlyInAnyOrder(Set.of("Named", "Type", "Test"), Set.of("Named", "Member"));
    }

    @Test
    void filterWithoutMatch() {
        assertThat(execute(Map.of("filter", Map.of("labels", List.of(List.of("Unknown")))))).isEmpty();
    }

    @Test
    void filterWithoutLabels() {
        assertThat(execute(Map.of("filter", Map.of()))).hasSize(3);
    }

    private List<NodeMetadata> execute(Map<String, Object> arguments) {
        GetSchemaResult result = getSchemaTool.execute(arguments);
        return result.getNodes();
    }

    private static NodeMetadata getNode(List<NodeMetadata> nodes, String... labels) {
        return nodes.stream()
            .filter(node -> node.getLabels()
                .equals(Set.of(labels)))
            .findFirst()
            .orElseThrow(() -> new AssertionError("No node with labels " + Set.of(labels)));
    }

    private static RelationshipMetadata getRelationship(NodeMetadata node, String type) {
        return node.getRelationships()
            .stream()
            .filter(relationship -> relationship.getType()
                .equals(type))
            .findFirst()
            .orElseThrow(() -> new AssertionError("No relationship of type " + type));
    }

    private static org.assertj.core.groups.Tuple tuple(Object... values) {
        return org.assertj.core.groups.Tuple.tuple(values);
    }

    // test model

    enum Visibility {
        PUBLIC,
        PRIVATE
    }

    @Abstract
    @Label("Named")
    interface NamedDescriptor {

        String getName();

        void setName(String name);
    }

    @Label("Type")
    interface TypeDescriptor extends NamedDescriptor {

        int getCount();

        void setCount(int count);

        Visibility getVisibility();

        void setVisibility(Visibility visibility);

        ZonedDateTime getCreated();

        void setCreated(ZonedDateTime created);

        String[] getModifiers();

        void setModifiers(String[] modifiers);

        @Relation("DECLARES")
        List<MemberDescriptor> getDeclaredMembers();

        @Outgoing
        List<DependsOnDescriptor> getDependencies();

        @Incoming
        List<DependsOnDescriptor> getDependents();
    }

    @Label("Test")
    interface TestTypeDescriptor extends TypeDescriptor {

        String getFramework();

        void setFramework(String framework);
    }

    @Label("Member")
    interface MemberDescriptor extends NamedDescriptor {

        Boolean getActive();

        void setActive(Boolean active);

        double getWeight();

        void setWeight(double weight);

        Long getSize();

        void setSize(Long size);

        @Relation("DECLARES")
        @Incoming
        TypeDescriptor getDeclaringType();
    }

    @Relation("DEPENDS_ON")
    interface DependsOnDescriptor {

        @Outgoing
        TypeDescriptor getDependent();

        @Incoming
        TypeDescriptor getDependency();

        long getWeight();

        void setWeight(long weight);

        String getScope();

        void setScope(String scope);

        int[] getLines();

        void setLines(int[] lines);
    }

    /**
     * Minimal Neo4j metadata factory, allows creating the XO metadata without a datastore.
     */
    private static class TestMetadataFactory extends AbstractNeo4jMetadataFactory<TestLabel, TestRelationshipType> {

        @Override
        protected TestRelationshipType createRelationshipType(String name) {
            return new TestRelationshipType(name);
        }

        @Override
        protected TestLabel createLabel(String name) {
            return new TestLabel(name);
        }

        @Override
        protected boolean isBatchableDefault() {
            return false;
        }
    }

    private record TestLabel(String name) implements Neo4jLabel {

        @Override
        public String getName() {
            return name;
        }
    }

    private record TestRelationshipType(String name) implements Neo4jRelationshipType {

        @Override
        public String getName() {
            return name;
        }
    }
}
